package br.com.grupo99.oficinaservice.application.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Teste Unitário - DocumentoConstraintValidator")
class DocumentoConstraintValidatorTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private DocumentoConstraintValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DocumentoConstraintValidator();
    }

    // --- Testes de CPF ---

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224725",    // CPF válido
            "11144477735"     // CPF válido
    })
    @DisplayName("Dado um CPF válido, Quando validar, Então deve retornar verdadeiro")
    void givenValidCPF_whenIsValid_thenShouldReturnTrue(String cpf) {
        assertTrue(validator.isValid(cpf, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "11111111111",      // Dígitos repetidos
            "12345678900",      // Dígitos verificadores incorretos
            "52998224726"       // Último dígito incorreto
    })
    @DisplayName("Dado um CPF inválido, Quando validar, Então deve retornar falso")
    void givenInvalidCPF_whenIsValid_thenShouldReturnFalse(String cpf) {
        assertFalse(validator.isValid(cpf, null));
    }

    // --- Testes de CNPJ ---

    @ParameterizedTest
    @ValueSource(strings = {
            "19131243000197",   // CNPJ válido
            "27865757000102"    // CNPJ válido
    })
    @DisplayName("Dado um CNPJ válido, Quando validar, Então deve retornar verdadeiro")
    void givenValidCNPJ_whenIsValid_thenShouldReturnTrue(String cnpj) {
        assertTrue(validator.isValid(cnpj, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000000",      // Dígitos repetidos
            "11222333000100",      // Dígitos verificadores incorretos
            "19131243000190"       // Último dígito incorreto
    })
    @DisplayName("Dado um CNPJ inválido, Quando validar, Então deve retornar falso")
    void givenInvalidCNPJ_whenIsValid_thenShouldReturnFalse(String cnpj) {
        assertFalse(validator.isValid(cnpj, null));
    }

    // --- Testes Gerais (com formatação) ---

    @ParameterizedTest
    @ValueSource(strings = {
            "529.982.247-25",        // CPF com formatação
            "19.131.243/0001-97"     // CNPJ com formatação
    })
    @DisplayName("Dado um documento com formatação, Quando validar, Então deve limpar e validar corretamente")
    void givenFormattedDocument_whenIsValid_thenShouldCleanAndValidate(String documento) {
        assertTrue(validator.isValid(documento, null));
    }

    // --- Testes com nulo e branco ---

    @Test
    @DisplayName("Dado um documento nulo ou em branco, Quando validar, Então deve retornar verdadeiro")
    void givenNullOrBlankDocument_whenIsValid_thenShouldReturnTrue() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));
    }

    // --- Testes adicionais para tamanhos incorretos ---

    @ParameterizedTest
    @ValueSource(strings = {
            "12345678",           // Muito curto
            "1234567890123456"    // Muito longo
    })
    @DisplayName("Dado um documento com quantidade inválida de dígitos, Quando validar, Então deve retornar falso")
    void givenInvalidLengthDocument_whenIsValid_thenShouldReturnFalse(String documento) {
        assertFalse(validator.isValid(documento, null));
    }

    // --- Teste com caracteres inválidos no meio ---

    @Test
    @DisplayName("Dado um documento com caracteres inválidos, Quando validar, Então deve retornar falso")
    void givenInvalidCharactersInDocument_whenIsValid_thenShouldReturnFalse() {
        assertFalse(validator.isValid("ABC52998224725XYZ", null));
        assertFalse(validator.isValid("##19131243000197$$", null));
    }
}
