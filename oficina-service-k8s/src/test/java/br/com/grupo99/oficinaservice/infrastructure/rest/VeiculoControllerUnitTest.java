package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.VeiculoController;
import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
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
@DisplayName("Testes unitários para VeiculoRestController")
class VeiculoControllerUnitTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private VeiculoController veiculoController;

    @InjectMocks
    private VeiculoRestController veiculoRestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID veiculoId;
    private VeiculoRequestDTO veiculoRequestDTO;
    private VeiculoResponseDTO veiculoResponseDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(veiculoRestController)
                .setControllerAdvice(new br.com.grupo99.oficinaservice.infrastructure.rest.handler.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        veiculoId = UUID.randomUUID();
        veiculoRequestDTO = new VeiculoRequestDTO(
                "ABC-1234",
                "12345678901",
                "Toyota",
                "Corolla",
                2020,
                UUID.randomUUID()
        );
        veiculoResponseDTO = new VeiculoResponseDTO(
                veiculoId,
                "ABC-1234",
                "12345678901",
                "Toyota",
                "Corolla",
                2020
        );
    }

    @Test
    @DisplayName("Deve criar novo veículo com sucesso")
    void shouldCreateVeiculoSuccessfully() throws Exception {
        // Given
        when(veiculoController.criar(any(VeiculoRequestDTO.class)))
                .thenReturn(veiculoResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(veiculoId.toString()))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.modelo").value("Corolla"))
                .andExpect(jsonPath("$.placa").value("ABC-1234"))
                .andExpect(jsonPath("$.renavam").value("12345678901"))
                .andExpect(jsonPath("$.ano").value(2020));

        verify(veiculoController).criar(any(VeiculoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve buscar veículo por ID com sucesso")
    void shouldGetVeiculoByIdSuccessfully() throws Exception {
        // Given
        when(veiculoController.buscarPorId(veiculoId)).thenReturn(veiculoResponseDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/veiculos/{id}", veiculoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(veiculoId.toString()))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.modelo").value("Corolla"))
                .andExpect(jsonPath("$.placa").value("ABC-1234"));

        verify(veiculoController).buscarPorId(veiculoId);
    }

    @Test
    @DisplayName("Deve retornar 404 quando veículo não encontrado")
    void shouldReturn404WhenVeiculoNotFound() throws Exception {
        // Given
        when(veiculoController.buscarPorId(veiculoId))
                .thenThrow(new ResourceNotFoundException("Veículo não encontrado"));

        // When & Then
        mockMvc.perform(get("/api/v1/veiculos/{id}", veiculoId))
                .andExpect(status().isNotFound());

        verify(veiculoController).buscarPorId(veiculoId);
    }

    @Test
    @DisplayName("Deve listar todos os veículos")
    void shouldListAllVeiculos() throws Exception {
        // Given
        VeiculoResponseDTO veiculo2 = new VeiculoResponseDTO(
                UUID.randomUUID(),
                "XYZ-9876",
                "98765432109",
                "Honda",
                "Civic",
                2021
        );
        List<VeiculoResponseDTO> veiculos = Arrays.asList(veiculoResponseDTO, veiculo2);
        when(veiculoController.listarTodos()).thenReturn(veiculos);

        // When & Then
        mockMvc.perform(get("/api/v1/veiculos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(veiculoId.toString()))
                .andExpect(jsonPath("$[0].marca").value("Toyota"))
                .andExpect(jsonPath("$[1].marca").value("Honda"))
                .andExpect(jsonPath("$[1].modelo").value("Civic"));

        verify(veiculoController).listarTodos();
    }

    @Test
    @DisplayName("Deve atualizar veículo existente")
    void shouldUpdateVeiculoSuccessfully() throws Exception {
        // Given
        VeiculoRequestDTO updateRequest = new VeiculoRequestDTO(
                "ABC-1234",
                "12345678901",
                "Toyota",
                "Corolla Cross",
                2022,
                UUID.randomUUID()
        );
        VeiculoResponseDTO updatedResponse = new VeiculoResponseDTO(
                veiculoId,
                "ABC-1234",
                "12345678901",
                "Toyota",
                "Corolla Cross",
                2022
        );

        when(veiculoController.atualizar(eq(veiculoId), any(VeiculoRequestDTO.class)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/veiculos/{id}", veiculoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(veiculoId.toString()))
                .andExpect(jsonPath("$.modelo").value("Corolla Cross"))
                .andExpect(jsonPath("$.ano").value(2022));

        verify(veiculoController).atualizar(eq(veiculoId), any(VeiculoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve deletar veículo com sucesso")
    void shouldDeleteVeiculoSuccessfully() throws Exception {
        // Given
        doNothing().when(veiculoController).deletar(veiculoId);

        // When & Then
        mockMvc.perform(delete("/api/v1/veiculos/{id}", veiculoId))
                .andExpect(status().isNoContent());

        verify(veiculoController).deletar(veiculoId);
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar deletar veículo inexistente")
    void shouldReturn404WhenDeletingNonExistentVeiculo() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Veículo não encontrado"))
                .when(veiculoController).deletar(veiculoId);

        // When & Then
        mockMvc.perform(delete("/api/v1/veiculos/{id}", veiculoId))
                .andExpect(status().isNotFound());

        verify(veiculoController).deletar(veiculoId);
    }

    @Test
    @DisplayName("Deve retornar erro de validação para dados inválidos")
    void shouldReturnValidationErrorForInvalidData() throws Exception {
        // Given
        VeiculoRequestDTO invalidRequest = new VeiculoRequestDTO(
                "", // marca vazia
                "", // modelo vazio
                "", // placa vazia
                "", // renavam vazio
                0,  // ano inválido
                null // clienteId nulo
        );

        // When & Then
        mockMvc.perform(post("/api/v1/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(veiculoController, never()).criar(any());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há veículos")
    void shouldReturnEmptyListWhenNoVeiculos() throws Exception {
        // Given
        when(veiculoController.listarTodos()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/veiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(veiculoController).listarTodos();
    }

    @Test
    @DisplayName("Deve chamar controller com parâmetros corretos na criação")
    void shouldCallControllerWithCorrectParametersOnCreate() throws Exception {
        // Given
        when(veiculoController.criar(any(VeiculoRequestDTO.class)))
                .thenReturn(veiculoResponseDTO);

        // When
        mockMvc.perform(post("/api/v1/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veiculoRequestDTO)))
                .andExpect(status().isCreated());

        // Then
        verify(veiculoController).criar(
                argThat(request ->
                        "Toyota".equals(request.marca()) && 
                        "Corolla".equals(request.modelo()) &&
                        "ABC-1234".equals(request.placa()) &&
                        2020 == request.ano()
                )
        );
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar veículo inexistente")
    void shouldReturn404WhenUpdatingNonExistentVeiculo() throws Exception {
        // Given
        VeiculoRequestDTO updateRequest = new VeiculoRequestDTO(
                "DEF-5678",
                "11122233344",
                "Ford",
                "Focus",
                2019,
                UUID.randomUUID()
        );

        when(veiculoController.atualizar(eq(veiculoId), any(VeiculoRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Veículo não encontrado"));

        // When & Then
        mockMvc.perform(put("/api/v1/veiculos/{id}", veiculoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(veiculoController).atualizar(eq(veiculoId), any(VeiculoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios na criação")
    void shouldValidateRequiredFieldsOnCreate() throws Exception {
        // Given
        VeiculoRequestDTO invalidRequest = new VeiculoRequestDTO(
                null, // marca nula
                "Civic", 
                "ABC-1234", 
                "12345678901", 
                2020, 
                UUID.randomUUID()
        );

        // When & Then
        mockMvc.perform(post("/api/v1/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(veiculoController, never()).criar(any());
    }
}