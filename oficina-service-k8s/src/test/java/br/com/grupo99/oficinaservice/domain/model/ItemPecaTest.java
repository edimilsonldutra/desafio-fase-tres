package br.com.grupo99.oficinaservice.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Teste Unitário da Entidade ItemPeca")
class ItemPecaTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private Peca peca;

    @BeforeEach
    void setUp() {
        peca = new Peca("Pastilha de Freio", "Fabricante Teste", new BigDecimal("90.50"), 10);
    }

    @Test
    @DisplayName("Deve criar um item de peça com construtor parametrizado e valores válidos")
    void deveCriarItemPecaComConstrutorParametrizado() {
        ItemPeca itemPeca = new ItemPeca(peca, 2);
        assertNotNull(itemPeca);
        assertEquals(peca, itemPeca.getPeca());
        assertEquals(2, itemPeca.getQuantidade());
        assertEquals(peca.getPreco(), itemPeca.getValorUnitario());
        assertEquals(peca.getPreco().multiply(BigDecimal.valueOf(2)), itemPeca.getValorTotal());
    }

    @Test
    @DisplayName("Deve calcular valor total corretamente com múltiplas quantidades")
    void deveCalcularValorTotalComMultiplasQuantidades() {
        ItemPeca itemPeca = new ItemPeca(peca, 5);

        assertEquals(5, itemPeca.getQuantidade());
        assertEquals(new BigDecimal("90.50"), itemPeca.getValorUnitario());
        assertEquals(new BigDecimal("452.50"), itemPeca.getValorTotal());
    }

    @Test
    @DisplayName("Deve definir e obter todos os campos através dos setters e getters")
    void deveDefinirEObterTodosOsCampos() {
        ItemPeca itemPeca = new ItemPeca(peca, 2);
        UUID id = UUID.randomUUID();
        BigDecimal valorUnitario = new BigDecimal("100.00");
        BigDecimal valorTotal = new BigDecimal("300.00");

        itemPeca.setId(id);
        itemPeca.setPeca(peca);
        itemPeca.setQuantidade(3);
        itemPeca.setValorUnitario(valorUnitario);
        itemPeca.setValorTotal(valorTotal);

        assertEquals(id, itemPeca.getId());
        assertEquals(peca, itemPeca.getPeca());
        assertEquals(3, itemPeca.getQuantidade());
        assertEquals(valorUnitario, itemPeca.getValorUnitario());
        assertEquals(valorTotal, itemPeca.getValorTotal());
    }

    @Test
    @DisplayName("Deve verificar igualdade baseada no ID")
    void deveVerificarIgualdadeBaseadaNoId() {
        UUID id = UUID.randomUUID();

        ItemPeca itemPeca1 = new ItemPeca(peca, 2);
        itemPeca1.setId(id);

        ItemPeca itemPeca2 = new ItemPeca(peca, 2);
        itemPeca2.setId(id);

        ItemPeca itemPeca3 = new ItemPeca(peca, 2);
        itemPeca3.setId(UUID.randomUUID());

        // Testa igualdade reflexiva
        assertEquals(itemPeca1, itemPeca1);

        // Testa igualdade simétrica
        assertEquals(itemPeca1, itemPeca2);
        assertEquals(itemPeca2, itemPeca1);

        // Testa desigualdade
        assertNotEquals(itemPeca1, itemPeca3);
        assertNotEquals(itemPeca1, null);
        assertNotEquals(itemPeca1, "string");
    }

    @Test
    @DisplayName("Deve verificar hashCode baseado no ID")
    void deveVerificarHashCodeBaseadoNoId() {
        UUID id = UUID.randomUUID();

        ItemPeca itemPeca1 = new ItemPeca(peca, 2);
        itemPeca1.setId(id);

        ItemPeca itemPeca2 = new ItemPeca(peca, 2);
        itemPeca2.setId(id);

        assertEquals(itemPeca1.hashCode(), itemPeca2.hashCode());
    }

    @Test
    @DisplayName("Deve lidar com ID nulo em equals e hashCode")
    void deveLidarComIdNuloEmEqualsEHashCode() {
        ItemPeca itemPeca1 = new ItemPeca(peca, 2);
        ItemPeca itemPeca2 = new ItemPeca(peca, 2);

        assertEquals(itemPeca1, itemPeca2);
        assertEquals(itemPeca1.hashCode(), itemPeca2.hashCode());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar item de peça com peça nula")
    void deveLancarExcecaoPecaNula() {
        assertThrows(IllegalArgumentException.class, () -> new ItemPeca(null, 2));
    }

    @Test
    @DisplayName("Deve atualizar quantidade e recalcular valor total")
    void deveAtualizarQuantidadeERecalcularTotal() {
        ItemPeca itemPeca = new ItemPeca(peca, 2);
        itemPeca.setQuantidade(3);
        assertEquals(3, itemPeca.getQuantidade());
        assertEquals(new BigDecimal("271.50"), itemPeca.getValorTotal());
    }

    @Test
    @DisplayName("Deve validar equals e hashCode")
    void deveValidarEqualsHashCode() {
        ItemPeca i1 = new ItemPeca(peca, 2);
        ItemPeca i2 = new ItemPeca(peca, 2);
        assertEquals(i1, i2);
        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    @DisplayName("Deve validar toString")
    void deveValidarToString() {
        ItemPeca itemPeca = new ItemPeca(peca, 2);
        String str = itemPeca.toString();
        assertTrue(str.contains("Pastilha de Freio"));
        assertTrue(str.contains("90.50"));
    }

    @Test
    @DisplayName("Dado um item de peça criado, quando consultar os dados, então deve retornar os valores corretos")
    void dadoItemPecaCriado_quandoConsultarDados_entaoRetornaCorreto() {
        // Given
        ItemPeca itemPeca = new ItemPeca(peca, 2);
        // When
        Peca pecaObtida = itemPeca.getPeca();
        int quantidade = itemPeca.getQuantidade();
        BigDecimal valorUnitario = itemPeca.getValorUnitario();
        BigDecimal valorTotal = itemPeca.getValorTotal();
        // Then
        assertEquals(peca, pecaObtida);
        assertEquals(2, quantidade);
        assertEquals(new BigDecimal("90.50"), valorUnitario);
        assertEquals(new BigDecimal("181.00"), valorTotal);
    }

    @Test
    @DisplayName("Dado um item de peça, quando atualizar quantidade, então o valor total deve ser recalculado")
    void dadoItemPeca_quandoAtualizarQuantidade_entaoRecalculaTotal() {
        // Given
        ItemPeca itemPeca = new ItemPeca(peca, 2);
        // When
        itemPeca.setQuantidade(4);
        // Then
        assertEquals(4, itemPeca.getQuantidade());
        assertEquals(new BigDecimal("362.00"), itemPeca.getValorTotal());
    }

    @Test
    @DisplayName("Dado um item de peça, quando criar com peça nula, então deve lançar exceção")
    void dadoItemPeca_quandoPecaNula_entaoLancaExcecao() {
        // Given/When/Then
        assertThrows(IllegalArgumentException.class, () -> new ItemPeca(null, 2));
    }
}
