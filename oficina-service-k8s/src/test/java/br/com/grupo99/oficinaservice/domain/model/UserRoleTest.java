package br.com.grupo99.oficinaservice.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

class UserRoleTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Test
    void deveRetornarDisplayNameCorreto() {
        assertThat(UserRole.CLIENTE.getDisplayName()).isEqualTo("Cliente");
        assertThat(UserRole.MECANICO.getDisplayName()).isEqualTo("Mecânico");
        assertThat(UserRole.ADMIN.getDisplayName()).isEqualTo("Administrador");
    }

    @Test
    void deveConverterStringParaEnum() {
        assertThat(UserRole.fromString("CLIENTE")).isEqualTo(UserRole.CLIENTE);
        assertThat(UserRole.fromString("cliente")).isEqualTo(UserRole.CLIENTE);
        assertThat(UserRole.fromString("MECANICO")).isEqualTo(UserRole.MECANICO);
        assertThat(UserRole.fromString("mecanico")).isEqualTo(UserRole.MECANICO);
        assertThat(UserRole.fromString("ADMIN")).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void deveLancarExcecaoQuandoRoleInvalida() {
        assertThatThrownBy(() -> UserRole.fromString("INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role inválido: INVALID");
    }

    @Test
    void deveLancarExcecaoQuandoRoleNula() {
        assertThatThrownBy(() -> UserRole.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role não pode ser nulo ou vazio");
    }

    @Test
    void deveLancarExcecaoQuandoRoleVazia() {
        assertThatThrownBy(() -> UserRole.fromString(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role não pode ser nulo ou vazio");
    }

    @Test
    void deveVerificarSeEhMecanicoOuSuperior() {
        assertThat(UserRole.CLIENTE.isMecanicoOrHigher()).isFalse();
        assertThat(UserRole.MECANICO.isMecanicoOrHigher()).isTrue();
        assertThat(UserRole.ADMIN.isMecanicoOrHigher()).isTrue();
    }

    @Test
    void deveVerificarSeEhCliente() {
        assertThat(UserRole.CLIENTE.isCliente()).isTrue();
        assertThat(UserRole.MECANICO.isCliente()).isFalse();
        assertThat(UserRole.ADMIN.isCliente()).isFalse();
    }
}
