package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.TempoMedioServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.usecase.CalcularTempoMedioServicoUseCase;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Controller de Relatório na camada de Adapter.
 * Responsável por orquestrar chamadas aos casos de uso.
 * Segue o padrão Clean Architecture, delegando a lógica de negócio aos UseCases.
 */
@Component
public class RelatorioController {

    private final CalcularTempoMedioServicoUseCase calcularTempoMedioServicoUseCase;

    public RelatorioController(CalcularTempoMedioServicoUseCase calcularTempoMedioServicoUseCase) {
        this.calcularTempoMedioServicoUseCase = calcularTempoMedioServicoUseCase;
    }

    public TempoMedioServicoResponseDTO calcularTempoMedioPorServico(UUID servicoId) {
        return calcularTempoMedioServicoUseCase.execute(servicoId);
    }
}
