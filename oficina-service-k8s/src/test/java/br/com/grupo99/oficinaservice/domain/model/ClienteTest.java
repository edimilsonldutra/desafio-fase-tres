package br.com.grupo99.oficinaservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Teste Unitário da Entidade Cliente")
class ClienteTest {

    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, "teste@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }

    @Test
    @DisplayName("Deve criar um cliente com nome e CPF/CNPJ")
    void deveCriarCliente() {
        Cliente cliente = criarClienteComPessoa("João da Silva", "12345678900");
        assertNotNull(cliente);
        assertEquals("João da Silva", cliente.getPessoa().getName());
        assertEquals("12345678900", cliente.getPessoa().getNumeroDocumento());
        assertTrue(cliente.getVeiculos().isEmpty());
    }

    @Test
    @DisplayName("Deve adicionar um veículo à lista de veículos do cliente")
    void deveAdicionarVeiculo() {
        Cliente cliente = criarClienteComPessoa("Maria Oliveira", "98765432100");
        Veiculo veiculo = new Veiculo("ABC-1234", "Fiat", "Uno", 2010);

        // Em uma associação bidirecional, é importante setar os dois lados
        cliente.getVeiculos().add(veiculo);
        veiculo.setCliente(cliente);

        assertEquals(1, cliente.getVeiculos().size());
        assertEquals(veiculo, cliente.getVeiculos().get(0));
        assertEquals(cliente, veiculo.getCliente());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com pessoa nula")
    void deveLancarExcecaoPessoaNula() {
        assertThrows(IllegalArgumentException.class, () -> new Cliente(null));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com pessoa de perfil incorreto")
    void deveLancarExcecaoPerfilIncorreto() {
        Pessoa pessoa = new Pessoa("12345678900", TipoPessoa.FISICA, "João", "joao@email.com", Perfil.ADMIN);
        assertThrows(IllegalArgumentException.class, () -> new Cliente(pessoa));
    }

    @Test
    @DisplayName("Deve atualizar dados da pessoa do cliente")
    void deveAtualizarDadosCliente() {
        Cliente cliente = criarClienteComPessoa("João da Silva", "12345678900");
        cliente.getPessoa().setPhone("11999999999");
        cliente.getPessoa().setEmail("joao.novo@email.com");
        assertEquals("11999999999", cliente.getPessoa().getPhone());
        assertEquals("joao.novo@email.com", cliente.getPessoa().getEmail());
    }

    @Test
    @DisplayName("Deve remover veículo da lista de veículos do cliente")
    void deveRemoverVeiculo() {
        Cliente cliente = criarClienteComPessoa("Maria Oliveira", "98765432100");
        Veiculo veiculo = new Veiculo("ABC-1234", "Fiat", "Uno", 2010);
        cliente.getVeiculos().add(veiculo);
        cliente.getVeiculos().remove(veiculo);
        assertTrue(cliente.getVeiculos().isEmpty());
    }

    @Test
    @DisplayName("Deve validar equals e hashCode")
    void deveValidarEqualsHashCode() {
        Cliente c1 = criarClienteComPessoa("João da Silva", "12345678900");
        Cliente c2 = criarClienteComPessoa("João da Silva", "12345678900");
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    @DisplayName("Deve validar toString")
    void deveValidarToString() {
        Cliente cliente = criarClienteComPessoa("João da Silva", "12345678900");
        String str = cliente.toString();
        assertTrue(str.contains("João da Silva"));
    }

    @Test
    @DisplayName("Dado um cliente criado, quando consultar os dados, então deve retornar os valores corretos")
    void dadoClienteCriado_quandoConsultarDados_entaoRetornaCorreto() {
        // Given
        Cliente cliente = criarClienteComPessoa("Ana Paula", "11122233344");
        // When
        String nome = cliente.getPessoa().getName();
        String documento = cliente.getPessoa().getNumeroDocumento();
        // Then
        assertEquals("Ana Paula", nome);
        assertEquals("11122233344", documento);
    }

    @Test
    @DisplayName("Dado um cliente, quando adicionar um veículo, então a lista de veículos deve ser atualizada")
    void dadoCliente_quandoAdicionarVeiculo_entaoListaAtualizada() {
        // Given
        Cliente cliente = criarClienteComPessoa("Carlos", "22233344455");
        Veiculo veiculo = new Veiculo("DEF-5678", "VW", "Gol", 2015);
        // When
        cliente.getVeiculos().add(veiculo);
        veiculo.setCliente(cliente);
        // Then
        assertEquals(1, cliente.getVeiculos().size());
        assertEquals(veiculo, cliente.getVeiculos().getFirst());
    }

    @Test
    @DisplayName("Dado um cliente, quando remover um veículo, então a lista deve ficar vazia")
    void dadoCliente_quandoRemoverVeiculo_entaoListaVazia() {
        // Given
        Cliente cliente = criarClienteComPessoa("Lucas", "33344455566");
        Veiculo veiculo = new Veiculo("GHI-9012", "Ford", "Ka", 2018);
        cliente.getVeiculos().add(veiculo);
        // When
        cliente.getVeiculos().remove(veiculo);
        // Then
        assertTrue(cliente.getVeiculos().isEmpty());
    }

    @Test
    @DisplayName("Dado um cliente, quando atualizar telefone/email, então os dados devem ser atualizados")
    void dadoCliente_quandoAtualizarDados_entaoAtualiza() {
        // Given
        Cliente cliente = criarClienteComPessoa("Bruna", "44455566677");
        // When
        cliente.getPessoa().setPhone("11988887777");
        cliente.getPessoa().setEmail("bruna.nova@email.com");
        // Then
        assertEquals("11988887777", cliente.getPessoa().getPhone());
        assertEquals("bruna.nova@email.com", cliente.getPessoa().getEmail());
    }

    @Test
    @DisplayName("Dado um cliente, quando criar com pessoa nula, então deve lançar exceção")
    void dadoCliente_quandoPessoaNula_entaoLancaExcecao() {
        // Given/When/Then
        assertThrows(IllegalArgumentException.class, () -> new Cliente(null));
    }

    @Test
    @DisplayName("Dado um cliente, quando criar com perfil incorreto, então deve lançar exceção")
    void dadoCliente_quandoPerfilIncorreto_entaoLancaExcecao() {
        // Given
        Pessoa pessoa = new Pessoa("12345678900", TipoPessoa.FISICA, "João", "joao@email.com", Perfil.MECANICO);
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new Cliente(pessoa));
    }
}
