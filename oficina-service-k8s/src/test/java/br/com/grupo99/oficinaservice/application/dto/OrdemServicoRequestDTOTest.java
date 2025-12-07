package br.com.grupo99.oficinaservice.application.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Testes unitários para OrdemServicoRequestDTO")
class OrdemServicoRequestDTOTest {
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
    @DisplayName("Deve criar OrdemServicoRequestDTO válido com todos os campos")
    void shouldCreateValidOrdemServicoRequestDTO() {
        // Given
        List<UUID> servicosIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> pecasIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                "123.456.789-00",
                "ABC-1234",
                servicosIds,
                pecasIds
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        assertThat(dto.cpfCnpjCliente()).isEqualTo("123.456.789-00");
        assertThat(dto.placaVeiculo()).isEqualTo("ABC-1234");
        assertThat(dto.servicosIds()).isEqualTo(servicosIds);
        assertThat(dto.pecasIds()).isEqualTo(pecasIds);
    }

    @Test
    @DisplayName("Deve criar OrdemServicoRequestDTO válido com listas vazias")
    void shouldCreateValidOrdemServicoRequestDTOWithEmptyLists() {
        // Given
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                "987.654.321-00",
                "XYZ-9876",
                Collections.emptyList(),
                Collections.emptyList()
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        assertThat(dto.servicosIds()).isEmpty();
        assertThat(dto.pecasIds()).isEmpty();
    }

    @Test
    @DisplayName("Deve criar OrdemServicoRequestDTO válido com listas null")
    void shouldCreateValidOrdemServicoRequestDTOWithNullLists() {
        // Given
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                "111.222.333-44",
                "DEF-5678",
                null,
                null
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        assertThat(dto.servicosIds()).isNull();
        assertThat(dto.pecasIds()).isNull();
    }

    @Test
    @DisplayName("Deve falhar validação quando CPF/CNPJ do cliente for vazio")
    void shouldFailValidationWhenCpfCnpjClienteIsBlank() {
        // Given
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                "",
                "ABC-1234",
                Arrays.asList(UUID.randomUUID()),
                Arrays.asList(UUID.randomUUID())
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<OrdemServicoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("cpfCnpjCliente");
    }

    @Test
    @DisplayName("Deve falhar validação quando CPF/CNPJ do cliente for null")
    void shouldFailValidationWhenCpfCnpjClienteIsNull() {
        // Given
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                null,
                "ABC-1234",
                Arrays.asList(UUID.randomUUID()),
                Arrays.asList(UUID.randomUUID())
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<OrdemServicoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("cpfCnpjCliente");
    }

    @Test
    @DisplayName("Deve falhar validação quando placa do veículo for vazia")
    void shouldFailValidationWhenPlacaVeiculoIsBlank() {
        // Given
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                "123.456.789-00",
                "",
                Arrays.asList(UUID.randomUUID()),
                Arrays.asList(UUID.randomUUID())
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<OrdemServicoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("placaVeiculo");
    }

    @Test
    @DisplayName("Deve falhar validação quando placa do veículo for null")
    void shouldFailValidationWhenPlacaVeiculoIsNull() {
        // Given
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                "123.456.789-00",
                null,
                Arrays.asList(UUID.randomUUID()),
                Arrays.asList(UUID.randomUUID())
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(1);
        ConstraintViolation<OrdemServicoRequestDTO> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("placaVeiculo");
    }

    @Test
    @DisplayName("Deve falhar validação quando ambos CPF/CNPJ e placa forem inválidos")
    void shouldFailValidationWhenBothCpfCnpjAndPlacaAreInvalid() {
        // Given
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                "",
                "",
                Arrays.asList(UUID.randomUUID()),
                Arrays.asList(UUID.randomUUID())
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).hasSize(2);
        Set<String> propertyPaths = violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
        assertThat(propertyPaths).containsExactlyInAnyOrder("cpfCnpjCliente", "placaVeiculo");
    }

    @Test
    @DisplayName("Deve manter igualdade entre dois DTOs com mesmos valores")
    void shouldMaintainEqualityBetweenSameDTOs() {
        // Given
        List<UUID> servicosIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        List<UUID> pecasIds = Arrays.asList(UUID.randomUUID());
        
        OrdemServicoRequestDTO dto1 = new OrdemServicoRequestDTO("123.456.789-00", "ABC-1234", servicosIds, pecasIds);
        OrdemServicoRequestDTO dto2 = new OrdemServicoRequestDTO("123.456.789-00", "ABC-1234", servicosIds, pecasIds);

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("Deve ter toString() válido")
    void shouldHaveValidToString() {
        // Given
        List<UUID> servicosIds = Arrays.asList(UUID.randomUUID());
        List<UUID> pecasIds = Arrays.asList(UUID.randomUUID());
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO("123.456.789-00", "ABC-1234", servicosIds, pecasIds);

        // When
        String toString = dto.toString();

        // Then
        assertThat(toString).contains("123.456.789-00");
        assertThat(toString).contains("ABC-1234");
        assertThat(toString).contains("servicosIds");
        assertThat(toString).contains("pecasIds");
    }

    @Test
    @DisplayName("Deve aceitar apenas serviços sem peças")
    void shouldAcceptOnlyServicesWithoutParts() {
        // Given
        List<UUID> servicosIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                "123.456.789-00",
                "ABC-1234",
                servicosIds,
                null
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        assertThat(dto.servicosIds()).hasSize(2);
        assertThat(dto.pecasIds()).isNull();
    }

    @Test
    @DisplayName("Deve aceitar apenas peças sem serviços")
    void shouldAcceptOnlyPartsWithoutServices() {
        // Given
        List<UUID> pecasIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        OrdemServicoRequestDTO dto = new OrdemServicoRequestDTO(
                "987.654.321-00",
                "XYZ-9876",
                null,
                pecasIds
        );

        // When
        Set<ConstraintViolation<OrdemServicoRequestDTO>> violations = validator.validate(dto);

        // Then
        assertThat(violations).isEmpty();
        assertThat(dto.servicosIds()).isNull();
        assertThat(dto.pecasIds()).hasSize(2);
    }
}