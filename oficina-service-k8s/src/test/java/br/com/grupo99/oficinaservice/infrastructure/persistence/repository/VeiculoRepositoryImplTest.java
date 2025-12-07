package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.VeiculoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("VeiculoRepositoryImpl Tests")
class VeiculoRepositoryImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private VeiculoJpaRepository jpaRepository;

    @InjectMocks
    private VeiculoRepositoryImpl veiculoRepository;

    private Veiculo veiculo;
    private UUID veiculoId;

    @BeforeEach
    void setUp() {
        veiculoId = UUID.randomUUID();
        veiculo = new Veiculo("ABC-1234", "Toyota", "Corolla", 2020);
        veiculo.setId(veiculoId);
        veiculo.setRenavam("12345678901");
    }

    @Test
    @DisplayName("Deve salvar veículo com sucesso")
    void shouldSaveVeiculoSuccessfully() {
        // Given
        when(jpaRepository.save(any(Veiculo.class))).thenReturn(veiculo);

        // When
        Veiculo result = veiculoRepository.save(veiculo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(veiculoId);
        assertThat(result.getPlaca()).isEqualTo("ABC-1234");
        assertThat(result.getMarca()).isEqualTo("Toyota");
        assertThat(result.getModelo()).isEqualTo("Corolla");
        assertThat(result.getAno()).isEqualTo(2020);
        
        verify(jpaRepository).save(veiculo);
    }

    @Test
    @DisplayName("Deve buscar veículo por ID com sucesso")
    void shouldFindVeiculoByIdSuccessfully() {
        // Given
        when(jpaRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));

        // When
        Optional<Veiculo> result = veiculoRepository.findById(veiculoId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(veiculoId);
        assertThat(result.get().getPlaca()).isEqualTo("ABC-1234");
        
        verify(jpaRepository).findById(veiculoId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando veículo não encontrado por ID")
    void shouldReturnEmptyOptionalWhenVeiculoNotFoundById() {
        // Given
        when(jpaRepository.findById(veiculoId)).thenReturn(Optional.empty());

        // When
        Optional<Veiculo> result = veiculoRepository.findById(veiculoId);

        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findById(veiculoId);
    }

    @Test
    @DisplayName("Deve buscar veículo por placa com sucesso")
    void shouldFindVeiculoByPlacaSuccessfully() {
        // Given
        String placa = "ABC-1234";
        when(jpaRepository.findByPlaca(placa)).thenReturn(Optional.of(veiculo));

        // When
        Optional<Veiculo> result = veiculoRepository.findByPlaca(placa);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getPlaca()).isEqualTo(placa);
        
        verify(jpaRepository).findByPlaca(placa);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando veículo não encontrado por placa")
    void shouldReturnEmptyOptionalWhenVeiculoNotFoundByPlaca() {
        // Given
        String placa = "XYZ-9876";
        when(jpaRepository.findByPlaca(placa)).thenReturn(Optional.empty());

        // When
        Optional<Veiculo> result = veiculoRepository.findByPlaca(placa);

        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findByPlaca(placa);
    }

    @Test
    @DisplayName("Deve deletar veículo por ID")
    void shouldDeleteVeiculoById() {
        // Given
        doNothing().when(jpaRepository).deleteById(veiculoId);

        // When
        veiculoRepository.deleteById(veiculoId);

        // Then
        verify(jpaRepository).deleteById(veiculoId);
    }

    @Test
    @DisplayName("Deve listar todos os veículos")
    void shouldFindAllVeiculos() {
        // Given
        Veiculo veiculo2 = new Veiculo("XYZ-9876", "Honda", "Civic", 2021);
        List<Veiculo> veiculos = Arrays.asList(veiculo, veiculo2);
        when(jpaRepository.findAll()).thenReturn(veiculos);

        // When
        List<Veiculo> result = veiculoRepository.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(veiculo, veiculo2);
        
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há veículos")
    void shouldReturnEmptyListWhenNoVeiculos() {
        // Given
        when(jpaRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Veiculo> result = veiculoRepository.findAll();

        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Deve verificar se veículo existe por ID")
    void shouldCheckIfVeiculoExistsById() {
        // Given
        when(jpaRepository.existsById(veiculoId)).thenReturn(true);

        // When
        boolean result = veiculoRepository.existsById(veiculoId);

        // Then
        assertThat(result).isTrue();
        
        verify(jpaRepository).existsById(veiculoId);
    }

    @Test
    @DisplayName("Deve retornar false quando veículo não existe por ID")
    void shouldReturnFalseWhenVeiculoNotExistsById() {
        // Given
        when(jpaRepository.existsById(veiculoId)).thenReturn(false);

        // When
        boolean result = veiculoRepository.existsById(veiculoId);

        // Then
        assertThat(result).isFalse();
        
        verify(jpaRepository).existsById(veiculoId);
    }

    @Test
    @DisplayName("Deve verificar se veículo existe por Renavam")
    void shouldCheckIfVeiculoExistsByRenavam() {
        // Given
        String renavam = "12345678901";
        when(jpaRepository.existsByRenavam(renavam)).thenReturn(true);

        // When
        boolean result = veiculoRepository.existsByRenavam(renavam);

        // Then
        assertThat(result).isTrue();
        
        verify(jpaRepository).existsByRenavam(renavam);
    }

    @Test
    @DisplayName("Deve retornar false quando veículo não existe por Renavam")
    void shouldReturnFalseWhenVeiculoNotExistsByRenavam() {
        // Given
        String renavam = "98765432109";
        when(jpaRepository.existsByRenavam(renavam)).thenReturn(false);

        // When
        boolean result = veiculoRepository.existsByRenavam(renavam);

        // Then
        assertThat(result).isFalse();
        
        verify(jpaRepository).existsByRenavam(renavam);
    }
}