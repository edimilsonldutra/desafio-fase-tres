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

@DisplayName("Teste Unitário da Entidade ItemServico")
class ItemServicoTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private Servico servico;

    @BeforeEach
    void setUp() {
        servico = new Servico("Troca de Óleo", new BigDecimal("250.00"));
    }

    @Test
    @DisplayName("Deve criar um item de serviço com construtor parametrizado e valores válidos")
    void deveCriarItemServicoComConstrutorParametrizado() {
        ItemServico itemServico = new ItemServico(servico, 3);
        assertNotNull(itemServico);
        assertEquals(servico, itemServico.getServico());
        assertEquals(3, itemServico.getQuantidade());
        assertEquals(servico.getPreco(), itemServico.getValorUnitario());
        assertEquals(servico.getPreco().multiply(BigDecimal.valueOf(3)), itemServico.getValorTotal());
    }

    @Test
    @DisplayName("Deve calcular valor total corretamente com múltiplas quantidades")
    void deveCalcularValorTotalComMultiplasQuantidades() {
        ItemServico itemServico = new ItemServico(servico, 3);

        assertEquals(3, itemServico.getQuantidade());
        assertEquals(new BigDecimal("250.00"), itemServico.getValorUnitario());
        assertEquals(new BigDecimal("750.00"), itemServico.getValorTotal());
    }

    @Test
    @DisplayName("Deve definir e obter todos os campos através dos setters e getters")
    void deveDefinirEObterTodosOsCampos() {
        ItemServico itemServico = new ItemServico();
        UUID id = UUID.randomUUID();
        BigDecimal valorUnitario = new BigDecimal("100.00");
        BigDecimal valorTotal = new BigDecimal("300.00");

        itemServico.setId(id);
        itemServico.setServico(servico);
        itemServico.setValorUnitario(valorUnitario);
        itemServico.setQuantidade(3);
        itemServico.setValorTotal(valorTotal);

        assertEquals(id, itemServico.getId());
        assertEquals(servico, itemServico.getServico());
        assertEquals(3, itemServico.getQuantidade());
        assertEquals(valorUnitario, itemServico.getValorUnitario());
        assertEquals(valorTotal, itemServico.getValorTotal());
    }

    @Test
    @DisplayName("Deve verificar igualdade baseada no ID")
    void deveVerificarIgualdadeBaseadaNoId() {
        UUID id = UUID.randomUUID();
        
        ItemServico itemServico1 = new ItemServico();
        itemServico1.setId(id);
        
        ItemServico itemServico2 = new ItemServico();
        itemServico2.setId(id);
        
        ItemServico itemServico3 = new ItemServico();
        itemServico3.setId(UUID.randomUUID());

        // Testa igualdade reflexiva
        assertEquals(itemServico1, itemServico1);
        
        // Testa igualdade simétrica
        assertEquals(itemServico1, itemServico2);
        assertEquals(itemServico2, itemServico1);
        
        // Testa desigualdade
        assertNotEquals(itemServico1, itemServico3);
        assertNotEquals(itemServico1, null);
        assertNotEquals(itemServico1, "string");
    }

    @Test
    @DisplayName("Deve verificar hashCode baseado no ID")
    void deveVerificarHashCodeBaseadoNoId() {
        UUID id = UUID.randomUUID();
        
        ItemServico itemServico1 = new ItemServico();
        itemServico1.setId(id);
        
        ItemServico itemServico2 = new ItemServico();
        itemServico2.setId(id);

        assertEquals(itemServico1.hashCode(), itemServico2.hashCode());
    }

    @Test
    @DisplayName("Deve lidar com ID nulo em equals e hashCode")
    void deveLidarComIdNuloEmEqualsEHashCode() {
        ItemServico itemServico1 = new ItemServico();
        ItemServico itemServico2 = new ItemServico();

        assertEquals(itemServico1, itemServico2);
        assertEquals(itemServico1.hashCode(), itemServico2.hashCode());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar item de serviço com serviço nulo")
    void deveLancarExcecaoServicoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new ItemServico(null, 1));
    }

    @Test
    @DisplayName("Deve atualizar quantidade e recalcular valor total")
    void deveAtualizarQuantidadeERecalcularTotal() {
        ItemServico itemServico = new ItemServico(servico, 1);
        itemServico.setQuantidade(3);
        assertEquals(3, itemServico.getQuantidade());
        assertEquals(new BigDecimal("750.00"), itemServico.getValorTotal());
    }

    @Test
    @DisplayName("Deve validar equals e hashCode")
    void deveValidarEqualsHashCode() {
        ItemServico i1 = new ItemServico(servico, 1);
        ItemServico i2 = new ItemServico(servico, 1);
        assertEquals(i1, i2);
        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    @DisplayName("Deve validar toString")
    void deveValidarToString() {
        ItemServico itemServico = new ItemServico(servico, 1);
        String str = itemServico.toString();
        assertTrue(str.contains("Troca de Óleo"));
        assertTrue(str.contains("250.00"));
    }

    @Test
    @DisplayName("Dado um item de serviço criado, quando consultar os dados, então deve retornar os valores corretos")
    void dadoItemServicoCriado_quandoConsultarDados_entaoRetornaCorreto() {
        // Given
        ItemServico itemServico = new ItemServico(servico, 1);
        // When
        Servico servicoObtido = itemServico.getServico();
        int quantidade = itemServico.getQuantidade();
        BigDecimal valorUnitario = itemServico.getValorUnitario();
        BigDecimal valorTotal = itemServico.getValorTotal();
        // Then
        assertEquals(servico, servicoObtido);
        assertEquals(1, quantidade);
        assertEquals(new BigDecimal("250.00"), valorUnitario);
        assertEquals(new BigDecimal("250.00"), valorTotal);
    }

    @Test
    @DisplayName("Dado um item de serviço, quando atualizar quantidade, então o valor total deve ser recalculado")
    void dadoItemServico_quandoAtualizarQuantidade_entaoRecalculaTotal() {
        // Given
        ItemServico itemServico = new ItemServico(servico, 1);
        // When
        itemServico.setQuantidade(3);
        // Then
        assertEquals(3, itemServico.getQuantidade());
        assertEquals(new BigDecimal("750.00"), itemServico.getValorTotal());
    }

    @Test
    @DisplayName("Dado um item de serviço, quando criar com serviço nulo, então deve lançar exceção")
    void dadoItemServico_quandoServicoNulo_entaoLancaExcecao() {
        // Given/When/Then
        assertThrows(IllegalArgumentException.class, () -> new ItemServico(null, 1));
    }
}
