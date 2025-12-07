package br.com.grupo99.oficinaservice.adapter.presenter.impl;

import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Testes unitários para VeiculoPresenterImpl")
class VeiculoPresenterImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private VeiculoPresenterImpl presenter;
    private Veiculo veiculo;
    private UUID veiculoId;

    @BeforeEach
    void setUp() {
        presenter = new VeiculoPresenterImpl();

        veiculoId = UUID.randomUUID();

        veiculo = new Veiculo("ABC1D23", "Toyota", "Corolla", 2023);
        veiculo.setId(veiculoId);
        veiculo.setRenavam("12345678901");
    }

    @Test
    @DisplayName("Deve converter Veiculo para VeiculoResponseDTO")
    void shouldConvertEntityToDTO() {
        // When
        VeiculoResponseDTO result = presenter.paraResponseDTO(veiculo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(veiculoId);
        assertThat(result.placa()).isEqualTo("ABC1D23");
        assertThat(result.renavam()).isEqualTo("12345678901");
        assertThat(result.marca()).isEqualTo("Toyota");
        assertThat(result.modelo()).isEqualTo("Corolla");
        assertThat(result.ano()).isEqualTo(2023);
    }

    @Test
    @DisplayName("Deve retornar null quando veiculo é null")
    void shouldReturnNullWhenVeiculoIsNull() {
        // When
        VeiculoResponseDTO result = presenter.paraResponseDTO(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve converter lista de Veiculos para lista de DTOs")
    void shouldConvertListToDTO() {
        // Given
        Veiculo veiculo2 = new Veiculo("XYZ9A87", "Honda", "Civic", 2024);
        veiculo2.setId(UUID.randomUUID());
        veiculo2.setRenavam("98765432109");

        List<Veiculo> veiculos = Arrays.asList(veiculo, veiculo2);

        // When
        List<VeiculoResponseDTO> result = presenter.paraListaResponseDTO(veiculos);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(veiculoId);
        assertThat(result.get(0).placa()).isEqualTo("ABC1D23");
        assertThat(result.get(1).placa()).isEqualTo("XYZ9A87");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando lista é null")
    void shouldReturnEmptyListWhenListIsNull() {
        // When
        List<VeiculoResponseDTO> result = presenter.paraListaResponseDTO(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando lista está vazia")
    void shouldReturnEmptyListWhenListIsEmpty() {
        // Given
        List<Veiculo> veiculos = List.of();

        // When
        List<VeiculoResponseDTO> result = presenter.paraListaResponseDTO(veiculos);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve converter veiculo mesmo sem renavam")
    void shouldConvertVeiculoWithoutRenavam() {
        // Given
        veiculo.setRenavam(null);

        // When
        VeiculoResponseDTO result = presenter.paraResponseDTO(veiculo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.renavam()).isNull();
        assertThat(result.placa()).isEqualTo("ABC1D23");
    }

    @Test
    @DisplayName("Deve manter todos os campos ao converter")
    void shouldKeepAllFieldsWhenConverting() {
        // When
        VeiculoResponseDTO result = presenter.paraResponseDTO(veiculo);

        // Then
        assertThat(result.id()).isNotNull();
        assertThat(result.placa()).isNotNull();
        assertThat(result.marca()).isNotNull();
        assertThat(result.modelo()).isNotNull();
        assertThat(result.ano()).isNotNull();
    }
}


