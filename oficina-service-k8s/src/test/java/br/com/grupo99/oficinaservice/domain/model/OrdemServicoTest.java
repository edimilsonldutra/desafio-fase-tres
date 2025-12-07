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

@DisplayName("Teste Unitário do Agregado OrdemServico")
class OrdemServicoTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private Cliente cliente;
    private Veiculo veiculo;
    private OrdemServico os;

    @BeforeEach
    void setUp() {
        // Prepara os objetos de teste
        Pessoa pessoa = new Pessoa("12345678901", TipoPessoa.FISICA, "Cliente Teste", "clienteteste@email.com", Perfil.CLIENTE);
        cliente = new Cliente(pessoa);
        cliente.setId(UUID.randomUUID()); // Simula um ID que viria da base de dados

        veiculo = new Veiculo("ABC-1234", "Marca Teste", "Modelo Teste", 2023);
        veiculo.setId(UUID.randomUUID()); // Simula um ID que viria da base de dados

        // Cria a Ordem de Serviço com os IDs, conforme o novo modelo
        os = new OrdemServico(cliente.getId(), veiculo.getId());
    }

    @Test
    @DisplayName("Deve criar uma Ordem de Serviço com os IDs e status corretos")
    void deveCriarOrdemServicoComStatusInicialCorreto() {
        assertNotNull(os);
        assertEquals(StatusOS.RECEBIDA, os.getStatus());
        assertEquals(cliente.getId(), os.getClienteId());
        assertEquals(veiculo.getId(), os.getVeiculoId());
        assertEquals(BigDecimal.ZERO, os.getValorTotal());
    }

    @Test
    @DisplayName("Deve adicionar peça à lista e baixar o estoque, atualizando o valor total")
    void deveAdicionarPecaEBaixarEstoque() {
        Peca peca = new Peca();
        peca.setNome("Filtro de Óleo");
        peca.setPreco(new BigDecimal("50.00"));
        peca.setEstoque(10);
        os.adicionarPeca(peca, 2);
        assertEquals(1, os.getPecas().size());
        assertEquals(8, peca.getEstoque());
        // Agora o valor total é atualizado automaticamente
        assertEquals(new BigDecimal("100.00"), os.getValorTotal());
    }

    @Test
    @DisplayName("Deve transitar corretamente pelos status até ser FINALIZADA")
    void deveTransitarStatusCorretamente() {
        assertEquals(StatusOS.RECEBIDA, os.getStatus());

        os.iniciarDiagnostico();
        assertEquals(StatusOS.EM_DIAGNOSTICO, os.getStatus());

        os.aguardarAprovacao();
        assertEquals(StatusOS.AGUARDANDO_APROVACAO, os.getStatus());

        os.aprovar();
        assertEquals(StatusOS.EM_EXECUCAO, os.getStatus());

        os.finalizar();
        assertEquals(StatusOS.FINALIZADA, os.getStatus());
        assertNotNull(os.getDataFinalizacao());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar aprovar uma OS que não está aguardando aprovação")
    void deveLancarExcecaoAoAprovarOsEmStatusIncorreto() {
        assertEquals(StatusOS.RECEBIDA, os.getStatus());
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> os.aprovar());
        assertEquals("OS não pode ser aprovada pois não está aguardando aprovação", exception.getMessage());
    }

    @Test
    @DisplayName("Deve adicionar serviço e atualizar valor total")
    void deveAdicionarServicoEAtualizarValorTotal() {
        Servico servico = new Servico("Serviço Teste", new BigDecimal("100.00"));
        os.adicionarServico(servico, 2);
        assertEquals(1, os.getServicos().size());
        assertEquals(new BigDecimal("200.00"), os.getValorTotal());
    }

    @Test
    @DisplayName("Deve finalizar ordem de serviço e alterar status")
    void deveFinalizarOrdemServico() {
        os.iniciarDiagnostico();
        os.aguardarAprovacao();
        os.aprovar();
        os.finalizar();
        assertEquals(StatusOS.FINALIZADA, os.getStatus());
        assertNotNull(os.getDataFinalizacao());
    }

    @Test
    @DisplayName("Não deve finalizar ordem já finalizada")
    void naoDeveFinalizarOrdemJaFinalizada() {
        os.iniciarDiagnostico();
        os.aguardarAprovacao();
        os.aprovar();
        os.finalizar();
        assertThrows(IllegalStateException.class, os::finalizar);
    }

    @Test
    @DisplayName("Deve remover peça e atualizar valor total")
    void deveRemoverPecaEAtualizarValorTotal() {
        Peca peca = new Peca();
        peca.setNome("Filtro de Ar");
        peca.setPreco(new BigDecimal("30.00"));
        peca.setEstoque(5);
        os.adicionarPeca(peca, 2);
        assertEquals(new BigDecimal("60.00"), os.getValorTotal());
        // Remover pelo id do item
        ItemPeca item = os.getPecas().getFirst();
        os.removerPeca(item.getId());
        assertEquals(BigDecimal.ZERO, os.getValorTotal());
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar peça nula")
    void deveLancarExcecaoAdicionarPecaNula() {
        assertThrows(IllegalArgumentException.class, () -> os.adicionarPeca(null, 1));
    }

    @Test
    @DisplayName("Deve lançar exceção ao adicionar serviço nulo")
    void deveLancarExcecaoAdicionarServicoNulo() {
        assertThrows(IllegalArgumentException.class, () -> os.adicionarServico(null, 1));
    }

    @Test
    @DisplayName("Deve lançar exceção ao finalizar OS em status incorreto")
    void deveLancarExcecaoFinalizarStatusIncorreto() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> os.finalizar());
        assertEquals("OS não pode ser finalizada pois não está em execução", exception.getMessage());
    }

    @Test
    @DisplayName("Deve validar equals e hashCode")
    void deveValidarEqualsHashCode() {
        OrdemServico os1 = new OrdemServico(cliente.getId(), veiculo.getId());
        OrdemServico os2 = new OrdemServico(cliente.getId(), veiculo.getId());
        assertEquals(os1, os2);
        assertEquals(os1.hashCode(), os2.hashCode());
    }

    @Test
    @DisplayName("Deve validar toString")
    void deveValidarToString() {
        String str = os.toString();
        assertTrue(str.contains("OrdemServico"));
        assertTrue(str.contains(cliente.getId().toString()));
        assertTrue(str.contains(veiculo.getId().toString()));
    }

    @Test
    @DisplayName("Dado uma OS sem itens, quando consultar o valor total, então deve ser zero")
    void dadoOsSemItens_quandoConsultarValorTotal_entaoDeveSerZero() {
        // Given
        // Ordem de serviço criada no setUp sem itens
        // When
        BigDecimal valorTotal = os.getValorTotal();
        // Then
        assertEquals(BigDecimal.ZERO, valorTotal);
    }

    @Test
    @DisplayName("Dado OS com múltiplos itens, quando adicionar itens, então valor total correto")
    void dadoOsComMultiplosItens_quandoAdicionarItens_entaoValorTotalCorreto() {
        Servico servico1 = new Servico("Serviço 1", new BigDecimal("50.00"));
        Servico servico2 = new Servico("Serviço 2", new BigDecimal("30.00"));
        os.adicionarServico(servico1, 2); // 100
        os.adicionarServico(servico2, 1); // 30
        Peca peca = new Peca("Filtro", "Marca", new BigDecimal("20.00"), 10);
        os.adicionarPeca(peca, 3); // 60
        assertEquals(new BigDecimal("190.00"), os.getValorTotal());
    }

    @Test
    @DisplayName("Dado uma OS, quando tentar remover uma peça inexistente, então deve lançar exceção")
    void dadoOs_quandoRemoverPecaInexistente_entaoLancaExcecao() {
        // Given
        UUID idInexistente = UUID.randomUUID();
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> os.removerPeca(idInexistente));
    }

    @Test
    @DisplayName("Dado uma OS, quando tentar transitar para status inválido, então deve lançar exceção")
    void dadoOs_quandoTransitarStatusInvalido_entaoLancaExcecao() {
        // Given
        // Status inicial RECEBIDA
        // When & Then
        assertThrows(IllegalStateException.class, () -> os.finalizar());
    }
}
