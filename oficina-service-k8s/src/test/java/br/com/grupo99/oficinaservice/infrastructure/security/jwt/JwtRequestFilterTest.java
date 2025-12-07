package br.com.grupo99.oficinaservice.infrastructure.security.jwt;

import br.com.grupo99.oficinaservice.infrastructure.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.GrantedAuthority;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtRequestFilter - Testes Unitários")
class JwtRequestFilterTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            "teste@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private JwtUtil jwtUtil;

    // UserDetailsService não é mais usado após refatoração

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtRequestFilter jwtRequestFilter;

    private UserDetails userDetails;
    private String validToken;
    private String username;

    @BeforeEach
    void setUp() {
        username = "testuser";
        validToken = "valid.jwt.token";
        
        userDetails = User.builder()
                .username(username)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Configurar SecurityContextHolder mock
        SecurityContextHolder.setContext(securityContext);
    }
    private JwtUserDetails createMockJwtUserDetails() {
        JwtUserDetails mockUserDetails = mock(JwtUserDetails.class);
        lenient().when(mockUserDetails.getUsername()).thenReturn(username);
        lenient().when(mockUserDetails.getAuthorities()).thenReturn(Collections.emptyList());
        return mockUserDetails;
    }


    @Test
    @DisplayName("Deve processar requisição com token JWT válido")
    void deveProcessarRequisicaoComTokenJWTValido() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        JwtUserDetails mockUserDetails = createMockJwtUserDetails();
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUserDetails(validToken)).thenReturn(mockUserDetails);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.isTokenValid(validToken, mockUserDetails)).thenReturn(true);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).extractUserDetails(validToken);
        verify(jwtUtil).isTokenValid(validToken, mockUserDetails);
        verify(securityContext).setAuthentication(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve continuar filter chain quando Authorization header está ausente")
    void deveContinuarFilterChainQuandoAuthorizationHeaderEstaAusente() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUserDetails(anyString());
        // UserDetailsService não é mais usado
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    @DisplayName("Deve continuar filter chain quando Authorization header não começa com Bearer")
    void deveContinuarFilterChainQuandoAuthorizationHeaderNaoComecaComBearer() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUserDetails(anyString());
        // UserDetailsService não é mais usado
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    @DisplayName("Deve continuar filter chain quando token lança exceção")
    void deveContinuarFilterChainQuandoUsernameEhNulo() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUserDetails(validToken)).thenThrow(new RuntimeException("Token inválido"));

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).extractUserDetails(validToken);
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    @DisplayName("Deve continuar filter chain quando usuário já está autenticado")
    void deveContinuarFilterChainQuandoUsuarioJaEstaAutenticado() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        Authentication existingAuth = mock(Authentication.class);
        JwtUserDetails mockUserDetails = createMockJwtUserDetails();
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        lenient().when(jwtUtil.extractUserDetails(validToken)).thenReturn(mockUserDetails);
        when(securityContext.getAuthentication()).thenReturn(existingAuth);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    @DisplayName("Deve continuar filter chain quando token é inválido")
    void deveContinuarFilterChainQuandoTokenEhInvalido() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        JwtUserDetails mockUserDetails = createMockJwtUserDetails();
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUserDetails(validToken)).thenReturn(mockUserDetails);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.isTokenValid(validToken, mockUserDetails)).thenReturn(false);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).extractUserDetails(validToken);
        verify(jwtUtil).isTokenValid(validToken, mockUserDetails);
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }

    @Test
    @DisplayName("Deve extrair token corretamente do header Authorization")
    void deveExtrairTokenCorretamenteDoHeaderAuthorization() throws ServletException, IOException {
        // Given
        String tokenValue = "meu.token.jwt";
        String authHeader = "Bearer " + tokenValue;
        JwtUserDetails mockUserDetails = createMockJwtUserDetails();
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUserDetails(tokenValue)).thenReturn(mockUserDetails);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.isTokenValid(tokenValue, mockUserDetails)).thenReturn(true);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil).extractUserDetails(tokenValue);
        verify(jwtUtil).isTokenValid(tokenValue, mockUserDetails);
    }

    @Test
    @DisplayName("Deve configurar autenticação no SecurityContext quando token é válido")
    void deveConfigurarAutenticacaoNoSecurityContextQuandoTokenEhValido() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        JwtUserDetails mockUserDetails = createMockJwtUserDetails();
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUserDetails(validToken)).thenReturn(mockUserDetails);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.isTokenValid(validToken, mockUserDetails)).thenReturn(true);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(securityContext).setAuthentication(argThat(auth -> {
            assertThat(auth.getPrincipal()).isEqualTo(mockUserDetails);
            assertThat(auth.getCredentials()).isNull();
            List<String> authAuthorities = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            List<String> userDetailsAuthorities = mockUserDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            assertThat(authAuthorities).containsExactlyInAnyOrderElementsOf(userDetailsAuthorities);
            assertThat(auth.getDetails()).isNotNull();
            return true;
        }));
    }

    @Test
    @DisplayName("Deve processar header Authorization com espaços extras")
    void deveProcessarHeaderAuthorizationComEspacosExtras() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer    " + validToken; // espaços extras
        JwtUserDetails mockUserDetails = createMockJwtUserDetails();
        
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUserDetails("   " + validToken)).thenReturn(mockUserDetails);
        when(securityContext.getAuthentication()).thenReturn(null);
        when(jwtUtil.isTokenValid("   " + validToken, mockUserDetails)).thenReturn(true);

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil).extractUserDetails("   " + validToken);
    }

    @Test
    @DisplayName("Deve continuar processamento quando extractUserDetails lança exceção")
    void deveContinuarProcessamentoQuandoUserDetailsServiceLancaExcecao() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUserDetails(validToken)).thenThrow(new RuntimeException("Token inválido"));

        // When
        jwtRequestFilter.doFilterInternal(request, response, filterChain);

        // Then
        // O filtro captura a exceção e continua o processamento
        verify(filterChain).doFilter(request, response);
        verify(securityContext, never()).setAuthentication(any());
    }
}