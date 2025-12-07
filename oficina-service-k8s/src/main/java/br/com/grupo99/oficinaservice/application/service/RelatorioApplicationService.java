package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.TempoMedioServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.application.usecase.CalcularTempoMedioServicoUseCase;
import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.Servico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import br.com.grupo99.oficinaservice.domain.repository.OrdemServicoRepository;
import br.com.grupo99.oficinaservice.domain.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class RelatorioApplicationService implements CalcularTempoMedioServicoUseCase {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ServicoRepository servicoRepository;

    public RelatorioApplicationService(
            OrdemServicoRepository ordemServicoRepository,
            ServicoRepository servicoRepository
    ) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.servicoRepository = servicoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TempoMedioServicoResponseDTO execute(UUID servicoId) {
        Servico servico = buscarServicoPorId(servicoId);
        List<OrdemServico> ordens = ordemServicoRepository.findAll();

        double mediaEmMinutos = calcularMediaDuracao(ordens, servicoId);
        long totalOrdens = contarOrdensAnalisadas(ordens, servicoId);
        String tempoFormatado = formatarDuracao(mediaEmMinutos);

        return new TempoMedioServicoResponseDTO(
                servicoId,
                servico.getDescricao(),
                totalOrdens,
                tempoFormatado
        );
    }

    private Servico buscarServicoPorId(UUID servicoId) {
        return servicoRepository.findById(servicoId)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o id: " + servicoId));
    }

    private double calcularMediaDuracao(List<OrdemServico> ordens, UUID servicoId) {
        return ordens.stream()
                .filter(this::isFinalizadaOuEntregue)
                .filter(os -> contemServico(os, servicoId))
                .filter(os -> os.getDataCriacao() != null && os.getDataFinalizacao() != null)
                .mapToLong(os -> Duration.between(os.getDataCriacao(), os.getDataFinalizacao()).toMinutes())
                .average()
                .orElse(0.0);
    }

    private long contarOrdensAnalisadas(List<OrdemServico> ordens, UUID servicoId) {
        return ordens.stream()
                .filter(this::isFinalizadaOuEntregue)
                .filter(os -> contemServico(os, servicoId))
                .count();
    }

    private boolean isFinalizadaOuEntregue(OrdemServico os) {
        return os.getStatus() == StatusOS.FINALIZADA || os.getStatus() == StatusOS.ENTREGUE;
    }

    private boolean contemServico(OrdemServico os, UUID servicoId) {
        return os.getServicos().stream()
                .anyMatch(item -> item.getServico().getId().equals(servicoId));
    }

    private String formatarDuracao(double mediaEmMinutos) {
        if (mediaEmMinutos == 0) {
            return "N/A";
        }
        long totalMinutos = (long) mediaEmMinutos;
        long horas = totalMinutos / 60;
        long minutos = totalMinutos % 60;
        return String.format("%d horas e %d minutos", horas, minutos);
    }
}