package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.TempoMedioServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.*;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import br.com.grupo99.oficinaservice.domain.repository.OrdemServicoRepository;
import br.com.grupo99.oficinaservice.domain.repository.ServicoRepository;
import br.com.grupo99.oficinaservice.domain.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Teste de Integração BDD - RelatorioApplicationService")
class RelatorioApplicationServiceIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Autowired private RelatorioApplicationService relatorioService;
    @Autowired private OrdemServicoRepository osRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;

    private Servico servicoAnalisado;
    private Cliente cliente;
    private Veiculo veiculo;

    @BeforeEach
    void setUp() {
        // Cria dados base para os testes
        servicoAnalisado = new Servico("Serviço Teste", new BigDecimal("100.00"));
        servicoAnalisado.setDescricao("Troca de Pneu");
        servicoAnalisado.setPreco(BigDecimal.TEN);
        servicoAnalisado = servicoRepository.save(servicoAnalisado);

        cliente = clienteRepository.save(criarClienteComPessoa("Cliente Relatório", "12312312312"));
        veiculo = new Veiculo("REL-0001", "Marca", "Modelo", 2020);
        veiculo.setCliente(cliente);
        veiculo = veiculoRepository.save(veiculo);
    }

    @Test
    @DisplayName("Dado OS finalizadas para um serviço, Quando calcular tempo médio, Então deve retornar a média correta")
    void givenFinalizedOrders_whenCalculateAverageTime_thenShouldReturnCorrectAverage() {
        // Given (Dado)
        // OS 1: Duração de 60 minutos
        OrdemServico os1 = new OrdemServico(cliente.getId(), veiculo.getId());
        os1.adicionarServico(servicoAnalisado, 1);
        os1.setDataCriacao(LocalDateTime.now().minusHours(2));
        os1.setDataFinalizacao(LocalDateTime.now().minusHours(1)); // 60 min
        os1.setStatus(StatusOS.FINALIZADA);
        osRepository.save(os1);

        // OS 2: Duração de 120 minutos
        OrdemServico os2 = new OrdemServico(cliente.getId(), veiculo.getId());
        os2.adicionarServico(servicoAnalisado, 1);
        os2.setDataCriacao(LocalDateTime.now().minusHours(4));
        os2.setDataFinalizacao(LocalDateTime.now().minusHours(2)); // 120 min
        os2.setStatus(StatusOS.ENTREGUE);
        osRepository.save(os2);

        // Média esperada: (60 + 120) / 2 = 90 minutos

        // When (Quando)
        TempoMedioServicoResponseDTO response = relatorioService.execute(servicoAnalisado.getId());

        // Then (Então)
        assertThat(response).isNotNull();
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(2);
        assertThat(response.tempoMedioFormatado()).isEqualTo("1 horas e 30 minutos");
    }

    @Test
    @DisplayName("Dado que não existem OS para um serviço, Quando calcular tempo médio, Então deve retornar N/A")
    void givenNoOrdersForService_whenCalculateAverageTime_thenShouldReturnNotApplicable() {
        // Given: Nenhum OS criado para o serviço

        // When
        TempoMedioServicoResponseDTO response = relatorioService.execute(servicoAnalisado.getId());

        // Then
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(0);
        assertThat(response.tempoMedioFormatado()).isEqualTo("N/A");
    }

    @Test
    @DisplayName("Dado um ID de serviço inexistente, Quando calcular tempo médio, Então deve lançar exceção")
    void givenNonExistingServiceId_whenCalculateAverageTime_thenShouldThrowException() {
        // Given
        UUID idInexistente = UUID.randomUUID();

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> relatorioService.execute(idInexistente));
    }
}
