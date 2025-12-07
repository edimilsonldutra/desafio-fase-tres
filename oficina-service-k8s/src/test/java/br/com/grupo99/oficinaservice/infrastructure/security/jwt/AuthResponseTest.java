package br.com.grupo99.oficinaservice.infrastructure.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("AuthResponse - Testes Unitários")
class AuthResponseTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Test
    @DisplayName("Deve criar AuthResponse com token")
    void deveCriarAuthResponseComToken() {
        // Given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

        // When
        AuthResponse authResponse = new AuthResponse(token);

        // Then
        assertThat(authResponse.token()).isEqualTo(token);
    }

    @Test
    @DisplayName("Deve criar AuthResponse com token nulo")
    void deveCriarAuthResponseComTokenNulo() {
        // When
        AuthResponse authResponse = new AuthResponse(null);

        // Then
        assertThat(authResponse.token()).isNull();
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void deveImplementarEqualsCorretamente() {
        // Given
        String token = "test.jwt.token";
        AuthResponse authResponse1 = new AuthResponse(token);
        AuthResponse authResponse2 = new AuthResponse(token);
        AuthResponse authResponse3 = new AuthResponse("different.token");

        // Then
        assertThat(authResponse1).isEqualTo(authResponse2);
        assertThat(authResponse1).isNotEqualTo(authResponse3);
        assertThat(authResponse1).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void deveImplementarHashCodeCorretamente() {
        // Given
        String token = "test.jwt.token";
        AuthResponse authResponse1 = new AuthResponse(token);
        AuthResponse authResponse2 = new AuthResponse(token);

        // Then
        assertThat(authResponse1.hashCode()).isEqualTo(authResponse2.hashCode());
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Given
        String token = "test.jwt.token";
        AuthResponse authResponse = new AuthResponse(token);

        // When
        String toString = authResponse.toString();

        // Then
        assertThat(toString).contains("AuthResponse");
        assertThat(toString).contains(token);
    }

    @Test
    @DisplayName("Deve permitir token vazio")
    void devePermitirTokenVazio() {
        // Given
        String token = "";

        // When
        AuthResponse authResponse = new AuthResponse(token);

        // Then
        assertThat(authResponse.token()).isEmpty();
    }

    @Test
    @DisplayName("Deve manter token com espaços")
    void deveManterTokenComEspacos() {
        // Given
        String token = " token with spaces ";

        // When
        AuthResponse authResponse = new AuthResponse(token);

        // Then
        assertThat(authResponse.token()).isEqualTo(token);
    }

    @Test
    @DisplayName("Deve manter token longo")
    void deveManterTokenLongo() {
        // Given
        String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When
        AuthResponse authResponse = new AuthResponse(longToken);

        // Then
        assertThat(authResponse.token()).isEqualTo(longToken);
    }

    @Test
    @DisplayName("Deve comparar tokens diferentes corretamente")
    void deveCompararTokensDiferentesCorretamente() {
        // Given
        AuthResponse authResponse1 = new AuthResponse("token1");
        AuthResponse authResponse2 = new AuthResponse("token2");

        // Then
        assertThat(authResponse1).isNotEqualTo(authResponse2);
        assertThat(authResponse1.hashCode()).isNotEqualTo(authResponse2.hashCode());
    }

    @Test
    @DisplayName("Deve comparar com token nulo corretamente")
    void deveCompararComTokenNuloCorretamente() {
        // Given
        AuthResponse authResponseWithToken = new AuthResponse("token");
        AuthResponse authResponseWithNull = new AuthResponse(null);

        // Then
        assertThat(authResponseWithToken).isNotEqualTo(authResponseWithNull);
        assertThat(authResponseWithToken.hashCode()).isNotEqualTo(authResponseWithNull.hashCode());
    }
}