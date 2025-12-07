package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.application.dto.PecaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaUpdateEstoqueRequestDTO;
import br.com.grupo99.oficinaservice.domain.model.Peca;
import br.com.grupo99.oficinaservice.domain.repository.PecaRepository;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Teste de Integração - PecaController")
@ActiveProfiles("test")
class PecaControllerIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PecaRepository pecaRepository;

    @Test
    @DisplayName("Deve criar uma peça e retornar 201 Created")
    @WithMockUser(username = "admin")
    void deveCriarPeca() throws Exception {
        PecaRequestDTO request = new PecaRequestDTO("Amortecedor", "Cofap", new BigDecimal("250.00"), 10);

        mockMvc.perform(post("/api/v1/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome", is("Amortecedor")))
                .andExpect(jsonPath("$.estoque", is(10)));
    }

    @Test
    @DisplayName("Deve atualizar o estoque de uma peça")
    @WithMockUser(username = "admin")
    void deveAtualizarEstoque() throws Exception {
        Peca peca = new Peca();
        peca.setNome("Vela");
        peca.setEstoque(5);
        peca.setPreco(new BigDecimal("20.00"));
        peca = pecaRepository.save(peca);

        PecaUpdateEstoqueRequestDTO request = new PecaUpdateEstoqueRequestDTO(10); // Adiciona 10

        mockMvc.perform(patch("/api/v1/pecas/{id}/estoque", peca.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoque", is(15)));
    }
}
