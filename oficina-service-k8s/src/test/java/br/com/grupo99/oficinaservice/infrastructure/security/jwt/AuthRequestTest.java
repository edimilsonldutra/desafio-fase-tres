package br.com.grupo99.oficinaservice.infrastructure.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("AuthRequest - Testes Unitários")
class AuthRequestTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Test
    @DisplayName("Deve criar AuthRequest com username e password")
    void deveCriarAuthRequestComUsernameEPassword() {
        // Given
        String username = "testuser";
        String password = "testpassword";

        // When
        AuthRequest authRequest = new AuthRequest(username, password);

        // Then
        assertThat(authRequest.username()).isEqualTo(username);
        assertThat(authRequest.password()).isEqualTo(password);
    }

    @Test
    @DisplayName("Deve criar AuthRequest com valores nulos")
    void deveCriarAuthRequestComValoresNulos() {
        // When
        AuthRequest authRequest = new AuthRequest(null, null);

        // Then
        assertThat(authRequest.username()).isNull();
        assertThat(authRequest.password()).isNull();
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void deveImplementarEqualsCorretamente() {
        // Given
        AuthRequest authRequest1 = new AuthRequest("user", "pass");
        AuthRequest authRequest2 = new AuthRequest("user", "pass");
        AuthRequest authRequest3 = new AuthRequest("other", "pass");

        // Then
        assertThat(authRequest1).isEqualTo(authRequest2);
        assertThat(authRequest1).isNotEqualTo(authRequest3);
        assertThat(authRequest1).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Deve implementar hashCode corretamente")
    void deveImplementarHashCodeCorretamente() {
        // Given
        AuthRequest authRequest1 = new AuthRequest("user", "pass");
        AuthRequest authRequest2 = new AuthRequest("user", "pass");

        // Then
        assertThat(authRequest1.hashCode()).isEqualTo(authRequest2.hashCode());
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Given
        AuthRequest authRequest = new AuthRequest("testuser", "testpass");

        // When
        String toString = authRequest.toString();

        // Then
        assertThat(toString).contains("AuthRequest");
        assertThat(toString).contains("testuser");
        assertThat(toString).contains("testpass");
    }

    @Test
    @DisplayName("Deve permitir username vazio")
    void devePermitirUsernameVazio() {
        // Given
        String username = "";
        String password = "password";

        // When
        AuthRequest authRequest = new AuthRequest(username, password);

        // Then
        assertThat(authRequest.username()).isEmpty();
        assertThat(authRequest.password()).isEqualTo(password);
    }

    @Test
    @DisplayName("Deve permitir password vazio")
    void devePermitirPasswordVazio() {
        // Given
        String username = "username";
        String password = "";

        // When
        AuthRequest authRequest = new AuthRequest(username, password);

        // Then
        assertThat(authRequest.username()).isEqualTo(username);
        assertThat(authRequest.password()).isEmpty();
    }
}