package com.bonitasoft.presales.connector.mergepdf;

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MergePDFTest {

    MergePDF connector;

    @Mock
    Document pdfDocument1;

    @Mock
    Document pdfDocument2;

    @Mock
    Document nonPdfDocument;

    @BeforeEach
    void setUp() {
        connector = new MergePDF();
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

}