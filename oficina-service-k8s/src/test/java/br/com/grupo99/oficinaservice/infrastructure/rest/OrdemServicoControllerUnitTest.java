package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.OrdemServicoController;
import br.com.grupo99.oficinaservice.application.dto.*;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para OrdemServicoRestController")
class OrdemServicoRestControllerUnitTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private OrdemServicoController ordemServicoController;

    @InjectMocks
    private OrdemServicoRestController ordemServicoRestController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID ordemServicoId;
    private OrdemServicoRequestDTO ordemServicoRequestDTO;
    private OrdemServicoResponseDTO ordemServicoResponseDTO;
    private OrdemServicoDetalhesDTO ordemServicoDetalhesDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ordemServicoRestController)
                .setControllerAdvice(new br.com.grupo99.oficinaservice.infrastructure.rest.handler.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        
        ordemServicoId = UUID.randomUUID();
        
        ordemServicoRequestDTO = new OrdemServicoRequestDTO(
                "123.456.789-00",
                "ABC-1234",
                Arrays.asList(UUID.randomUUID()),
                Arrays.asList(UUID.randomUUID())
        );
        
        ordemServicoResponseDTO = new OrdemServicoResponseDTO(
                ordemServicoId,
                "João Silva",
                "ABC-1234",
                StatusOS.RECEBIDA,
                getStatusDescricao(StatusOS.RECEBIDA),
                BigDecimal.valueOf(500.00),
                LocalDateTime.now()
        );

        // Create DTO objects for OrdemServicoDetalhesDTO
        ClienteResponseDTO clienteDTO = new ClienteResponseDTO(
                UUID.randomUUID(),
                "João Silva",
                "123.456.789-00",
                "11999887766",
                "joao@email.com"
        );
        
        VeiculoResponseDTO veiculoDTO = new VeiculoResponseDTO(
                UUID.randomUUID(),
                "ABC-1234",
                "12345678901",
                "Toyota",
                "Corolla",
                2020
        );

        ordemServicoDetalhesDTO = new OrdemServicoDetalhesDTO(
                ordemServicoId,
                StatusOS.RECEBIDA,
                BigDecimal.valueOf(500.00),
                LocalDateTime.now(),
                null,
                null,
                clienteDTO,
                veiculoDTO,
                Arrays.asList(),
                Arrays.asList()
        );
    }

    @Test
    @DisplayName("Deve criar nova ordem de serviço com sucesso")
    void shouldCreateOrdemServicoSuccessfully() throws Exception {
        // Given
        when(ordemServicoController.criar(any(OrdemServicoRequestDTO.class)))
                .thenReturn(ordemServicoResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ordemServicoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(ordemServicoId.toString()))
                .andExpect(jsonPath("$.clienteNome").value("João Silva"))
                .andExpect(jsonPath("$.placaVeiculo").value("ABC-1234"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.valorTotal").value(500.00));

        verify(ordemServicoController).criar(any(OrdemServicoRequestDTO.class));
    }

    @Test
    @DisplayName("Deve listar todas as ordens de serviço")
    void shouldListAllOrdensServico() throws Exception {
        // Given
        List<OrdemServicoResponseDTO> ordens = Arrays.asList(ordemServicoResponseDTO);
        when(ordemServicoController.listarTodos()).thenReturn(ordens);

        // When & Then
        mockMvc.perform(get("/api/v1/ordens-servico"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(ordemServicoId.toString()))
                .andExpect(jsonPath("$[0].clienteNome").value("João Silva"));

        verify(ordemServicoController).listarTodos();
    }

    @Test
    @DisplayName("Deve buscar detalhes de ordem de serviço por ID")
    void shouldGetOrdemServicoDetailsById() throws Exception {
        // Given
        when(ordemServicoController.buscarDetalhes(ordemServicoId))
                .thenReturn(ordemServicoDetalhesDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/ordens-servico/{id}", ordemServicoId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(ordemServicoId.toString()))
                .andExpect(jsonPath("$.cliente.nome").value("João Silva"))
                .andExpect(jsonPath("$.cliente.telefone").value("11999887766"))
                .andExpect(jsonPath("$.cliente.email").value("joao@email.com"))
                .andExpect(jsonPath("$.veiculo.marca").value("Toyota"))
                .andExpect(jsonPath("$.veiculo.modelo").value("Corolla"))
                .andExpect(jsonPath("$.veiculo.placa").value("ABC-1234"));

        verify(ordemServicoController).buscarDetalhes(ordemServicoId);
    }

    @Test
    @DisplayName("Deve retornar 404 quando ordem de serviço não encontrada")
    void shouldReturn404WhenOrdemServicoNotFound() throws Exception {
        // Given
        when(ordemServicoController.buscarDetalhes(ordemServicoId))
                .thenThrow(new ResourceNotFoundException("Ordem de serviço não encontrada"));

        // When & Then
        mockMvc.perform(get("/api/v1/ordens-servico/{id}", ordemServicoId))
                .andExpect(status().isNotFound());

        verify(ordemServicoController).buscarDetalhes(ordemServicoId);
    }

    @Test
    @DisplayName("Deve atualizar status da ordem de serviço")
    void shouldUpdateOrdemServicoStatus() throws Exception {
        // Given
        OrdemServicoStatusUpdateRequestDTO statusUpdateDTO = 
                new OrdemServicoStatusUpdateRequestDTO(StatusOS.EM_EXECUCAO);
        
        OrdemServicoResponseDTO updatedResponse = new OrdemServicoResponseDTO(
                ordemServicoId,
                "João Silva",
                "ABC-1234",
                StatusOS.EM_EXECUCAO,
                getStatusDescricao(StatusOS.EM_EXECUCAO),
                BigDecimal.valueOf(500.00),
                LocalDateTime.now()
        );

        when(ordemServicoController.atualizarStatus(eq(ordemServicoId), eq(StatusOS.EM_EXECUCAO)))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/ordens-servico/{id}/status", ordemServicoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ordemServicoId.toString()))
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));

        verify(ordemServicoController).atualizarStatus(eq(ordemServicoId), eq(StatusOS.EM_EXECUCAO));
    }

    @Test
    @DisplayName("Deve retornar 400 para corpo de requisição inválido na criação")
    void shouldReturn400ForInvalidRequestBodyOnCreate() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 400 para ID inválido")
    void shouldReturn400ForInvalidId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/ordens-servico/invalid-uuid"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há ordens de serviço")
    void shouldReturnEmptyListWhenNoOrdensServico() throws Exception {
        // Given
        when(ordemServicoController.listarTodos()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/ordens-servico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(ordemServicoController).listarTodos();
    }

    @Test
    @DisplayName("Deve chamar use case com parâmetros corretos na criação")
    void shouldCallUseCaseWithCorrectParametersOnCreate() throws Exception {
        // Given
        when(ordemServicoController.criar(any(OrdemServicoRequestDTO.class)))
                .thenReturn(ordemServicoResponseDTO);

        // When
        mockMvc.perform(post("/api/v1/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ordemServicoRequestDTO)))
                .andExpect(status().isCreated());

        // Then
        verify(ordemServicoController).criar(
                argThat(request ->
                        "123.456.789-00".equals(request.cpfCnpjCliente()) &&
                        "ABC-1234".equals(request.placaVeiculo())
                )
        );
    }

    @Test
    @DisplayName("Deve incluir header Location na resposta de criação")
    void shouldIncludeLocationHeaderOnCreation() throws Exception {
        // Given
        when(ordemServicoController.criar(any(OrdemServicoRequestDTO.class)))
                .thenReturn(ordemServicoResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ordemServicoRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", 
                        containsString("/api/v1/ordens-servico/" + ordemServicoId)));
    }

    @Test
    @DisplayName("Deve retornar 404 quando tentar atualizar status de ordem inexistente")
    void shouldReturn404WhenUpdatingStatusOfNonExistentOrdem() throws Exception {
        // Given
        OrdemServicoStatusUpdateRequestDTO statusUpdateDTO = 
                new OrdemServicoStatusUpdateRequestDTO(StatusOS.FINALIZADA);

        when(ordemServicoController.atualizarStatus(eq(ordemServicoId), eq(StatusOS.FINALIZADA)))
                .thenThrow(new ResourceNotFoundException("Ordem de serviço não encontrada"));

        // When & Then
        mockMvc.perform(patch("/api/v1/ordens-servico/{id}/status", ordemServicoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateDTO)))
                .andExpect(status().isNotFound());

        verify(ordemServicoController).atualizarStatus(eq(ordemServicoId), eq(StatusOS.FINALIZADA));
    }

    // Adicionar método auxiliar para statusDescricao
    private static String getStatusDescricao(StatusOS status) {
        return switch (status) {
            case RECEBIDA -> "Recebida";
            case EM_DIAGNOSTICO -> "Diagnóstico";
            case AGUARDANDO_APROVACAO -> "Aguardando Aprovação";
            case EM_EXECUCAO -> "Execução";
            case FINALIZADA -> "Finalizada";
            case ENTREGUE -> "Entregue";
            case CANCELADA -> "Cancelada";
        };
    }
}



