package br.com.grupo99.oficinaservice.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para UserDetailsServiceImpl")
class UserDetailsServiceImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("Deve carregar usuário admin com sucesso")
    void shouldLoadAdminUserSuccessfully() {
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6");
        assertThat(userDetails.getAuthorities()).isEmpty();
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException para usuário não encontrado")
    void shouldThrowUsernameNotFoundExceptionForNonExistentUser() {
        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("inexistente"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Utilizador não encontrado: inexistente");
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException para username null")
    void shouldThrowUsernameNotFoundExceptionForNullUsername() {
        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Utilizador não encontrado: null");
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException para username vazio")
    void shouldThrowUsernameNotFoundExceptionForEmptyUsername() {
        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Utilizador não encontrado: ");
    }

    @Test
    @DisplayName("Deve ser case sensitive para username")
    void shouldBeCaseSensitiveForUsername() {
        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ADMIN"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Utilizador não encontrado: ADMIN");
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException para username com espaços")
    void shouldThrowUsernameNotFoundExceptionForUsernameWithSpaces() {
        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(" admin "))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Utilizador não encontrado:  admin ");
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException para outros usernames válidos")
    void shouldThrowUsernameNotFoundExceptionForOtherValidUsernames() {
        // Given
        String[] invalidUsernames = {"user", "test", "administrator", "root", "guest"};

        // When & Then
        for (String username : invalidUsernames) {
            assertThatThrownBy(() -> userDetailsService.loadUserByUsername(username))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessage("Utilizador não encontrado: " + username);
        }
    }

    @Test
    @DisplayName("Deve retornar UserDetails com coleção de authorities vazia")
    void shouldReturnUserDetailsWithEmptyAuthoritiesCollection() {
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(userDetails.getAuthorities()).isNotNull();
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar UserDetails com todas as flags de conta ativas")
    void shouldReturnUserDetailsWithAllAccountFlagsEnabled() {
        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }
}