package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.ClienteController;
import br.com.grupo99.oficinaservice.application.dto.ClienteRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.BusinessException;
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
@DisplayName("Testes unitários para ClienteRestController")
class ClienteControllerUnitTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private ClienteController clienteController;

    @InjectMocks
    private ClienteRestController clienteRestController;

    private ClienteRequestDTO clienteRequestDTO;
    private ClienteResponseDTO clienteResponseDTO;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        
        clienteRequestDTO = new ClienteRequestDTO(
                "João Silva",
                "111.444.777-35",
                "11999887766",
                "joao@email.com"
        );

        clienteResponseDTO = new ClienteResponseDTO(
                clienteId,
                "João Silva",
                "111.444.777-35",
                "11999887766",
                "joao@email.com"
        );
    }

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void shouldCreateClienteSuccessfully() {
        // Given
        when(clienteController.criar(any(ClienteRequestDTO.class)))
                .thenReturn(clienteResponseDTO);

        // When
        ResponseEntity<ClienteResponseDTO> response = clienteRestController.create(clienteRequestDTO);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(clienteId);
        assertThat(response.getBody().nome()).isEqualTo("João Silva");
        assertThat(response.getBody().cpfCnpj()).isEqualTo("111.444.777-35");
        assertThat(response.getBody().telefone()).isEqualTo("11999887766");
        assertThat(response.getBody().email()).isEqualTo("joao@email.com");
    }

    @Test
    @DisplayName("Deve propagar BusinessException do controller")
    void shouldPropagateBusinessExceptionFromController() {
        // Given
        when(clienteController.criar(any(ClienteRequestDTO.class)))
                .thenThrow(new BusinessException("Já existe um cliente com este CPF/CNPJ."));

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteRestController.create(clienteRequestDTO)
        );

        assertThat(exception.getMessage()).isEqualTo("Já existe um cliente com este CPF/CNPJ.");
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void shouldGetClienteByIdSuccessfully() {
        // Given
        when(clienteController.buscarPorId(clienteId)).thenReturn(clienteResponseDTO);

        // When
        ResponseEntity<ClienteResponseDTO> response = clienteRestController.getById(clienteId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(clienteId);
        assertThat(response.getBody().nome()).isEqualTo("João Silva");
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException do controller")
    void shouldPropagateResourceNotFoundExceptionFromController() {
        // Given
        when(clienteController.buscarPorId(clienteId))
                .thenThrow(new ResourceNotFoundException("Cliente não encontrado"));

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clienteRestController.getById(clienteId)
        );

        assertThat(exception.getMessage()).isEqualTo("Cliente não encontrado");
    }

    @Test
    @DisplayName("Deve listar todos os clientes com sucesso")
    void shouldGetAllClientesSuccessfully() {
        // Given
        ClienteResponseDTO cliente2 = new ClienteResponseDTO(
                UUID.randomUUID(),
                "Maria Santos",
                "111.222.333-96",
                "11777665544",
                "maria@email.com"
        );
        
        List<ClienteResponseDTO> clientes = Arrays.asList(clienteResponseDTO, cliente2);
        when(clienteController.listarTodos()).thenReturn(clientes);

        // When
        ResponseEntity<List<ClienteResponseDTO>> response = clienteRestController.getAll();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).nome()).isEqualTo("João Silva");
        assertThat(response.getBody().get(1).nome()).isEqualTo("Maria Santos");
    }

    @Test
    @DisplayName("Deve atualizar cliente com sucesso")
    void shouldUpdateClienteSuccessfully() {
        // Given
        ClienteRequestDTO updateRequest = new ClienteRequestDTO(
                "João Silva Santos",
                "111.444.777-35",
                "11888776655",
                "joao.santos@email.com"
        );

        ClienteResponseDTO updatedResponse = new ClienteResponseDTO(
                clienteId,
                "João Silva Santos",
                "111.444.777-35",
                "11888776655",
                "joao.santos@email.com"
        );

        when(clienteController.atualizar(eq(clienteId), any(ClienteRequestDTO.class)))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<ClienteResponseDTO> response = clienteRestController.update(clienteId, updateRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(clienteId);
        assertThat(response.getBody().nome()).isEqualTo("João Silva Santos");
        assertThat(response.getBody().telefone()).isEqualTo("11888776655");
        assertThat(response.getBody().email()).isEqualTo("joao.santos@email.com");
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException ao atualizar cliente inexistente")
    void shouldPropagateResourceNotFoundExceptionWhenUpdatingNonExistentCliente() {
        // Given
        when(clienteController.atualizar(eq(clienteId), any(ClienteRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Cliente não encontrado"));

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clienteRestController.update(clienteId, clienteRequestDTO)
        );

        assertThat(exception.getMessage()).isEqualTo("Cliente não encontrado");
    }

    @Test
    @DisplayName("Deve deletar cliente com sucesso")
    void shouldDeleteClienteSuccessfully() {
        // When
        clienteRestController.delete(clienteId);

        // Then - método void, se não lançar exceção está ok
        // Verifica se não houve exceção
    }

    @Test
    @DisplayName("Deve propagar ResourceNotFoundException ao deletar cliente inexistente")
    void shouldPropagateResourceNotFoundExceptionWhenDeletingNonExistentCliente() {
        // Given
        doThrow(new ResourceNotFoundException("Cliente não encontrado"))
                .when(clienteController).deletar(clienteId);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clienteRestController.delete(clienteId)
        );

        assertThat(exception.getMessage()).isEqualTo("Cliente não encontrado");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há clientes")
    void shouldReturnEmptyListWhenNoClientes() {
        // Given
        when(clienteController.listarTodos()).thenReturn(Arrays.asList());

        // When
        ResponseEntity<List<ClienteResponseDTO>> response = clienteRestController.getAll();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    @DisplayName("Deve chamar controller corretamente no método create")
    void shouldCallControllerCorrectlyInCreateMethod() {
        // Given
        when(clienteController.criar(clienteRequestDTO))
                .thenReturn(clienteResponseDTO);

        // When
        clienteRestController.create(clienteRequestDTO);

        // Then
        // Verifica se o controller foi chamado com os parâmetros corretos
        // (isso é verificado implicitamente pelo mock quando chamamos o método)
    }
}