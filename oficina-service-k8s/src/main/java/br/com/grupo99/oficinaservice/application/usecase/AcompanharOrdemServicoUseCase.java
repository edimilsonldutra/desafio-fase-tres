package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoDetalhesDTO;

import java.util.UUID;

public interface AcompanharOrdemServicoUseCase {
    OrdemServicoDetalhesDTO execute(UUID id);
}
