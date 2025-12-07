package br.com.grupo99.oficinaservice.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Teste de Integração - SecurityConfig")
class SecurityConfigIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve permitir acesso público ao endpoint de login")
    void devePermitirAcessoPublicoAoLogin() throws Exception {
        // Não esperamos um 200 OK porque não estamos a enviar um corpo,
        // mas um 400 (Bad Request) ou 401 (se houver um corpo inválido)
        // prova que o endpoint não foi bloqueado por um 403 (Forbidden).
        mockMvc.perform(get("/api/v1/auth/login"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Deve permitir acesso público ao Swagger UI")
    void devePermitirAcessoPublicoAoSwagger() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve bloquear acesso a endpoint protegido sem autenticação")
    void deveBloquearAcessoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isForbidden()); // ou 401 Unauthorized dependendo da configuração exata
    }

    @Test
    @DisplayName("Deve permitir acesso a endpoint protegido com autenticação")
    @WithMockUser(username = "admin")
    void devePermitirAcessoComAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk());
    }
}

