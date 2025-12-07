package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoResponseDTO;

public interface CriarOrdemServicoUseCase {
    OrdemServicoResponseDTO execute(OrdemServicoRequestDTO requestDTO);
}
