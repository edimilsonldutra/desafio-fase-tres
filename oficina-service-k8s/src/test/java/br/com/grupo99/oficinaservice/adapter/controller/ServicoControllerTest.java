package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.ServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarServicoUseCase;
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
@DisplayName("Testes unitários para ServicoController (Adapter)")
class ServicoControllerTest {

    @Mock
    private GerenciarServicoUseCase gerenciarServicoUseCase;

    @InjectMocks
    private ServicoController servicoController;

    private ServicoRequestDTO servicoRequestDTO;
    private ServicoResponseDTO servicoResponseDTO;
    private UUID servicoId;

    @BeforeEach
    void setUp() {
        servicoId = UUID.randomUUID();
        servicoRequestDTO = new ServicoRequestDTO("Troca de óleo", BigDecimal.valueOf(50.00));
        servicoResponseDTO = new ServicoResponseDTO(servicoId, "Troca de óleo", BigDecimal.valueOf(50.00));
    }

    @Test
    @DisplayName("Deve criar serviço delegando para o UseCase")
    void shouldCreateServicoDelegatingToUseCase() {
        // Given
        when(gerenciarServicoUseCase.create(any(ServicoRequestDTO.class)))
                .thenReturn(servicoResponseDTO);

        // When
        ServicoResponseDTO result = servicoController.criar(servicoRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(servicoId);
        assertThat(result.descricao()).isEqualTo("Troca de óleo");
        verify(gerenciarServicoUseCase).create(servicoRequestDTO);
    }

    @Test
    @DisplayName("Deve buscar serviço por ID delegando para o UseCase")
    void shouldGetServicoByIdDelegatingToUseCase() {
        // Given
        when(gerenciarServicoUseCase.getById(servicoId)).thenReturn(servicoResponseDTO);

        // When
        ServicoResponseDTO result = servicoController.buscarPorId(servicoId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(servicoId);
        verify(gerenciarServicoUseCase).getById(servicoId);
    }

    @Test
    @DisplayName("Deve listar todos os serviços delegando para o UseCase")
    void shouldListAllServicosDelegatingToUseCase() {
        // Given
        ServicoResponseDTO servico2 = new ServicoResponseDTO(
                UUID.randomUUID(),
                "Alinhamento",
                BigDecimal.valueOf(80.00)
        );
        List<ServicoResponseDTO> servicos = Arrays.asList(servicoResponseDTO, servico2);
        when(gerenciarServicoUseCase.getAll()).thenReturn(servicos);

        // When
        List<ServicoResponseDTO> result = servicoController.listarTodos();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).descricao()).isEqualTo("Troca de óleo");
        assertThat(result.get(1).descricao()).isEqualTo("Alinhamento");
        verify(gerenciarServicoUseCase).getAll();
    }

    @Test
    @DisplayName("Deve atualizar serviço delegando para o UseCase")
    void shouldUpdateServicoDelegatingToUseCase() {
        // Given
        ServicoResponseDTO updatedResponse = new ServicoResponseDTO(
                servicoId,
                "Troca de óleo premium",
                BigDecimal.valueOf(75.00)
        );
        when(gerenciarServicoUseCase.update(eq(servicoId), any(ServicoRequestDTO.class)))
                .thenReturn(updatedResponse);

        // When
        ServicoResponseDTO result = servicoController.atualizar(servicoId, servicoRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.descricao()).isEqualTo("Troca de óleo premium");
        verify(gerenciarServicoUseCase).update(servicoId, servicoRequestDTO);
    }

    @Test
    @DisplayName("Deve deletar serviço delegando para o UseCase")
    void shouldDeleteServicoDelegatingToUseCase() {
        // Given
        doNothing().when(gerenciarServicoUseCase).delete(servicoId);

        // When
        servicoController.deletar(servicoId);

        // Then
        verify(gerenciarServicoUseCase).delete(servicoId);
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException do UseCase")
    void shouldPropagateResourceNotFoundException() {
        // Given
        when(gerenciarServicoUseCase.getById(servicoId))
                .thenThrow(new ResourceNotFoundException("Serviço não encontrado"));

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> servicoController.buscarPorId(servicoId));
        verify(gerenciarServicoUseCase).getById(servicoId);
    }
}
