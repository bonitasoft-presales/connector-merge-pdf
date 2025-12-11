package com.bonitasoft.presales.connector.mergepdf;

import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

public class MergePDF extends AbstractConnector {

    private static final Logger LOGGER = Logger.getLogger(MergePDF.class.getName());

    static final String yesDOCUMENTS_INPUT = "documents";
    static final String MERGED_DOCUMENT_OUTPUT = "mergedDocument";

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        checkMandatoryDocumentsInput();
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
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        List<Document> documents = (List<Document>) getInputParameter(DOCUMENTS_INPUT);
        LOGGER.info(String.format("Merging %d documents", documents.size()));

        // TODO: Implement PDF merging logic
        // For now, return the first document as placeholder
        Document mergedDocument = documents.get(0);

        setOutputParameter(MERGED_DOCUMENT_OUTPUT, mergedDocument);
    }

    @Override
    public void connect() throws ConnectorException {
    }

    @Override
    public void disconnect() throws ConnectorException {
    }
}
