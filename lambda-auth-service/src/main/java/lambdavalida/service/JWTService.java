package lambdavalida.service;

import com.newrelic.api.agent.NewRelic;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lambdavalida.model.Pessoa;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class JWTService {
    private static final String SECRET = System.getenv().getOrDefault("JWT_SECRET", "my-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long");
    private final SecretKey key;

    public JWTService() {
        this.key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Pessoa pessoa) {
        try {
            Instant now = Instant.now();
            Instant exp = now.plus(1, ChronoUnit.HOURS);

            return Jwts.builder()
                .subject(pessoa.getNumeroDocumento())
                .claim("pessoaId", pessoa.getId())
                .claim("numeroDocumento", pessoa.getNumeroDocumento())
                .claim("tipoPessoa", pessoa.getTipoPessoa())
                .claim("name", pessoa.getName())
                .claim("email", pessoa.getEmail())
                .claim("cargo", pessoa.getCargo())
                .claim("perfil", pessoa.getPerfil())
                .claim("status", pessoa.getStatus())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
        } catch (Exception e) {
            NewRelic.noticeError(e);
            throw new RuntimeException("JWT generation failed", e);
        }
    }
}
