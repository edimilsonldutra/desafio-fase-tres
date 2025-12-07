package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoRequestDTO;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoStatusUpdateRequestDTO;
import br.com.grupo99.oficinaservice.domain.model.*;
import br.com.grupo99.oficinaservice.domain.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Teste de Integração - OrdemServicoController")
class OrdemServicoControllerIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private PecaRepository pecaRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private OrdemServicoRepository osRepository;

    private Cliente cliente;
    private Veiculo veiculo;
    private Peca peca;
    private Servico servico;

    @BeforeEach
    void setUp() {
        cliente = criarClienteComPessoa("Cliente OS Teste", "11122233344");
        veiculo = new Veiculo("OST-0001", "Test", "Model S", 2025);
        veiculo.setCliente(cliente);
        cliente.getVeiculos().add(veiculo);
        cliente = clienteRepository.save(cliente);
        veiculo = cliente.getVeiculos().get(0);

        peca = new Peca();
        peca.setNome("Filtro de Ar");
        peca.setPreco(new BigDecimal("80.00"));
        peca.setEstoque(15);
        peca = pecaRepository.save(peca);

        servico = new Servico("Serviço Teste", new BigDecimal("100.00"));
        servico.setDescricao("Troca de Filtro de Ar");
        servico.setPreco(new BigDecimal("50.00"));
        servico = servicoRepository.save(servico);
    }

    @Test
    @DisplayName("GET /ordens-servico/{id} - Deve permitir acesso público e retornar detalhes da OS")
    void devePermitirAcessoPublicoParaBuscarOsPorId() throws Exception {
        // Given
        OrdemServico os = osRepository.save(new OrdemServico(cliente.getId(), veiculo.getId()));

        // When & Then
        mockMvc.perform(get("/api/v1/ordens-servico/{id}", os.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(os.getId().toString()))
                .andExpect(jsonPath("$.cliente.nome").value(cliente.getPessoa().getName()));
    }

    @Test
    @DisplayName("GET /ordens-servico/{id} - Deve retornar 404 para OS inexistente")
    void deveRetornarNotFoundParaOsInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/ordens-servico/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /ordens-servico - Deve criar uma OS com sucesso e retornar 201 Created")
    @WithMockUser(username = "admin")
    void deveCriarOSComSucesso() throws Exception {
        // Given
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                cliente.getPessoa().getNumeroDocumento(),
                veiculo.getPlaca(),
                List.of(servico.getId()),
                List.of(peca.getId())
        );

        // When & Then
        mockMvc.perform(post("/api/v1/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.clienteNome").value(cliente.getPessoa().getName()))
                .andExpect(jsonPath("$.placaVeiculo").value(veiculo.getPlaca()))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.valorTotal").value(130.00));
    }

    @Test
    @DisplayName("GET /ordens-servico - Deve listar todas as OS")
    @WithMockUser(username = "admin")
    void deveListarTodasAsOS() throws Exception {
        // Given
        osRepository.save(new OrdemServico(cliente.getId(), veiculo.getId()));
        osRepository.save(new OrdemServico(cliente.getId(), veiculo.getId()));

        // When & Then
        mockMvc.perform(get("/api/v1/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("PATCH /ordens-servico/{id}/status - Deve atualizar o status com sucesso")
    @WithMockUser(username = "admin")
    void deveAtualizarStatusComSucesso() throws Exception {
        // Given
        OrdemServico os = osRepository.save(new OrdemServico(cliente.getId(), veiculo.getId()));
        OrdemServicoStatusUpdateRequestDTO request = new OrdemServicoStatusUpdateRequestDTO(StatusOS.EM_DIAGNOSTICO);

        // When & Then
        mockMvc.perform(patch("/api/v1/ordens-servico/{id}/status", os.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("EM_DIAGNOSTICO")));
    }
}