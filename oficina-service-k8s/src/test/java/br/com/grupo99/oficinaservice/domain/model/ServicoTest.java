package br.com.grupo99.oficinaservice.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Teste Unitário da Entidade Serviço")
class ServicoTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Test
    @DisplayName("Deve criar um serviço com descrição e preço")
    void deveCriarServico() {
        Servico servico = new Servico("Alinhamento e Balanceamento", new BigDecimal("120.00"));
        assertNotNull(servico);
        assertEquals("Alinhamento e Balanceamento", servico.getDescricao());
        assertEquals(new BigDecimal("120.00"), servico.getPreco());
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar serviço com descrição nula")
    void deveLancarExcecaoDescricaoNula() {
        assertThrows(IllegalArgumentException.class, () -> new Servico(null, new BigDecimal("120.00")));
    }

    @Test
    @DisplayName("Deve atualizar dados do serviço")
    void deveAtualizarDadosServico() {
        Servico servico = new Servico("Troca de Óleo", new BigDecimal("250.00"));
        servico.setDescricao("Balanceamento");
        servico.setPreco(new BigDecimal("180.00"));
        assertEquals("Balanceamento", servico.getDescricao());
        assertEquals(new BigDecimal("180.00"), servico.getPreco());
    }

    @Test
    @DisplayName("Deve validar equals e hashCode")
    void deveValidarEqualsHashCode() {
        Servico s1 = new Servico("Serviço Teste", new BigDecimal("120.00"));
        Servico s2 = new Servico("Serviço Teste", new BigDecimal("120.00"));
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    @DisplayName("Deve validar toString")
    void deveValidarToString() {
        Servico servico = new Servico("Serviço Teste", new BigDecimal("120.00"));
        String str = servico.toString();
        assertTrue(str.contains("Serviço Teste"));
        assertTrue(str.contains("120.00"));
    }

    @Test
    @DisplayName("Dado um serviço criado, quando consultar os dados, então deve retornar os valores corretos")
    void dadoServicoCriado_quandoConsultarDados_entaoRetornaCorreto() {
        // Given
        Servico servico = new Servico("Balanceamento", new BigDecimal("80.00"));
        // When
        String descricao = servico.getDescricao();
        BigDecimal preco = servico.getPreco();
        // Then
        assertEquals("Balanceamento", descricao);
        assertEquals(new BigDecimal("80.00"), preco);
    }

    @Test
    @DisplayName("Dado um serviço, quando atualizar descrição/preço, então os dados devem ser atualizados")
    void dadoServico_quandoAtualizarDados_entaoAtualiza() {
        // Given
        Servico servico = new Servico("Revisão Geral", new BigDecimal("200.00"));
        // When
        servico.setDescricao("Revisão Geral");
        servico.setPreco(new BigDecimal("200.00"));
        // Then
        assertEquals("Revisão Geral", servico.getDescricao());
        assertEquals(new BigDecimal("200.00"), servico.getPreco());
    }

    @Test
    @DisplayName("Dado um serviço, quando criar com descrição nula, então deve lançar exceção")
    void dadoServico_quandoDescricaoNula_entaoLancaExcecao() {
        // Given/When/Then
        assertThrows(IllegalArgumentException.class, () -> new Servico(null, new BigDecimal("120.00")));
    }
}
