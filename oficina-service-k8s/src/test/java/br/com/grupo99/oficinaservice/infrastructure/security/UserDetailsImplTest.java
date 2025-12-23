package br.com.grupo99.oficinaservice.infrastructure.security;

import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    private Pessoa pessoaAdmin;
    private Pessoa pessoaCliente;
    private UserDetailsImpl userDetailsAdmin;
    private UserDetailsImpl userDetailsCliente;

    @BeforeEach
    void setUp() {
        pessoaAdmin = new Pessoa(
                "12345678900",
                TipoPessoa.FISICA,
                "Admin User",
                "admin@teste.com",
                "$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6",
                Perfil.ADMIN
        );
        pessoaAdmin.setAtivo(true);

        pessoaCliente = new Pessoa(
                "98765432100",
                TipoPessoa.FISICA,
                "Cliente User",
                "cliente@teste.com",
                "$2a$10$encoded",
                Perfil.CLIENTE
        );
        pessoaCliente.setAtivo(true);

        userDetailsAdmin = new UserDetailsImpl(pessoaAdmin);
        userDetailsCliente = new UserDetailsImpl(pessoaCliente);
    }

    @Test
    void deveRetornarEmailComoUsername() {
        assertEquals("admin@teste.com", userDetailsAdmin.getUsername());
        assertEquals("cliente@teste.com", userDetailsCliente.getUsername());
    }

    @Test
    void deveRetornarSenhaCriptografada() {
        assertEquals("$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6", 
                     userDetailsAdmin.getPassword());
    }

    @Test
    void deveRetornarAuthoritiesComPrefixoRole() {
        Collection<? extends GrantedAuthority> authoritiesAdmin = userDetailsAdmin.getAuthorities();
        Collection<? extends GrantedAuthority> authoritiesCliente = userDetailsCliente.getAuthorities();

        assertFalse(authoritiesAdmin.isEmpty());
        assertFalse(authoritiesCliente.isEmpty());

        assertTrue(authoritiesAdmin.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authoritiesCliente.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CLIENTE")));
    }

    @Test
    void deveRetornarContaHabilitadaQuandoAtiva() {
        assertTrue(userDetailsAdmin.isEnabled());
        assertTrue(userDetailsCliente.isEnabled());
    }

    @Test
    void deveRetornarContaDesabilitadaQuandoInativa() {
        pessoaAdmin.setAtivo(false);
        UserDetailsImpl userDetailsInativo = new UserDetailsImpl(pessoaAdmin);

        assertFalse(userDetailsInativo.isEnabled());
    }

    @Test
    void deveRetornarContaDesabilitadaQuandoAtivoNull() {
        pessoaAdmin.setAtivo(null);
        UserDetailsImpl userDetailsNull = new UserDetailsImpl(pessoaAdmin);

        assertFalse(userDetailsNull.isEnabled());
    }

    @Test
    void deveRetornarContaNaoExpirada() {
        assertTrue(userDetailsAdmin.isAccountNonExpired());
    }

    @Test
    void deveRetornarContaNaoBloqueada() {
        assertTrue(userDetailsAdmin.isAccountNonLocked());
    }

    @Test
    void deveRetornarCredenciaisNaoExpiradas() {
        assertTrue(userDetailsAdmin.isCredentialsNonExpired());
    }

    @Test
    void deveRetornarPessoaEncapsulada() {
        Pessoa pessoaRetornada = userDetailsAdmin.getPessoa();

        assertNotNull(pessoaRetornada);
        assertEquals(pessoaAdmin.getEmail(), pessoaRetornada.getEmail());
        assertEquals(pessoaAdmin.getName(), pessoaRetornada.getName());
        assertEquals(pessoaAdmin.getPerfil(), pessoaRetornada.getPerfil());
    }

    @Test
    void deveTestarDiferentesPerfisCriam_DiferentesAuthorities() {
        Pessoa mecanico = new Pessoa(
                "11111111111",
                TipoPessoa.FISICA,
                "Mecânico",
                "mecanico@teste.com",
                "$2a$10$encoded",
                Perfil.MECANICO
        );
        mecanico.setAtivo(true);

        UserDetailsImpl userDetailsMecanico = new UserDetailsImpl(mecanico);

        assertTrue(userDetailsMecanico.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_MECANICO")));
        assertFalse(userDetailsMecanico.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }
}
