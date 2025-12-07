package br.com.grupo99.oficinaservice.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Teste Unitário da Entidade Peça")
class PecaTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private Peca peca;

    @BeforeEach
    void setUp() {
        peca = new Peca("Vela de Ignição", "NGK", new BigDecimal("45.00"), 10);
    }

    @Test
    @DisplayName("Deve adicionar quantidade ao estoque com sucesso")
    void deveAdicionarEstoque() {
        peca.adicionarEstoque(5);
        assertEquals(15, peca.getEstoque());
    }

    @Test
    @DisplayName("Deve baixar quantidade do estoque com sucesso")
    void deveBaixarEstoque() {
        peca.baixarEstoque(3);
        assertEquals(7, peca.getEstoque());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar baixar mais do que o estoque disponível")
    void deveLancarExcecaoPorEstoqueInsuficiente() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            peca.baixarEstoque(11);
        });
        assertEquals("Estoque insuficiente.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar peça com nome nulo")
    void deveLancarExcecaoNomeNulo() {
        Peca peca = new Peca();
        assertThrows(IllegalArgumentException.class, () -> peca.setNome(null));
    }

    @Test
    @DisplayName("Deve atualizar dados da peça")
    void deveAtualizarDadosPeca() {
        peca.setFabricante("Bosch");
        peca.setPreco(new BigDecimal("99.99"));
        assertEquals("Bosch", peca.getFabricante());
        assertEquals(new BigDecimal("99.99"), peca.getPreco());
    }

    @Test
    @DisplayName("Deve validar equals e hashCode")
    void deveValidarEqualsHashCode() {
        Peca p1 = new Peca();
        p1.setNome("Vela de Ignição");
        p1.setFabricante("NGK");
        p1.setPreco(new BigDecimal("45.00"));
        p1.setEstoque(10);
        Peca p2 = new Peca();
        p2.setNome("Vela de Ignição");
        p2.setFabricante("NGK");
        p2.setPreco(new BigDecimal("45.00"));
        p2.setEstoque(10);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    @DisplayName("Deve validar toString")
    void deveValidarToString() {
        String str = peca.toString();
        assertTrue(str.contains("Vela de Ignição"));
        assertTrue(str.contains("NGK"));
    }

    @Test
    @DisplayName("Dado uma peça criada, quando consultar os dados, então deve retornar os valores corretos")
    void dadoPecaCriada_quandoConsultarDados_entaoRetornaCorreto() {
        // Given
        Peca peca = new Peca();
        peca.setNome("Amortecedor");
        peca.setFabricante("Monroe");
        peca.setPreco(new BigDecimal("150.00"));
        peca.setEstoque(20);
        // When
        String nome = peca.getNome();
        String fabricante = peca.getFabricante();
        BigDecimal preco = peca.getPreco();
        int estoque = peca.getEstoque();
        // Then
        assertEquals("Amortecedor", nome);
        assertEquals("Monroe", fabricante);
        assertEquals(new BigDecimal("150.00"), preco);
        assertEquals(20, estoque);
    }

    @Test
    @DisplayName("Dado uma peça, quando adicionar estoque, então o valor do estoque deve ser atualizado")
    void dadoPeca_quandoAdicionarEstoque_entaoAtualiza() {
        // Given
        Peca peca = new Peca();
        peca.setEstoque(5);
        // When
        peca.adicionarEstoque(10);
        // Then
        assertEquals(15, peca.getEstoque());
    }

    @Test
    @DisplayName("Dado uma peça, quando baixar estoque, então o valor do estoque deve ser atualizado")
    void dadoPeca_quandoBaixarEstoque_entaoAtualiza() {
        // Given
        Peca peca = new Peca();
        peca.setEstoque(8);
        // When
        peca.baixarEstoque(3);
        // Then
        assertEquals(5, peca.getEstoque());
    }

    @Test
    @DisplayName("Dado uma peça, quando baixar mais do que o estoque disponível, então deve lançar exceção")
    void dadoPeca_quandoBaixarEstoqueInsuficiente_entaoLancaExcecao() {
        // Given
        Peca peca = new Peca();
        peca.setEstoque(2);
        // When/Then
        assertThrows(IllegalStateException.class, () -> peca.baixarEstoque(5));
    }

    @Test
    @DisplayName("Dado uma peça, quando atualizar fabricante/preço, então os dados devem ser atualizados")
    void dadoPeca_quandoAtualizarDados_entaoAtualiza() {
        // Given
        Peca peca = new Peca();
        // When
        peca.setFabricante("TRW");
        peca.setPreco(new BigDecimal("99.99"));
        // Then
        assertEquals("TRW", peca.getFabricante());
        assertEquals(new BigDecimal("99.99"), peca.getPreco());
    }

    @Test
    @DisplayName("Dado uma peça, quando criar com nome nulo, então deve lançar exceção")
    void dadoPeca_quandoNomeNulo_entaoLancaExcecao() {
        // Given/When/Then
        Peca peca = new Peca();
        assertThrows(IllegalArgumentException.class, () -> peca.setNome(null));
    }
}
