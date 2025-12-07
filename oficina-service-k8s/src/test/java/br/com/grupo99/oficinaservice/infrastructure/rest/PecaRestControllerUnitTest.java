package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.PecaController;
import br.com.grupo99.oficinaservice.application.dto.PecaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaUpdateEstoqueRequestDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para PecaRestController")
class PecaRestControllerUnitTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private PecaController pecaController;

    @InjectMocks
    private PecaRestController pecaRestController;

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
    @DisplayName("Deve criar peça com sucesso")
    void shouldCreatePecaSuccessfully() {
        // Given
        when(pecaController.criar(any(PecaRequestDTO.class)))
                .thenReturn(pecaResponseDTO);

        // When
        ResponseEntity<PecaResponseDTO> response = pecaRestController.create(pecaRequestDTO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(pecaId);
        assertThat(response.getBody().nome()).isEqualTo("Filtro de Óleo");
        assertThat(response.getBody().fabricante()).isEqualTo("Bosch");
        assertThat(response.getBody().preco()).isEqualTo(new BigDecimal("25.50"));
        assertThat(response.getBody().estoque()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve buscar peça por ID com sucesso")
    void shouldGetPecaByIdSuccessfully() {
        // Given
        when(pecaController.buscarPorId(pecaId)).thenReturn(pecaResponseDTO);

        // When
        ResponseEntity<PecaResponseDTO> response = pecaRestController.getById(pecaId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(pecaId);
        assertThat(response.getBody().nome()).isEqualTo("Filtro de Óleo");
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException do use case")
    void shouldPropagateResourceNotFoundExceptionFromUseCase() {
        // Given
        when(pecaController.buscarPorId(pecaId))
                .thenThrow(new ResourceNotFoundException("Peça não encontrada"));

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pecaRestController.getById(pecaId)
        );

        assertThat(exception.getMessage()).isEqualTo("Peça não encontrada");
    }

    @Test
    @DisplayName("Deve listar todas as peças com sucesso")
    void shouldGetAllPecasSuccessfully() {
        // Given
        PecaResponseDTO peca2 = new PecaResponseDTO(
                UUID.randomUUID(),
                "Pastilha de Freio",
                "Brembo",
                new BigDecimal("45.00"),
                5
        );
        
        List<PecaResponseDTO> pecas = Arrays.asList(pecaResponseDTO, peca2);
        when(pecaController.listarTodos()).thenReturn(pecas);

        // When
        ResponseEntity<List<PecaResponseDTO>> response = pecaRestController.getAll();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).nome()).isEqualTo("Filtro de Óleo");
        assertThat(response.getBody().get(1).nome()).isEqualTo("Pastilha de Freio");
    }

    @Test
    @DisplayName("Deve atualizar peça com sucesso")
    void shouldUpdatePecaSuccessfully() {
        // Given
        PecaRequestDTO updateRequest = new PecaRequestDTO(
                "Filtro de Ar",
                "Mann",
                new BigDecimal("35.00"),
                15
        );

        PecaResponseDTO updatedResponse = new PecaResponseDTO(
                pecaId,
                "Filtro de Ar",
                "Mann",
                new BigDecimal("35.00"),
                15
        );

        when(pecaController.atualizar(eq(pecaId), any(PecaRequestDTO.class)))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<PecaResponseDTO> response = pecaRestController.update(pecaId, updateRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(pecaId);
        assertThat(response.getBody().nome()).isEqualTo("Filtro de Ar");
        assertThat(response.getBody().fabricante()).isEqualTo("Mann");
        assertThat(response.getBody().preco()).isEqualTo(new BigDecimal("35.00"));
        assertThat(response.getBody().estoque()).isEqualTo(15);
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException ao atualizar peça inexistente")
    void shouldPropagateResourceNotFoundExceptionWhenUpdatingNonExistentPeca() {
        // Given
        when(pecaController.atualizar(eq(pecaId), any(PecaRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Peça não encontrada"));

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pecaRestController.update(pecaId, pecaRequestDTO)
        );

        assertThat(exception.getMessage()).isEqualTo("Peça não encontrada");
    }

    @Test
    @DisplayName("Deve deletar peça com sucesso")
    void shouldDeletePecaSuccessfully() {
        // When
        pecaRestController.delete(pecaId);

        // Then - método void, se não lançar exceção está ok
        // Verifica se não houve exceção
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException ao deletar peça inexistente")
    void shouldPropagateResourceNotFoundExceptionWhenDeletingNonExistentPeca() {
        // Given
        doThrow(new ResourceNotFoundException("Peça não encontrada"))
                .when(pecaController).deletar(pecaId);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pecaRestController.delete(pecaId)
        );

        assertThat(exception.getMessage()).isEqualTo("Peça não encontrada");
    }

    @Test
    @DisplayName("Deve atualizar estoque com sucesso")
    void shouldUpdateEstoqueSuccessfully() {
        // Given
        PecaUpdateEstoqueRequestDTO estoqueRequest = new PecaUpdateEstoqueRequestDTO(5);
        
        PecaResponseDTO updatedResponse = new PecaResponseDTO(
                pecaId,
                "Filtro de Óleo",
                "Bosch",
                new BigDecimal("25.50"),
                15 // 10 + 5
        );

        when(pecaController.adicionarEstoque(eq(pecaId), eq(5)))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<PecaResponseDTO> response = pecaRestController.updateEstoque(pecaId, estoqueRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(pecaId);
        assertThat(response.getBody().estoque()).isEqualTo(15);
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException ao atualizar estoque de peça inexistente")
    void shouldPropagateResourceNotFoundExceptionWhenUpdatingEstoqueOfNonExistentPeca() {
        // Given
        PecaUpdateEstoqueRequestDTO estoqueRequest = new PecaUpdateEstoqueRequestDTO(5);
        
        when(pecaController.adicionarEstoque(eq(pecaId), eq(5)))
                .thenThrow(new ResourceNotFoundException("Peça não encontrada"));

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pecaRestController.updateEstoque(pecaId, estoqueRequest)
        );

        assertThat(exception.getMessage()).isEqualTo("Peça não encontrada");
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

        PecaResponseDTO responseWithNullFabricante = new PecaResponseDTO(
                pecaId,
                "Peça Genérica",
                null,
                new BigDecimal("10.00"),
                5
        );

        when(pecaController.criar(any(PecaRequestDTO.class)))
                .thenReturn(responseWithNullFabricante);

        // When
        ResponseEntity<PecaResponseDTO> response = pecaRestController.create(requestWithNullFabricante);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().nome()).isEqualTo("Peça Genérica");
        assertThat(response.getBody().fabricante()).isNull();
        assertThat(response.getBody().preco()).isEqualTo(new BigDecimal("10.00"));
        assertThat(response.getBody().estoque()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há peças")
    void shouldReturnEmptyListWhenNoPecas() {
        // Given
        when(pecaController.listarTodos()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<PecaResponseDTO>> response = pecaRestController.getAll();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Deve aceitar estoque zero na criação")
    void shouldAcceptZeroStockOnCreation() {
        // Given
        PecaRequestDTO requestWithZeroStock = new PecaRequestDTO(
                "Peça sem Estoque",
                "Fabricante",
                new BigDecimal("50.00"),
                0
        );

        PecaResponseDTO responseWithZeroStock = new PecaResponseDTO(
                pecaId,
                "Peça sem Estoque",
                "Fabricante",
                new BigDecimal("50.00"),
                0
        );

        when(pecaController.criar(any(PecaRequestDTO.class)))
                .thenReturn(responseWithZeroStock);

        // When
        ResponseEntity<PecaResponseDTO> response = pecaRestController.create(requestWithZeroStock);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().estoque()).isEqualTo(0);
    }
}