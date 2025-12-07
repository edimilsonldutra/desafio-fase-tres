package br.com.grupo99.oficinaservice.infrastructure.rest.handler;

import br.com.grupo99.oficinaservice.application.exception.BusinessException;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private BindingResult bindingResult;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleBusinessException() {
        // Given
        String errorMessage = "Erro de negócio específico";
        BusinessException exception = new BusinessException(errorMessage);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleBusinessException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().get("erro"));
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        // Given
        String errorMessage = "Recurso não encontrado";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().get("erro"));
    }

    @Test
    void shouldHandleValidationExceptions() throws Exception {
        // Given
        FieldError fieldError1 = new FieldError("object", "field1", "Campo obrigatório");
        FieldError fieldError2 = new FieldError("object", "field2", "Valor inválido");
        
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Create a mock MethodParameter for MethodArgumentNotValidException
        Method method = this.getClass().getDeclaredMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Dados inválidos", response.getBody().get("mensagem"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("erros");
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("Campo obrigatório", errors.get("field1"));
        assertEquals("Valor inválido", errors.get("field2"));
    }

    @Test
    void shouldHandleGenericException() {
        // Given
        String errorMessage = "Erro inesperado";
        Exception exception = new RuntimeException(errorMessage);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ocorreu um erro inesperado no servidor.", response.getBody().get("erro"));
    }

    @Test
    void shouldHandleNullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("Referência nula");

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Ocorreu um erro inesperado no servidor.", response.getBody().get("erro"));
    }

    @Test
    void shouldHandleValidationExceptionWithSingleError() throws Exception {
        // Given
        FieldError fieldError = new FieldError("object", "nome", "Nome não pode estar vazio");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        Method method = this.getClass().getDeclaredMethod("dummyMethod", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Dados inválidos", response.getBody().get("mensagem"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody().get("erros");
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("Nome não pode estar vazio", errors.get("nome"));
    }

    @Test
    void shouldHandleResourceNotFoundExceptionWithEmptyMessage() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("");

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("", response.getBody().get("erro"));
    }

    @Test
    void shouldHandleBusinessExceptionWithLongMessage() {
        // Given
        String longMessage = "Esta é uma mensagem de erro muito longa que pode acontecer em situações complexas de negócio";
        BusinessException exception = new BusinessException(longMessage);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleBusinessException(exception);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(longMessage, response.getBody().get("erro"));
    }

    // Dummy method for creating MethodParameter in tests
    private void dummyMethod(String parameter) {
        // This method is used only for testing purposes
    }
}