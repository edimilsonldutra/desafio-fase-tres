package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.TempoMedioServicoResponseDTO;

import java.util.UUID;

public interface CalcularTempoMedioServicoUseCase {
    TempoMedioServicoResponseDTO execute(UUID servicoId);
}
