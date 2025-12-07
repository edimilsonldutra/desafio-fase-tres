package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.ServicoController;
import br.com.grupo99.oficinaservice.application.dto.ServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para ServicoRestController")
class ServicoRestControllerUnitTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private ServicoController servicoController;

    @InjectMocks
    private ServicoRestController servicoRestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID servicoId;
    private ServicoRequestDTO servicoRequestDTO;
    private ServicoResponseDTO servicoResponseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(servicoRestController)
                .setControllerAdvice(new br.com.grupo99.oficinaservice.infrastructure.rest.handler.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        servicoId = UUID.randomUUID();
        servicoRequestDTO = new ServicoRequestDTO("Troca de óleo", BigDecimal.valueOf(50.00));
        servicoResponseDTO = new ServicoResponseDTO(servicoId, "Troca de óleo", BigDecimal.valueOf(50.00));
    }

    @Test
    @DisplayName("Deve criar novo serviço com sucesso")
    void shouldCreateServicoSuccessfully() throws Exception {
        // Given
        when(servicoController.criar(any(ServicoRequestDTO.class)))
                .thenReturn(servicoResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(servicoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(servicoId.toString()))
                .andExpect(jsonPath("$.descricao").value("Troca de óleo"))
                .andExpect(jsonPath("$.preco").value(50.00));

        verify(servicoController).criar(any(ServicoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void shouldGetServicoByIdSuccessfully() throws Exception {
        // Given
        when(servicoController.buscarPorId(servicoId)).thenReturn(servicoResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/servicos/{id}", servicoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(servicoId.toString()))
                .andExpect(jsonPath("$.descricao").value("Troca de óleo"))
                .andExpect(jsonPath("$.preco").value(50.00));

        verify(servicoController).buscarPorId(servicoId);
    }

    @Test
    @DisplayName("Deve retornar 404 quando serviço não encontrado")
    void shouldReturn404WhenServicoNotFound() throws Exception {
        // Given
        when(servicoController.buscarPorId(servicoId))
                .thenThrow(new ResourceNotFoundException("Serviço não encontrado"));

        // When & Then
        mockMvc.perform(get("/api/v1/servicos/{id}", servicoId))
                .andExpect(status().isNotFound());

        verify(servicoController).buscarPorId(servicoId);
    }

    @Test
    @DisplayName("Deve listar todos os serviços")
    void shouldListAllServicos() throws Exception {
        // Given
        ServicoResponseDTO servico2 = new ServicoResponseDTO(
                UUID.randomUUID(), 
                "Alinhamento", 
                BigDecimal.valueOf(80.00)
        );
        List<ServicoResponseDTO> servicos = Arrays.asList(servicoResponseDTO, servico2);
        when(servicoController.listarTodos()).thenReturn(servicos);

        // When & Then
        mockMvc.perform(get("/api/v1/servicos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(servicoId.toString()))
                .andExpect(jsonPath("$[0].descricao").value("Troca de óleo"))
                .andExpect(jsonPath("$[1].descricao").value("Alinhamento"));

        verify(servicoController).listarTodos();
    }

    @Test
    @DisplayName("Deve atualizar serviço existente")
    void shouldUpdateServicoSuccessfully() throws Exception {
        // Given
        ServicoRequestDTO updateRequest = new ServicoRequestDTO("Troca de óleo premium", BigDecimal.valueOf(75.00));
        ServicoResponseDTO updatedResponse = new ServicoResponseDTO(
                servicoId, 
                "Troca de óleo premium", 
                BigDecimal.valueOf(75.00)
        );

        when(servicoController.atualizar(eq(servicoId), any(ServicoRequestDTO.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/servicos/{id}", servicoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(servicoId.toString()))
                .andExpect(jsonPath("$.descricao").value("Troca de óleo premium"))
                .andExpect(jsonPath("$.preco").value(75.00));

        verify(servicoController).atualizar(eq(servicoId), any(ServicoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve deletar serviço com sucesso")
    void shouldDeleteServicoSuccessfully() throws Exception {
        // Given
        doNothing().when(servicoController).deletar(servicoId);

        // When & Then
        mockMvc.perform(delete("/api/v1/servicos/{id}", servicoId))
                .andExpect(status().isNoContent());

        verify(servicoController).deletar(servicoId);
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar deletar serviço inexistente")
    void shouldReturn404WhenDeletingNonExistentServico() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Serviço não encontrado"))
                .when(servicoController).deletar(servicoId);

        // When & Then
        mockMvc.perform(delete("/api/v1/servicos/{id}", servicoId))
                .andExpect(status().isNotFound());

        verify(servicoController).deletar(servicoId);
    }

    @Test
    @DisplayName("Deve retornar erro de validação para dados inválidos")
    void shouldReturnValidationErrorForInvalidData() throws Exception {
        // Given
        ServicoRequestDTO invalidRequest = new ServicoRequestDTO("", null);

        // When & Then
        mockMvc.perform(post("/api/v1/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(servicoController, never()).criar(any());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há serviços")
    void shouldReturnEmptyListWhenNoServicos() throws Exception {
        // Given
        when(servicoController.listarTodos()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(servicoController).listarTodos();
    }

    @Test
    @DisplayName("Deve chamar use case com parâmetros corretos na criação")
    void shouldCallUseCaseWithCorrectParametersOnCreate() throws Exception {
        // Given
        when(servicoController.criar(any(ServicoRequestDTO.class)))
                .thenReturn(servicoResponseDTO);

        // When
        mockMvc.perform(post("/api/v1/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(servicoRequestDTO)))
                .andExpect(status().isCreated());

        // Then
        verify(servicoController).criar(
                argThat(request -> 
                        "Troca de óleo".equals(request.descricao()) && 
                        BigDecimal.valueOf(50.00).equals(request.preco())
                )
        );
    }

    @Test
    @DisplayName("Deve chamar use case com parâmetros corretos na atualização")
    void shouldCallUseCaseWithCorrectParametersOnUpdate() throws Exception {
        // Given
        ServicoRequestDTO updateRequest = new ServicoRequestDTO("Alinhamento completo", BigDecimal.valueOf(120.00));
        ServicoResponseDTO updatedResponse = new ServicoResponseDTO(
                servicoId, 
                "Alinhamento completo", 
                BigDecimal.valueOf(120.00)
        );

        when(servicoController.atualizar(eq(servicoId), any(ServicoRequestDTO.class)))
                .thenReturn(updatedResponse);

        // When
        mockMvc.perform(put("/api/v1/servicos/{id}", servicoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // Then
        verify(servicoController).atualizar(
                eq(servicoId),
                argThat(request -> 
                        "Alinhamento completo".equals(request.descricao()) && 
                        BigDecimal.valueOf(120.00).equals(request.preco())
                )
        );
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar serviço inexistente")
    void shouldReturn404WhenUpdatingNonExistentServico() throws Exception {
        // Given
        ServicoRequestDTO updateRequest = new ServicoRequestDTO("Serviço atualizado", BigDecimal.valueOf(100.00));

        when(servicoController.atualizar(eq(servicoId), any(ServicoRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Serviço não encontrado"));

        // When & Then
        mockMvc.perform(put("/api/v1/servicos/{id}", servicoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(servicoController).atualizar(eq(servicoId), any(ServicoRequestDTO.class));
    }
}


