package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoResponseDTO;

import java.util.List;

public interface ListarOrdensServicoUseCase {
    List<OrdemServicoResponseDTO> execute();
}
