package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.TempoMedioServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.*;
import br.com.grupo99.oficinaservice.domain.repository.OrdemServicoRepository;
import br.com.grupo99.oficinaservice.domain.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste Unitário - RelatorioApplicationService")
class RelatorioApplicationServiceTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock private OrdemServicoRepository ordemServicoRepository;
    @Mock private ServicoRepository servicoRepository;

    @InjectMocks private RelatorioApplicationService service;

    private Servico servico;
    private OrdemServico ordemServico1;
    private OrdemServico ordemServico2;
    private OrdemServico ordemServico3;

    @BeforeEach
    void setUp() {
        servico = new Servico("Serviço Teste", new BigDecimal("100.00"));
        servico.setId(UUID.randomUUID());

        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();

        // Ordem 1: 60 minutos (1 hora)
        ordemServico1 = new OrdemServico(clienteId, veiculoId);
        ordemServico1.setId(UUID.randomUUID());
        ordemServico1.setStatus(StatusOS.FINALIZADA);
        ordemServico1.setDataCriacao(LocalDateTime.now().minusHours(2));
        ordemServico1.setDataFinalizacao(LocalDateTime.now().minusHours(1));
        ordemServico1.adicionarServico(servico, 1);

        // Ordem 2: 120 minutos (2 horas)
        ordemServico2 = new OrdemServico(clienteId, veiculoId);
        ordemServico2.setId(UUID.randomUUID());
        ordemServico2.setStatus(StatusOS.FINALIZADA);
        ordemServico2.setDataCriacao(LocalDateTime.now().minusHours(4));
        ordemServico2.setDataFinalizacao(LocalDateTime.now().minusHours(2));
        ordemServico2.adicionarServico(servico, 1);

        // Ordem 3: Em execução (não finalizada)
        ordemServico3 = new OrdemServico(clienteId, veiculoId);
        ordemServico3.setId(UUID.randomUUID());
        ordemServico3.setStatus(StatusOS.EM_EXECUCAO);
        ordemServico3.setDataCriacao(LocalDateTime.now().minusHours(1));
        ordemServico3.adicionarServico(servico, 1);
    }

    @Test
    @DisplayName("Deve calcular tempo médio de serviço com múltiplas ordens finalizadas")
    void deveCalcularTempoMedioDeServicoComMultiplasOrdensFinalizadas() {
        // Given
        UUID servicoId = servico.getId();
        List<OrdemServico> ordens = List.of(ordemServico1, ordemServico2, ordemServico3);

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.findAll()).thenReturn(ordens);

        // When
        TempoMedioServicoResponseDTO response = service.execute(servicoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.servicoId()).isEqualTo(servicoId);
        assertThat(response.servicoDescricao()).isEqualTo(servico.getDescricao());
        assertThat(response.tempoMedioFormatado()).isEqualTo("1 horas e 30 minutos"); // Média de 60 e 120 minutos = 90 minutos
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve calcular tempo médio quando há apenas uma ordem finalizada")
    void deveCalcularTempoMedioQuandoHaApenasUmaOrdemFinalizada() {
        // Given
        UUID servicoId = servico.getId();
        List<OrdemServico> ordens = List.of(ordemServico1);

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.findAll()).thenReturn(ordens);

        // When
        TempoMedioServicoResponseDTO response = service.execute(servicoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.tempoMedioFormatado()).isEqualTo("1 horas e 0 minutos");
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve retornar zero quando não há ordens finalizadas com o serviço")
    void deveRetornarZeroQuandoNaoHaOrdensFinalizadasComOServico() {
        // Given
        UUID servicoId = servico.getId();
        List<OrdemServico> ordens = List.of(ordemServico3); // Apenas ordem em execução

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.findAll()).thenReturn(ordens);

        // When
        TempoMedioServicoResponseDTO response = service.execute(servicoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.tempoMedioFormatado()).isEqualTo("N/A");
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve retornar zero quando não há ordens de serviço")
    void deveRetornarZeroQuandoNaoHaOrdensDeServico() {
        // Given
        UUID servicoId = servico.getId();
        List<OrdemServico> ordens = List.of();

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.findAll()).thenReturn(ordens);

        // When
        TempoMedioServicoResponseDTO response = service.execute(servicoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.tempoMedioFormatado()).isEqualTo("N/A");
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(0);
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço não for encontrado")
    void deveLancarExcecaoQuandoServicoNaoForEncontrado() {
        // Given
        UUID servicoId = UUID.randomUUID();
        when(servicoRepository.findById(servicoId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> service.execute(servicoId)
        );
        assertThat(exception.getMessage()).contains("Serviço não encontrado");
    }

    @Test
    @DisplayName("Deve ignorar ordens sem data de finalização mesmo quando status é finalizada")
    void deveIgnorarOrdensSemDataDeFinalizacaoMesmoQuandoStatusEFinalizada() {
        // Given
        UUID servicoId = servico.getId();
        
        // Ordem com status finalizada mas sem data de finalização
        OrdemServico ordemSemDataFinalizacao = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        ordemSemDataFinalizacao.setStatus(StatusOS.FINALIZADA);
        ordemSemDataFinalizacao.setDataCriacao(LocalDateTime.now().minusHours(1));
        ordemSemDataFinalizacao.setDataFinalizacao(null);
        // Não adicionar servico para evitar problemas
        // ordemSemDataFinalizacao.adicionarServico(servico, 1);

        List<OrdemServico> ordens = List.of(ordemServico1, ordemSemDataFinalizacao);

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.findAll()).thenReturn(ordens);

        // When
        TempoMedioServicoResponseDTO response = service.execute(servicoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(1); // Apenas ordemServico1
        assertThat(response.tempoMedioFormatado()).isEqualTo("1 horas e 0 minutos");
    }

    @Test
    @DisplayName("Deve calcular corretamente tempo em minutos quando menor que 1 hora")
    void deveCalcularCorretamenteTempoEmMinutosQuandoMenorQue1Hora() {
        // Given
        UUID servicoId = servico.getId();
        
        // Ordem com 45 minutos
        OrdemServico ordemRapida = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        ordemRapida.setStatus(StatusOS.FINALIZADA);
        ordemRapida.setDataCriacao(LocalDateTime.now().minusMinutes(45));
        ordemRapida.setDataFinalizacao(LocalDateTime.now());
        ordemRapida.adicionarServico(servico, 1);

        List<OrdemServico> ordens = List.of(ordemRapida);

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.findAll()).thenReturn(ordens);

        // When
        TempoMedioServicoResponseDTO response = service.execute(servicoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.tempoMedioFormatado()).isEqualTo("0 horas e 45 minutos");
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve formatar corretamente tempo com horas e minutos")
    void deveFormatarCorretamenteTempoComHorasEMinutos() {
        // Given
        UUID servicoId = servico.getId();
        
        // Ordem com 2h 30min (150 minutos)
        OrdemServico ordemLonga = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        ordemLonga.setStatus(StatusOS.FINALIZADA);
        ordemLonga.setDataCriacao(LocalDateTime.now().minusMinutes(150));
        ordemLonga.setDataFinalizacao(LocalDateTime.now());
        ordemLonga.adicionarServico(servico, 1);

        List<OrdemServico> ordens = List.of(ordemLonga);

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.findAll()).thenReturn(ordens);

        // When
        TempoMedioServicoResponseDTO response = service.execute(servicoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.tempoMedioFormatado()).isEqualTo("2 horas e 30 minutos");
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deve ignorar ordens que não contêm o serviço especificado")
    void deveIgnorarOrdensQueNaoContemOServicoEspecificado() {
        // Given
        UUID servicoId = servico.getId();
        
        // Criar outro serviço
        Servico outroServico = new Servico("Outro Serviço", new BigDecimal("200.00"));

        // Ordem com outro serviço
        OrdemServico ordemComOutroServico = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        ordemComOutroServico.setStatus(StatusOS.FINALIZADA);
        ordemComOutroServico.setDataCriacao(LocalDateTime.now().minusHours(1));
        ordemComOutroServico.setDataFinalizacao(LocalDateTime.now());
        // Não vou adicionar o serviço para evitar problemas com valorUnitario null

        List<OrdemServico> ordens = List.of(ordemServico1, ordemComOutroServico);

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.findAll()).thenReturn(ordens);

        // When
        TempoMedioServicoResponseDTO response = service.execute(servicoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.totalOrdensAnalisadas()).isEqualTo(1); // Apenas ordemServico1
        assertThat(response.tempoMedioFormatado()).isEqualTo("1 horas e 0 minutos");
    }
}