package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.infrastructure.security.jwt.AuthRequest;
import br.com.grupo99.oficinaservice.infrastructure.security.jwt.AuthResponse;
import br.com.grupo99.oficinaservice.infrastructure.security.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para AuthController")
class AuthControllerUnitTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new br.com.grupo99.oficinaservice.infrastructure.rest.handler.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Deve autenticar usuário com credenciais válidas")
    void shouldAuthenticateUserWithValidCredentials() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("admin", "password");
        String expectedToken = "jwt-token-123";
        UserDetails userDetails = new User("admin", "password", new ArrayList<>());

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(expectedToken));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService).loadUserByUsername("admin");
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    @DisplayName("Deve retornar erro 500 para credenciais inválidas (BadCredentialsException não tratada)")
    void shouldReturn500ForBadCredentials() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("admin", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isInternalServerError());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para requisição sem body")
    void shouldReturn400ForEmptyBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(authenticationManager, never()).authenticate(any());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve retornar erro 400 para JSON inválido")
    void shouldReturn400ForInvalidJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json}"))
                .andExpect(status().isInternalServerError());

        verify(authenticationManager, never()).authenticate(any());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve chamar AuthenticationManager com token correto")
    void shouldCallAuthenticationManagerWithCorrectToken() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("testuser", "testpass");
        UserDetails userDetails = new User("testuser", "testpass", new ArrayList<>());

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("test-token");

        // When
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());

        // Then
        verify(authenticationManager).authenticate(
                argThat(token -> token instanceof UsernamePasswordAuthenticationToken &&
                        "testuser".equals(token.getName()) &&
                        "testpass".equals(token.getCredentials()))
        );
    }

    @Test
    @DisplayName("Deve gerar token para usuário válido")
    void shouldGenerateTokenForValidUser() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("validuser", "validpass");
        UserDetails userDetails = new User("validuser", "validpass", new ArrayList<>());
        String expectedToken = "generated-jwt-token";

        when(userDetailsService.loadUserByUsername("validuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(expectedToken));

        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    @DisplayName("Deve aceitar campos de AuthRequest válidos")
    void shouldAcceptValidAuthRequestFields() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("user@email.com", "complexPassword123!");
        UserDetails userDetails = new User("user@email.com", "complexPassword123!", new ArrayList<>());

        when(userDetailsService.loadUserByUsername("user@email.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("token");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());

        verify(userDetailsService).loadUserByUsername("user@email.com");
    }

    @Test
    @DisplayName("Deve retornar AuthResponse com estrutura correta")
    void shouldReturnAuthResponseWithCorrectStructure() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("admin", "password");
        UserDetails userDetails = new User("admin", "password", new ArrayList<>());
        String token = "jwt-token-response";

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(token);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").value(token));
    }
}