package br.com.grupo99.apiexterna.controller;

import br.com.grupo99.apiexterna.dto.AprovacaoOrcamentoRequestDTO;
import br.com.grupo99.apiexterna.service.AprovacaoOrcamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AprovacaoOrcamentoController.class)
class AprovacaoOrcamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AprovacaoOrcamentoService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveAceitarAprovacaoDeOrcamento() throws Exception {
        AprovacaoOrcamentoRequestDTO dto = new AprovacaoOrcamentoRequestDTO();
        dto.setOrdemServicoId(UUID.randomUUID());
        dto.setAprovado(true);
        dto.setMotivoRecusa(null);

        mockMvc.perform(post("/api/v1/notificacoes-aprovacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void deveAceitarRecusaDeOrcamento() throws Exception {
        AprovacaoOrcamentoRequestDTO dto = new AprovacaoOrcamentoRequestDTO();
        dto.setOrdemServicoId(UUID.randomUUID());
        dto.setAprovado(false);
        dto.setMotivoRecusa("Cliente não aprovou o valor");

        mockMvc.perform(post("/api/v1/notificacoes-aprovacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}
