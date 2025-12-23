package br.com.grupo99.oficinaservice.infrastructure.security;

import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;
import br.com.grupo99.oficinaservice.domain.repository.PessoaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private Pessoa pessoaAtiva;
    private Pessoa pessoaInativa;

    @BeforeEach
    void setUp() {
        pessoaAtiva = new Pessoa(
                "12345678900",
                TipoPessoa.FISICA,
                "João Silva",
                "joao@teste.com",
                "$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6",
                Perfil.ADMIN);
        pessoaAtiva.setAtivo(true);

        pessoaInativa = new Pessoa(
                "98765432100",
                TipoPessoa.FISICA,
                "Maria Santos",
                "maria@teste.com",
                "$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6",
                Perfil.CLIENTE);
        pessoaInativa.setAtivo(false);
    }

    @Test
    @DisplayName("Deve carregar usuário por email com sucesso")
    void shouldLoadUserByEmailSuccessfully() {
        // Given
        when(pessoaRepository.findByEmail("joao@teste.com"))
                .thenReturn(Optional.of(pessoaAtiva));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("joao@teste.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("joao@teste.com");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.getAuthorities()).isNotEmpty();
        assertThat(userDetails.getAuthorities()).anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException para usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(pessoaRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("naoexiste@teste.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta inativa")
    void shouldThrowExceptionWhenAccountInactive() {
        // Given
        when(pessoaRepository.findByEmail("maria@teste.com"))
                .thenReturn(Optional.of(pessoaInativa));

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("maria@teste.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Conta desativada");
    }

    @Test
    @DisplayName("Deve retornar UserDetailsImpl com perfil correto")
    void shouldReturnUserDetailsImplWithCorrectProfile() {
        // Given
        Pessoa mecanico = new Pessoa(
                "11111111111",
                TipoPessoa.FISICA,
                "Carlos Mecânico",
                "carlos@teste.com",
                "$2a$10$encoded",
                Perfil.MECANICO);
        mecanico.setAtivo(true);

        when(pessoaRepository.findByEmail("carlos@teste.com"))
                .thenReturn(Optional.of(mecanico));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("carlos@teste.com");

        // Then
        assertThat(userDetails).isInstanceOf(UserDetailsImpl.class);
        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) userDetails;
        assertThat(userDetailsImpl.getPessoa().getPerfil()).isEqualTo(Perfil.MECANICO);
        assertThat(userDetails.getAuthorities()).anyMatch(auth -> auth.getAuthority().equals("ROLE_MECANICO"));
    }
}
