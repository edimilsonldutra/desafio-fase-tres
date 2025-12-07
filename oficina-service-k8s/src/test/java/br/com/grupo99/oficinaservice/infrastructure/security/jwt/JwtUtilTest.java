package br.com.grupo99.oficinaservice.infrastructure.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil - Testes Unitários")
class JwtUtilTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private JwtUtil jwtUtil;
    private UserDetails userDetails;
    private String secretKey;
    private long jwtExpiration;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        secretKey = "dGVzdGVzZWNyZXRrZXl0ZXN0ZXNlY3JldGtleXRlc3Rlc2VjcmV0a2V5dGVzdGVzZWNyZXRrZXl0ZXN0ZXNlY3JldGtleQ==";
        jwtExpiration = 86400000L; // 24 horas

        // Configurar valores usando reflexão
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", jwtExpiration);

        // Criar UserDetails de teste
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("Deve gerar token JWT válido")
    void deveGerarTokenJWTValido() {
        // When
        String token = jwtUtil.generateToken(userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature
    }

    @Test
    @DisplayName("Deve gerar token JWT com claims extras")
    void deveGerarTokenJWTComClaimsExtras() {
        // Given
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "USER");
        extraClaims.put("customField", "customValue");

        // When
        String token = jwtUtil.generateToken(extraClaims, userDetails);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // Verificar se as claims extras estão presentes
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secretKey)))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertThat(claims.get("role")).isEqualTo("USER");
        assertThat(claims.get("customField")).isEqualTo("customValue");
    }

    @Test
    @DisplayName("Deve extrair username do token")
    void deveExtrairUsernameDoToken() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Deve extrair claim específica do token")
    void deveExtrairClaimEspecificaDoToken() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String subject = jwtUtil.extractClaim(token, Claims::getSubject);
        Date expiration = jwtUtil.extractClaim(token, Claims::getExpiration);

        // Then
        assertThat(subject).isEqualTo("testuser");
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("Deve validar token válido")
    void deveValidarTokenValido() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        boolean isValid = jwtUtil.isTokenValid(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve invalidar token com username incorreto")
    void deveInvalidarTokenComUsernameIncorreto() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        UserDetails wrongUserDetails = User.builder()
                .username("wronguser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        boolean isValid = jwtUtil.isTokenValid(token, wrongUserDetails);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar token expirado")
    void deveInvalidarTokenExpirado() throws InterruptedException {
        // Given - configurar tempo de expiração muito curto
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 10L); // 10ms
        String expiredToken = jwtUtil.generateToken(userDetails);
        
        // Aguardar um pouco para garantir expiração
        Thread.sleep(100);
        
        // Restaurar configuração normal
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", jwtExpiration);

        // When & Then - deve lançar ExpiredJwtException ao tentar extrair username
        assertThatThrownBy(() -> jwtUtil.extractUsername(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção para token malformado")
    void deveLancarExcecaoParaTokenMalformado() {
        // Given
        String malformedToken = "token.malformado.aqui";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção para token com assinatura inválida")
    void deveLancarExcecaoParaTokenComAssinaturaInvalida() {
        // Given - criar token com chave diferente
        Key wrongKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(wrongKey, SignatureAlgorithm.HS256)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(tokenWithWrongSignature))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Deve extrair data de expiração correta")
    void deveExtrairDataDeExpiracaoCorreta() {
        // Given
        String token = jwtUtil.generateToken(userDetails);
        Date beforeGeneration = new Date(System.currentTimeMillis() + jwtExpiration - 1000);
        Date afterGeneration = new Date(System.currentTimeMillis() + jwtExpiration + 1000);

        // When
        Date expiration = jwtUtil.extractClaim(token, Claims::getExpiration);

        // Then
        assertThat(expiration).isBetween(beforeGeneration, afterGeneration);
    }

    @Test
    @DisplayName("Deve validar token gerado sem claims extras")
    void deveValidarTokenGeradoSemClaimsExtras() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        boolean isValid = jwtUtil.isTokenValid(token, userDetails);
        String username = jwtUtil.extractUsername(token);

        // Then
        assertThat(isValid).isTrue();
        assertThat(username).isEqualTo(userDetails.getUsername());
    }

    @Test
    @DisplayName("Deve extrair subject corretamente")
    void deveExtrairSubjectCorretamente() {
        // Given
        String token = jwtUtil.generateToken(userDetails);

        // When
        String subject = jwtUtil.extractClaim(token, Claims::getSubject);

        // Then
        assertThat(subject).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Deve lidar com token nulo ou vazio")
    void deveLidarComTokenNuloOuVazio() {
        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(null))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> jwtUtil.extractUsername(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}