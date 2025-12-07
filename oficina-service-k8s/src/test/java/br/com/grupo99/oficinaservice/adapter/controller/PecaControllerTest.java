package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.PecaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarPecaUseCase;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para PecaController (Adapter)")
class PecaControllerTest {

    @Mock
    private GerenciarPecaUseCase gerenciarPecaUseCase;

    @InjectMocks
    private PecaController pecaController;

    private PecaRequestDTO pecaRequestDTO;
    private PecaResponseDTO pecaResponseDTO;
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

        pecaResponseDTO = new PecaResponseDTO(
                pecaId,
                "Filtro de Óleo",
                "Bosch",
                new BigDecimal("25.50"),
                10
        );
    }

    @Test
    @DisplayName("Deve criar peça delegando para o UseCase")
    void shouldCreatePecaDelegatingToUseCase() {
        // Given
        when(gerenciarPecaUseCase.create(any(PecaRequestDTO.class)))
                .thenReturn(pecaResponseDTO);

        // When
        PecaResponseDTO result = pecaController.criar(pecaRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(pecaId);
        assertThat(result.nome()).isEqualTo("Filtro de Óleo");
        verify(gerenciarPecaUseCase).create(pecaRequestDTO);
    }

    @Test
    @DisplayName("Deve buscar peça por ID delegando para o UseCase")
    void shouldGetPecaByIdDelegatingToUseCase() {
        // Given
        when(gerenciarPecaUseCase.getById(pecaId)).thenReturn(pecaResponseDTO);

        // When
        PecaResponseDTO result = pecaController.buscarPorId(pecaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(pecaId);
        verify(gerenciarPecaUseCase).getById(pecaId);
    }

    @Test
    @DisplayName("Deve listar todas as peças delegando para o UseCase")
    void shouldListAllPecasDelegatingToUseCase() {
        // Given
        PecaResponseDTO peca2 = new PecaResponseDTO(
                UUID.randomUUID(),
                "Pastilha de Freio",
                "Brembo",
                new BigDecimal("45.00"),
                5
        );
        List<PecaResponseDTO> pecas = Arrays.asList(pecaResponseDTO, peca2);
        when(gerenciarPecaUseCase.getAll()).thenReturn(pecas);

        // When
        List<PecaResponseDTO> result = pecaController.listarTodos();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).nome()).isEqualTo("Filtro de Óleo");
        assertThat(result.get(1).nome()).isEqualTo("Pastilha de Freio");
        verify(gerenciarPecaUseCase).getAll();
    }

    @Test
    @DisplayName("Deve atualizar peça delegando para o UseCase")
    void shouldUpdatePecaDelegatingToUseCase() {
        // Given
        PecaResponseDTO updatedResponse = new PecaResponseDTO(
                pecaId,
                "Filtro de Ar",
                "Mann",
                new BigDecimal("35.00"),
                15
        );
        when(gerenciarPecaUseCase.update(eq(pecaId), any(PecaRequestDTO.class)))
                .thenReturn(updatedResponse);

        // When
        PecaResponseDTO result = pecaController.atualizar(pecaId, pecaRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("Filtro de Ar");
        verify(gerenciarPecaUseCase).update(pecaId, pecaRequestDTO);
    }

    @Test
    @DisplayName("Deve deletar peça delegando para o UseCase")
    void shouldDeletePecaDelegatingToUseCase() {
        // Given
        doNothing().when(gerenciarPecaUseCase).delete(pecaId);

        // When
        pecaController.deletar(pecaId);

        // Then
        verify(gerenciarPecaUseCase).delete(pecaId);
    }

    @Test
    @DisplayName("Deve adicionar estoque delegando para o UseCase")
    void shouldAddEstoqueDelegatingToUseCase() {
        // Given
        PecaResponseDTO updatedResponse = new PecaResponseDTO(
                pecaId,
                "Filtro de Óleo",
                "Bosch",
                new BigDecimal("25.50"),
                15
        );
        when(gerenciarPecaUseCase.adicionarEstoque(pecaId, 5))
                .thenReturn(updatedResponse);

        // When
        PecaResponseDTO result = pecaController.adicionarEstoque(pecaId, 5);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.estoque()).isEqualTo(15);
        verify(gerenciarPecaUseCase).adicionarEstoque(pecaId, 5);
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException do UseCase")
    void shouldPropagateResourceNotFoundException() {
        // Given
        when(gerenciarPecaUseCase.getById(pecaId))
                .thenThrow(new ResourceNotFoundException("Peça não encontrada"));

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                () -> pecaController.buscarPorId(pecaId));
        verify(gerenciarPecaUseCase).getById(pecaId);
    }
}
