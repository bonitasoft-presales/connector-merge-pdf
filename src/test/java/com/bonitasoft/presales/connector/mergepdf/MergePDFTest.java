package com.bonitasoft.presales.connector.mergepdf;

import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.connector.ConnectorException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MergePDFTest {

    MergePDF connector;

    @Mock
    Document document1;

    @Mock
    Document document2;

    @BeforeEach
    void setUp() {
        connector = new MergePDF();
    }

    @Test
    void should_throw_exception_if_documents_input_is_missing() {
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_documents_input_is_empty() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, new ArrayList<>());
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_throw_exception_if_less_than_two_documents() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(document1));
        connector.setInputParameters(parameters);
        assertThrows(ConnectorValidationException.class, () ->
                connector.validateInputParameters()
        );
    }

    @Test
    void should_validate_with_two_or_more_documents() throws ConnectorValidationException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(document1, document2));
        connector.setInputParameters(parameters);
        connector.validateInputParameters();
    }

    @Test
    void should_execute_with_valid_documents() throws ConnectorException {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(MergePDF.DOCUMENTS_INPUT, List.of(document1, document2));
        connector.setInputParameters(parameters);
        Map<String, Object> outputs = connector.execute();
        assertThat(outputs).containsKey(MergePDF.MERGED_DOCUMENT_OUTPUT);
    }

}