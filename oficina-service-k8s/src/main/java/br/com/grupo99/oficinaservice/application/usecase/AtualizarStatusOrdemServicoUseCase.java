package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;

import java.util.UUID;

public interface AtualizarStatusOrdemServicoUseCase {
    OrdemServicoResponseDTO execute(UUID id, StatusOS novoStatus);
}
