package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.PecaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.Peca;
import br.com.grupo99.oficinaservice.domain.repository.PecaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para PecaApplicationService")
class PecaApplicationServiceTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private PecaRepository pecaRepository;

    @InjectMocks
    private PecaApplicationService pecaApplicationService;

    private PecaRequestDTO pecaRequestDTO;
    private Peca peca;
    private UUID pecaId;

    @BeforeEach
    void setUp() {
        pecaId = UUID.randomUUID();
        
        pecaRequestDTO = new PecaRequestDTO(
                "Filtro de Óleo",
                "Bosch",
                new BigDecimal("25.50"),
                10
        );

        peca = new Peca();
        peca.setId(pecaId);
        peca.setNome("Filtro de Óleo");
        peca.setFabricante("Bosch");
        peca.setPreco(new BigDecimal("25.50"));
        peca.setEstoque(10);
    }

    @Test
    @DisplayName("Deve criar uma nova peça com sucesso")
    void shouldCreatePecaSuccessfully() {
        // Given
        when(pecaRepository.save(any(Peca.class))).thenReturn(peca);

        // When
        PecaResponseDTO result = pecaApplicationService.create(pecaRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("Filtro de Óleo");
        assertThat(result.fabricante()).isEqualTo("Bosch");
        assertThat(result.preco()).isEqualTo(new BigDecimal("25.50"));
        assertThat(result.estoque()).isEqualTo(10);

        verify(pecaRepository).save(any(Peca.class));
    }

    @Test
    @DisplayName("Deve atualizar uma peça existente com sucesso")
    void shouldUpdatePecaSuccessfully() {
        // Given
        PecaRequestDTO updateRequest = new PecaRequestDTO(
                "Filtro de Ar Novo",
                "Mann",
                new BigDecimal("35.00"),
                15
        );

        Peca pecaAtualizada = new Peca();
        pecaAtualizada.setId(pecaId);
        pecaAtualizada.setNome("Filtro de Ar Novo");
        pecaAtualizada.setFabricante("Mann");
        pecaAtualizada.setPreco(new BigDecimal("35.00"));
        pecaAtualizada.setEstoque(15);

        when(pecaRepository.findById(pecaId)).thenReturn(Optional.of(peca));
        when(pecaRepository.save(any(Peca.class))).thenReturn(pecaAtualizada);

        // When
        PecaResponseDTO result = pecaApplicationService.update(pecaId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("Filtro de Ar Novo");
        assertThat(result.fabricante()).isEqualTo("Mann");
        assertThat(result.preco()).isEqualTo(new BigDecimal("35.00"));
        assertThat(result.estoque()).isEqualTo(15);

        verify(pecaRepository).findById(pecaId);
        verify(pecaRepository).save(any(Peca.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar peça inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentPeca() {
        // Given
        when(pecaRepository.findById(pecaId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pecaApplicationService.update(pecaId, pecaRequestDTO)
        );

        assertThat(exception.getMessage()).contains("Peça não encontrada com o id: " + pecaId);
        verify(pecaRepository).findById(pecaId);
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    @DisplayName("Deve buscar peça por ID com sucesso")
    void shouldGetPecaByIdSuccessfully() {
        // Given
        when(pecaRepository.findById(pecaId)).thenReturn(Optional.of(peca));

        // When
        PecaResponseDTO result = pecaApplicationService.getById(pecaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(pecaId);
        assertThat(result.nome()).isEqualTo("Filtro de Óleo");

        verify(pecaRepository).findById(pecaId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar peça inexistente por ID")
    void shouldThrowExceptionWhenGettingNonExistentPecaById() {
        // Given
        when(pecaRepository.findById(pecaId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pecaApplicationService.getById(pecaId)
        );

        assertThat(exception.getMessage()).contains("Peça não encontrada com o id: " + pecaId);
        verify(pecaRepository).findById(pecaId);
    }

    @Test
    @DisplayName("Deve listar todas as peças com sucesso")
    void shouldGetAllPecasSuccessfully() {
        // Given
        Peca peca2 = new Peca();
        peca2.setId(UUID.randomUUID());
        peca2.setNome("Pastilha de Freio");
        peca2.setFabricante("Brembo");
        peca2.setPreco(new BigDecimal("45.00"));
        peca2.setEstoque(5);

        List<Peca> pecas = Arrays.asList(peca, peca2);
        when(pecaRepository.findAll()).thenReturn(pecas);

        // When
        List<PecaResponseDTO> result = pecaApplicationService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).nome()).isEqualTo("Filtro de Óleo");
        assertThat(result.get(1).nome()).isEqualTo("Pastilha de Freio");

        verify(pecaRepository).findAll();
    }

    @Test
    @DisplayName("Deve deletar peça existente com sucesso")
    void shouldDeletePecaSuccessfully() {
        // Given
        when(pecaRepository.existsById(pecaId)).thenReturn(true);

        // When
        pecaApplicationService.delete(pecaId);

        // Then
        verify(pecaRepository).existsById(pecaId);
        verify(pecaRepository).deleteById(pecaId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar peça inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentPeca() {
        // Given
        when(pecaRepository.existsById(pecaId)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pecaApplicationService.delete(pecaId)
        );

        assertThat(exception.getMessage()).contains("Peça não encontrada com o id: " + pecaId);
        verify(pecaRepository).existsById(pecaId);
        verify(pecaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve adicionar estoque com sucesso")
    void shouldAddEstoqueSuccessfully() {
        // Given
        int quantidadeParaAdicionar = 5;
        
        Peca pecaComEstoqueAtualizado = new Peca();
        pecaComEstoqueAtualizado.setId(pecaId);
        pecaComEstoqueAtualizado.setNome("Filtro de Óleo");
        pecaComEstoqueAtualizado.setFabricante("Bosch");
        pecaComEstoqueAtualizado.setPreco(new BigDecimal("25.50"));
        pecaComEstoqueAtualizado.setEstoque(15); // 10 + 5

        when(pecaRepository.findById(pecaId)).thenReturn(Optional.of(peca));
        when(pecaRepository.save(any(Peca.class))).thenReturn(pecaComEstoqueAtualizado);

        // When
        PecaResponseDTO result = pecaApplicationService.adicionarEstoque(pecaId, quantidadeParaAdicionar);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.estoque()).isEqualTo(15);

        verify(pecaRepository).findById(pecaId);
        verify(pecaRepository).save(any(Peca.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar adicionar estoque em peça inexistente")
    void shouldThrowExceptionWhenAddingEstoqueToNonExistentPeca() {
        // Given
        when(pecaRepository.findById(pecaId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pecaApplicationService.adicionarEstoque(pecaId, 5)
        );

        assertThat(exception.getMessage()).contains("Peça não encontrada com o id: " + pecaId);
        verify(pecaRepository).findById(pecaId);
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    @DisplayName("Deve criar peça com fabricante null")
    void shouldCreatePecaWithNullFabricante() {
        // Given
        PecaRequestDTO requestWithNullFabricante = new PecaRequestDTO(
                "Peça Genérica",
                null,
                new BigDecimal("10.00"),
                5
        );

        Peca pecaComFabricanteNull = new Peca();
        pecaComFabricanteNull.setId(pecaId);
        pecaComFabricanteNull.setNome("Peça Genérica");
        pecaComFabricanteNull.setFabricante(null);
        pecaComFabricanteNull.setPreco(new BigDecimal("10.00"));
        pecaComFabricanteNull.setEstoque(5);

        when(pecaRepository.save(any(Peca.class))).thenReturn(pecaComFabricanteNull);

        // When
        PecaResponseDTO result = pecaApplicationService.create(requestWithNullFabricante);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("Peça Genérica");
        assertThat(result.fabricante()).isNull();
        assertThat(result.preco()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.estoque()).isEqualTo(5);

        verify(pecaRepository).save(any(Peca.class));
    }
}