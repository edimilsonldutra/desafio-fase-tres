package br.com.grupo99.oficinaservice.domain.service;

import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.Peca;
import br.com.grupo99.oficinaservice.domain.model.Servico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@DisplayName("Teste Unitário - OrcamentoService")
class OrcamentoServiceTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    private OrcamentoService orcamentoService;

    @BeforeEach
    void setUp() {
        orcamentoService = new OrcamentoService();
    }

    @Test
    @DisplayName("Dado uma Ordem de Serviço com peças e serviços, Quando calcular o valor total, Então deve retornar a soma correta")
    void givenOrdemServicoWithItems_whenCalcularValorTotal_thenShouldReturnCorrectSum() {
        // Given (Dado)
        OrdemServico os = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());

        Peca peca1 = new Peca();
        peca1.setPreco(new BigDecimal("100.50"));
        peca1.setEstoque(10); // Estoque suficiente

        Servico servico1 = new Servico("Serviço Teste", new BigDecimal("100.00"));

        os.adicionarPeca(peca1, 1);
        os.adicionarServico(servico1, 1);

        // When (Quando)
        BigDecimal valorTotal = orcamentoService.calcularValorTotal(os);

        // Then (Então)
        assertEquals(new BigDecimal("200.50"), valorTotal);
    }

    @Test
    @DisplayName("Dado uma Ordem de Serviço sem itens, Quando calcular o valor total, Então deve retornar zero")
    void givenOrdemServicoWithoutItems_whenCalcularValorTotal_thenShouldReturnZero() {
        // Given (Dado)
        OrdemServico os = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());

        // When (Quando)
        BigDecimal valorTotal = orcamentoService.calcularValorTotal(os);

        // Then (Então)
        assertEquals(BigDecimal.ZERO, valorTotal);
    }

    @Test
    @DisplayName("Dado uma Ordem de Serviço nula, Quando calcular o valor total, Então deve retornar zero")
    void givenNullOrdemServico_whenCalcularValorTotal_thenShouldReturnZero() {
        // When (Quando)
        BigDecimal valorTotal = orcamentoService.calcularValorTotal(null);

        // Then (Então)
        assertEquals(BigDecimal.ZERO, valorTotal);
    }
}