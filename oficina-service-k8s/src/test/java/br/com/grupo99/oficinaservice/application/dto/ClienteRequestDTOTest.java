package br.com.grupo99.oficinaservice.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Testes unitários para ClienteRequestDTO")
class ClienteRequestDTOTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve criar ClienteRequestDTO válido com todos os campos")
    void shouldCreateValidClienteRequestDTO() {
        // Given
        ClienteRequestDTO dto = new ClienteRequestDTO(
                "João Silva",
                "111.444.777-35", // CPF válido
                "11999887766",
                "joao@email.com"
        );

        // When
        Set<ConstraintViolation<ClienteRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        assertThat(dto.nome()).isEqualTo("João Silva");
        assertThat(dto.cpfCnpj()).isEqualTo("111.444.777-35");
        assertThat(dto.telefone()).isEqualTo("11999887766");
        assertThat(dto.email()).isEqualTo("joao@email.com");
    }

    @Test
    @DisplayName("Deve falhar validação quando nome for vazio")
    void shouldFailValidationWhenNomeIsBlank() {
        // Given
        ClienteRequestDTO dto = new ClienteRequestDTO(
                "",
                "111.444.777-35", // CPF válido
                "11999887766",
                "joao@email.com"
        );

        // When
        Set<ConstraintViolation<ClienteRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O nome não pode ser vazio.");
    }

    @Test
    @DisplayName("Deve falhar validação quando nome for null")
    void shouldFailValidationWhenNomeIsNull() {
        // Given
        ClienteRequestDTO dto = new ClienteRequestDTO(
                null,
                "111.444.777-35", // CPF válido
                "11999887766",
                "joao@email.com"
        );

        // When
        Set<ConstraintViolation<ClienteRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O nome não pode ser vazio.");
    }

    @Test
    @DisplayName("Deve falhar validação quando CPF/CNPJ for vazio")
    void shouldFailValidationWhenCpfCnpjIsBlank() {
        // Given
        ClienteRequestDTO dto = new ClienteRequestDTO(
                "João Silva",
                "",
                "11999887766",
                "joao@email.com"
        );

        // When
        Set<ConstraintViolation<ClienteRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O CPF/CNPJ não pode ser vazio.");
    }

    @Test
    @DisplayName("Deve falhar validação quando CPF/CNPJ for null")
    void shouldFailValidationWhenCpfCnpjIsNull() {
        // Given
        ClienteRequestDTO dto = new ClienteRequestDTO(
                "João Silva",
                null,
                "11999887766",
                "joao@email.com"
        );

        // When
        Set<ConstraintViolation<ClienteRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O CPF/CNPJ não pode ser vazio.");
    }

    @Test
    @DisplayName("Deve falhar validação quando CPF/CNPJ for inválido")
    void shouldFailValidationWhenCpfCnpjIsInvalid() {
        // Given
        ClienteRequestDTO dto = new ClienteRequestDTO(
                "João Silva",
                "123.456.789-00", // CPF inválido
                "11999887766",
                "joao@email.com"
        );

        // When
        Set<ConstraintViolation<ClienteRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("CPF ou CNPJ inválido");
    }

    @Test
    @DisplayName("Deve falhar validação quando email for inválido")
    void shouldFailValidationWhenEmailIsInvalid() {
        // Given
        ClienteRequestDTO dto = new ClienteRequestDTO(
                "João Silva",
                "111.444.777-35", // CPF válido
                "11999887766",
                "email_invalido"
        );

        // When
        Set<ConstraintViolation<ClienteRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O formato do e-mail é inválido.");
    }

    @Test
    @DisplayName("Deve aceitar telefone null ou vazio")
    void shouldAcceptNullOrEmptyTelefone() {
        // Given
        ClienteRequestDTO dtoWithNullTelefone = new ClienteRequestDTO(
                "João Silva",
                "111.444.777-35", // CPF válido
                null,
                "joao@email.com"
        );

        ClienteRequestDTO dtoWithEmptyTelefone = new ClienteRequestDTO(
                "Maria Santos",
                "111.222.333-96", // CPF válido
                "",
                "maria@email.com"
        );

        // When
        Set<ConstraintViolation<ClienteRequestDTO>> violationsNull = validator.validate(dtoWithNullTelefone);
        Set<ConstraintViolation<ClienteRequestDTO>> violationsEmpty = validator.validate(dtoWithEmptyTelefone);

        // Then
        assertThat(violationsNull).isEmpty();
        assertThat(violationsEmpty).isEmpty();
    }

    @Test
    @DisplayName("Deve validar CNPJ válido")
    void shouldValidateValidCNPJ() {
        // Given
        ClienteRequestDTO dto = new ClienteRequestDTO(
                "Empresa LTDA",
                "11.222.333/0001-81", // CNPJ válido
                "11999887766",
                "empresa@email.com"
        );

        // When
        Set<ConstraintViolation<ClienteRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Deve manter igualdade entre dois DTOs com mesmos valores")
    void shouldMaintainEqualityBetweenSameDTOs() {
        // Given
        ClienteRequestDTO dto1 = new ClienteRequestDTO("Nome", "111.444.777-35", "11999887766", "email@test.com");
        ClienteRequestDTO dto2 = new ClienteRequestDTO("Nome", "111.444.777-35", "11999887766", "email@test.com");

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString() válido")
    void shouldHaveValidToString() {
        // Given
        ClienteRequestDTO dto = new ClienteRequestDTO("Nome Teste", "111.444.777-35", "11999887766", "test@email.com");

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("Nome Teste");
        assertThat(toString).contains("111.444.777-35");
        assertThat(toString).contains("11999887766");
        assertThat(toString).contains("test@email.com");
    }
}