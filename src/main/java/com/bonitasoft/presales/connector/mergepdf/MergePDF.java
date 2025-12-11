package com.bonitasoft.presales.connector.mergepdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public class MergePDF extends AbstractConnector {

    private static final Logger LOGGER = Logger.getLogger(MergePDF.class.getName());
    private static final String PDF_MIME_TYPE = "application/pdf";

    static final String DOCUMENTS_INPUT = "documents";
    static final String OUTPUT_FILE_NAME_INPUT = "outputFileName";
    static final String MERGED_DOCUMENT_OUTPUT = "mergedDocument";

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        checkMandatoryDocumentsInput();
        checkMandatoryOutputFileName();
    }

    protected void checkMandatoryDocumentsInput() throws ConnectorValidationException {
        List<?> documents = (List<?>) getInputParameter(DOCUMENTS_INPUT);
        if (documents == null || documents.isEmpty()) {
            throw new ConnectorValidationException(this,
                    String.format("Mandatory parameter '%s' is missing or empty.", DOCUMENTS_INPUT));
        }
        if (documents.size() < 2) {
            throw new ConnectorValidationException(this,
                    "At least 2 documents are required to merge.");
        }
        for (Object doc : documents) {
            if (!(doc instanceof Document)) {
                throw new ConnectorValidationException(this,
                        "All items in the documents list must be Document objects.");
            }
            Document document = (Document) doc;
            if (!isPdfDocument(document)) {
                throw new ConnectorValidationException(this,
                        String.format("Document '%s' is not a PDF. Only PDF documents can be merged.",
                                document.getContentFileName()));
            }
        }
    }

    protected void checkMandatoryOutputFileName() throws ConnectorValidationException {
        String outputFileName = (String) getInputParameter(OUTPUT_FILE_NAME_INPUT);
        if (outputFileName == null || outputFileName.trim().isEmpty()) {
            throw new ConnectorValidationException(this,
                    String.format("Mandatory parameter '%s' is missing or empty.", OUTPUT_FILE_NAME_INPUT));
        }
    }

    private boolean isPdfDocument(Document document) {
        String mimeType = document.getContentMimeType();
        String fileName = document.getContentFileName();
        return PDF_MIME_TYPE.equalsIgnoreCase(mimeType) ||
                (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        List<Document> documents = (List<Document>) getInputParameter(DOCUMENTS_INPUT);
        String outputFileName = (String) getInputParameter(OUTPUT_FILE_NAME_INPUT);

        LOGGER.info(String.format("Merging %d PDF documents into '%s'", documents.size(), outputFileName));

        try {
            byte[] mergedPdfContent = mergePdfDocuments(documents);
            DocumentValue documentValue = new DocumentValue(mergedPdfContent, PDF_MIME_TYPE, outputFileName);
            setOutputParameter(MERGED_DOCUMENT_OUTPUT, documentValue);
            LOGGER.info(String.format("Successfully merged %d documents into '%s' (%d bytes)",
                    documents.size(), outputFileName, mergedPdfContent.length));
        } catch (IOException | DocumentNotFoundException e) {
            throw new ConnectorException("Failed to merge PDF documents: " + e.getMessage(), e);
        }
    }

    private byte[] mergePdfDocuments(List<Document> documents) throws IOException, DocumentNotFoundException {
        ProcessAPI processAPI = getAPIAccessor().getProcessAPI();
        PDFMergerUtility pdfMerger = new PDFMergerUtility();

        for (Document document : documents) {
            byte[] content = processAPI.getDocumentContent(document.getContentStorageId());
            pdfMerger.addSource(new ByteArrayInputStream(content));
            LOGGER.fine(String.format("Added document '%s' to merge queue", document.getContentFileName()));
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        pdfMerger.setDestinationStream(outputStream);
        pdfMerger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

        return outputStream.toByteArray();
    }

    @Override
    public void connect() throws ConnectorException {
    }

    @Override
    public void disconnect() throws ConnectorException {
    }
}
