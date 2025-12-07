package br.com.grupo99.oficinaservice.infrastructure.rest;
import br.com.grupo99.oficinaservice.application.dto.ClienteRequestDTO;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Teste de Integração - ClienteController")
@ActiveProfiles("test")
class ClienteControllerIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }



    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private ClienteRepository clienteRepository;

    ClienteControllerIT(MockMvc mockMvc, ObjectMapper objectMapper, ClienteRepository clienteRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.clienteRepository = clienteRepository;
    }

    @Test
    @DisplayName("POST /clientes - Deve criar um cliente e retornar 201 Created")
    @WithMockUser(username = "admin")
    void deveCriarCliente() throws Exception {
        ClienteRequestDTO request = new ClienteRequestDTO("Novo Cliente", "111.222.333-44", "11987654321", "novo@cliente.com");

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.nome", is("Novo Cliente")));
    }

    @Test
    @DisplayName("POST /clientes - Deve retornar 400 Bad Request para dados inválidos")
    @WithMockUser(username = "admin")
    void deveRetornarBadRequestParaDadosInvalidos() throws Exception {
        ClienteRequestDTO request = new ClienteRequestDTO("", "", "11987654321", "email-invalido");

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /clientes/{id} - Deve buscar um cliente pelo ID")
    @WithMockUser(username = "admin")
    void deveBuscarClientePorId() throws Exception {
        Cliente cliente = clienteRepository.save(criarClienteComPessoa("Cliente Existente", "55566677788"));

        mockMvc.perform(get("/api/v1/clientes/{id}", cliente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(cliente.getId().toString())))
                .andExpect(jsonPath("$.nome", is("Cliente Existente")));
    }

    @Test
    @DisplayName("GET /clientes/{id} - Deve retornar 404 Not Found para ID inexistente")
    @WithMockUser(username = "admin")
    void deveRetornarNotFoundParaIdInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /clientes/{id} - Deve atualizar um cliente com sucesso")
    @WithMockUser(username = "admin")
    void deveAtualizarCliente() throws Exception {
        Cliente cliente = clienteRepository.save(criarClienteComPessoa("Nome Antigo", "88888888888"));
        ClienteRequestDTO request = new ClienteRequestDTO("Nome Atualizado", "888.888.888-88", null, "atualizado@email.com");

        mockMvc.perform(put("/api/v1/clientes/{id}", cliente.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Nome Atualizado")));
    }

    @Test
    @DisplayName("DELETE /clientes/{id} - Deve deletar um cliente com sucesso")
    @WithMockUser(username = "admin")
    void deveDeletarCliente() throws Exception {
        Cliente cliente = clienteRepository.save(criarClienteComPessoa("Cliente para Deletar", "99999999999"));

        mockMvc.perform(delete("/api/v1/clientes/{id}", cliente.getId()))
                .andExpect(status().isNoContent());
    }
}