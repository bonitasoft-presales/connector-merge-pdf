package com.bonitasoft.presales.connector.mergepdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MergePDFTest {

    // Test subclass to expose protected methods
    static class TestableMergePDF extends MergePDF {
        public Map<String, Object> getOutputs() {
            return getOutputParameters();
        }
    }

    TestableMergePDF connector;

    @Mock
    Document pdfDocument1;

    @Mock
    Document pdfDocument2;

    @Mock
    Document nonPdfDocument;

    @Mock
    APIAccessor apiAccessor;

    @Mock
    ProcessAPI processAPI;

    @BeforeEach
    void setUp() {
        connector = new TestableMergePDF();
    }

    // Helper method to create a simple PDF with text content
    private byte[] createPdfWithText(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(text);
                contentStream.endText();
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    // Helper method to extract text from PDF bytes
    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // Helper method to count pages in PDF
    private int countPagesInPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            return document.getNumberOfPages();
        }
    }

    @Test
    void should_throw_exception_if_documents_input_is_missing() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_documents_input_is_empty() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, new ArrayList<>());
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_less_than_two_documents() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_output_filename_is_missing() {
        when(pdfDocument1.getContentMimeType()).thenReturn("application/pdf");
        when(pdfDocument2.getContentMimeType()).thenReturn("application/pdf");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, pdfDocument2));
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_output_filename_is_empty() {
        when(pdfDocument1.getContentMimeType()).thenReturn("application/pdf");
        when(pdfDocument2.getContentMimeType()).thenReturn("application/pdf");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, pdfDocument2));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "  ");
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_document_is_not_pdf() {
        when(pdfDocument1.getContentMimeType()).thenReturn("application/pdf");
        when(nonPdfDocument.getContentMimeType()).thenReturn("text/plain");
        when(nonPdfDocument.getContentFileName()).thenReturn("document.txt");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, nonPdfDocument));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_input_is_not_a_document_object() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of("not a document", "also not a document"));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");
        connector.setInputParameters(parameters);
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
        assertThat(exception.getMessage()).contains("Document objects");
    }

    @Test
    void should_accept_pdf_by_mime_type() {
        when(pdfDocument1.getContentMimeType()).thenReturn("application/pdf");
        when(pdfDocument2.getContentMimeType()).thenReturn("application/pdf");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, pdfDocument2));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");
        connector.setInputParameters(parameters);
        assertDoesNotThrow(() -> connector.validateInputParameters());
    }

    @Test
    void should_accept_pdf_by_file_extension() {
        when(pdfDocument1.getContentMimeType()).thenReturn(null);
        when(pdfDocument1.getContentFileName()).thenReturn("document1.pdf");
        when(pdfDocument2.getContentMimeType()).thenReturn(null);
        when(pdfDocument2.getContentFileName()).thenReturn("document2.PDF");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, pdfDocument2));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");
        connector.setInputParameters(parameters);
        assertDoesNotThrow(() -> connector.validateInputParameters());
    }

    // ==================== Business Logic Tests ====================

    @Test
    void should_merge_two_pdf_documents() throws Exception {
        // Given
        byte[] pdf1Content = createPdfWithText("Content from PDF 1");
        byte[] pdf2Content = createPdfWithText("Content from PDF 2");

        when(pdfDocument1.getContentStorageId()).thenReturn("storage-id-1");
        when(pdfDocument2.getContentStorageId()).thenReturn("storage-id-2");

        when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
        when(processAPI.getDocumentContent("storage-id-1")).thenReturn(pdf1Content);
        when(processAPI.getDocumentContent("storage-id-2")).thenReturn(pdf2Content);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, pdfDocument2));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");

        connector.setInputParameters(parameters);
        connector.setAPIAccessor(apiAccessor);

        // When
        connector.executeBusinessLogic();

        // Then
        DocumentValue result = (DocumentValue) connector.getOutputs().get(MergePDF.MERGED_DOCUMENT_OUTPUT);
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("merged.pdf");
        assertThat(result.getMimeType()).isEqualTo("application/pdf");
        assertThat(result.getContent()).isNotNull();

        // Verify merged PDF has 2 pages
        int pageCount = countPagesInPdf(result.getContent());
        assertThat(pageCount).isEqualTo(2);

        // Verify content from both PDFs is present
        String mergedText = extractTextFromPdf(result.getContent());
        assertThat(mergedText).contains("Content from PDF 1");
        assertThat(mergedText).contains("Content from PDF 2");
    }

    @Test
    void should_merge_multiple_pdf_documents() throws Exception {
        // Given
        byte[] pdf1Content = createPdfWithText("Page 1");
        byte[] pdf2Content = createPdfWithText("Page 2");
        byte[] pdf3Content = createPdfWithText("Page 3");

        Document pdfDocument3 = org.mockito.Mockito.mock(Document.class);

        when(pdfDocument1.getContentStorageId()).thenReturn("storage-id-1");
        when(pdfDocument2.getContentStorageId()).thenReturn("storage-id-2");
        when(pdfDocument3.getContentStorageId()).thenReturn("storage-id-3");

        when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
        when(processAPI.getDocumentContent("storage-id-1")).thenReturn(pdf1Content);
        when(processAPI.getDocumentContent("storage-id-2")).thenReturn(pdf2Content);
        when(processAPI.getDocumentContent("storage-id-3")).thenReturn(pdf3Content);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, pdfDocument2, pdfDocument3));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged-three.pdf");

        connector.setInputParameters(parameters);
        connector.setAPIAccessor(apiAccessor);

        // When
        connector.executeBusinessLogic();

        // Then
        DocumentValue result = (DocumentValue) connector.getOutputs().get(MergePDF.MERGED_DOCUMENT_OUTPUT);
        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("merged-three.pdf");

        // Verify merged PDF has 3 pages
        int pageCount = countPagesInPdf(result.getContent());
        assertThat(pageCount).isEqualTo(3);

        // Verify content from all PDFs is present
        String mergedText = extractTextFromPdf(result.getContent());
        assertThat(mergedText).contains("Page 1");
        assertThat(mergedText).contains("Page 2");
        assertThat(mergedText).contains("Page 3");
    }

    @Test
    void should_throw_exception_when_document_not_found() throws Exception {
        // Given
        when(pdfDocument1.getContentStorageId()).thenReturn("storage-id-1");
        when(pdfDocument2.getContentStorageId()).thenReturn("invalid-storage-id");

        when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
        when(processAPI.getDocumentContent("storage-id-1")).thenReturn(createPdfWithText("Content"));
        when(processAPI.getDocumentContent("invalid-storage-id"))
                .thenThrow(new DocumentNotFoundException("Document not found"));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, pdfDocument2));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");

        connector.setInputParameters(parameters);
        connector.setAPIAccessor(apiAccessor);

        // When / Then
        ConnectorException exception = assertThrows(ConnectorException.class,
                () -> connector.executeBusinessLogic());
        assertThat(exception.getMessage()).contains("Failed to merge PDF documents");
    }

    @Test
    void should_throw_exception_when_document_content_is_not_valid_pdf() throws Exception {
        // Given - one valid PDF and one invalid content (not a PDF)
        byte[] validPdfContent = createPdfWithText("Valid PDF");
        byte[] invalidContent = "This is not a PDF file, just plain text".getBytes();

        when(pdfDocument1.getContentStorageId()).thenReturn("storage-id-1");
        when(pdfDocument2.getContentStorageId()).thenReturn("storage-id-2");

        when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
        when(processAPI.getDocumentContent("storage-id-1")).thenReturn(validPdfContent);
        when(processAPI.getDocumentContent("storage-id-2")).thenReturn(invalidContent);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, pdfDocument2));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "merged.pdf");

        connector.setInputParameters(parameters);
        connector.setAPIAccessor(apiAccessor);

        // When / Then
        ConnectorException exception = assertThrows(ConnectorException.class,
                () -> connector.executeBusinessLogic());
        assertThat(exception.getMessage()).contains("Failed to merge PDF documents");
    }

    @Test
    void should_preserve_page_order_when_merging() throws Exception {
        // Given - create PDFs with distinguishable content
        byte[] pdf1Content = createPdfWithText("FIRST_DOCUMENT");
        byte[] pdf2Content = createPdfWithText("SECOND_DOCUMENT");

        when(pdfDocument1.getContentStorageId()).thenReturn("storage-id-1");
        when(pdfDocument2.getContentStorageId()).thenReturn("storage-id-2");

        when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
        when(processAPI.getDocumentContent("storage-id-1")).thenReturn(pdf1Content);
        when(processAPI.getDocumentContent("storage-id-2")).thenReturn(pdf2Content);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(pdfDocument1, pdfDocument2));
        parameters.put(MergePDF.OUTPUT_FILE_NAME_INPUT, "ordered.pdf");

        connector.setInputParameters(parameters);
        connector.setAPIAccessor(apiAccessor);

        // When
        connector.executeBusinessLogic();

        // Then - verify order by extracting text from each page
        DocumentValue result = (DocumentValue) connector.getOutputs().get(MergePDF.MERGED_DOCUMENT_OUTPUT);
        try (PDDocument mergedDoc = PDDocument.load(new ByteArrayInputStream(result.getContent()))) {
            PDFTextStripper stripper = new PDFTextStripper();

            // Extract text from first page only
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            String firstPageText = stripper.getText(mergedDoc);
            assertThat(firstPageText).contains("FIRST_DOCUMENT");

            // Extract text from second page only
            stripper.setStartPage(2);
            stripper.setEndPage(2);
            String secondPageText = stripper.getText(mergedDoc);
            assertThat(secondPageText).contains("SECOND_DOCUMENT");
        }
    }

}