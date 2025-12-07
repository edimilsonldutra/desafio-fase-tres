package br.com.grupo99.oficinaservice.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Testes unitários para PecaRequestDTO")
class PecaRequestDTOTest {
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
    @DisplayName("Deve criar PecaRequestDTO válido com todos os campos")
    void shouldCreateValidPecaRequestDTO() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO(
                "Filtro de Óleo",
                "Bosch",
                new BigDecimal("25.50"),
                10
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        assertThat(dto.nome()).isEqualTo("Filtro de Óleo");
        assertThat(dto.fabricante()).isEqualTo("Bosch");
        assertThat(dto.preco()).isEqualTo(new BigDecimal("25.50"));
        assertThat(dto.estoque()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve falhar validação quando nome for vazio")
    void shouldFailValidationWhenNomeIsBlank() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO(
                "",
                "Bosch",
                new BigDecimal("25.50"),
                10
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O nome da peça não pode ser vazio.");
    }

    @Test
    @DisplayName("Deve falhar validação quando nome for null")
    void shouldFailValidationWhenNomeIsNull() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO(
                null,
                "Bosch",
                new BigDecimal("25.50"),
                10
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O nome da peça não pode ser vazio.");
    }

    @Test
    @DisplayName("Deve falhar validação quando preço for null")
    void shouldFailValidationWhenPrecoIsNull() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO(
                "Filtro de Óleo",
                "Bosch",
                null,
                10
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O preço não pode ser nulo.");
    }

    @Test
    @DisplayName("Deve falhar validação quando preço for zero")
    void shouldFailValidationWhenPrecoIsZero() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO(
                "Filtro de Óleo",
                "Bosch",
                BigDecimal.ZERO,
                10
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O preço deve ser um valor positivo.");
    }

    @Test
    @DisplayName("Deve falhar validação quando preço for negativo")
    void shouldFailValidationWhenPrecoIsNegative() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO(
                "Filtro de Óleo",
                "Bosch",
                new BigDecimal("-10.00"),
                10
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O preço deve ser um valor positivo.");
    }

    @Test
    @DisplayName("Deve falhar validação quando estoque for negativo")
    void shouldFailValidationWhenEstoqueIsNegative() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO(
                "Filtro de Óleo",
                "Bosch",
                new BigDecimal("25.50"),
                -1
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("O estoque deve ser um valor positivo ou zero.");
    }

    @Test
    @DisplayName("Deve aceitar estoque zero")
    void shouldAcceptEstoqueZero() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO(
                "Filtro de Óleo",
                "Bosch",
                new BigDecimal("25.50"),
                0
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        assertThat(dto.estoque()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve aceitar fabricante null ou vazio")
    void shouldAcceptNullOrEmptyFabricante() {
        // Given
        PecaRequestDTO dtoWithNullFabricante = new PecaRequestDTO(
                "Filtro de Óleo",
                null,
                new BigDecimal("25.50"),
                10
        );

        PecaRequestDTO dtoWithEmptyFabricante = new PecaRequestDTO(
                "Pastilha de Freio",
                "",
                new BigDecimal("35.00"),
                5
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violationsNull = validator.validate(dtoWithNullFabricante);
        Set<ConstraintViolation<PecaRequestDTO>> violationsEmpty = validator.validate(dtoWithEmptyFabricante);

        // Then
        assertThat(violationsNull).isEmpty();
        assertThat(violationsEmpty).isEmpty();
    }

    @Test
    @DisplayName("Deve manter igualdade entre dois DTOs com mesmos valores")
    void shouldMaintainEqualityBetweenSameDTOs() {
        // Given
        PecaRequestDTO dto1 = new PecaRequestDTO("Peça", "Fabricante", new BigDecimal("10.00"), 5);
        PecaRequestDTO dto2 = new PecaRequestDTO("Peça", "Fabricante", new BigDecimal("10.00"), 5);

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString() válido")
    void shouldHaveValidToString() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO("Filtro de Ar", "Mann", new BigDecimal("15.75"), 8);

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("Filtro de Ar");
        assertThat(toString).contains("Mann");
        assertThat(toString).contains("15.75");
        assertThat(toString).contains("8");
    }

    @Test
    @DisplayName("Deve aceitar valores decimais no preço")
    void shouldAcceptDecimalPrices() {
        // Given
        PecaRequestDTO dto = new PecaRequestDTO(
                "Óleo do Motor",
                "Shell",
                new BigDecimal("45.99"),
                20
        );

        // When
        Set<ConstraintViolation<PecaRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        assertThat(dto.preco()).isEqualTo(new BigDecimal("45.99"));
    }
}