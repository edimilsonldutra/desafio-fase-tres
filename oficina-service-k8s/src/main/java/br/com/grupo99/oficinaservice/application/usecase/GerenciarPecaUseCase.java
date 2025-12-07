package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.PecaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;

import java.util.List;
import java.util.UUID;

public interface GerenciarPecaUseCase {
    PecaResponseDTO create(PecaRequestDTO requestDTO);
    PecaResponseDTO update(UUID id, PecaRequestDTO requestDTO);
    PecaResponseDTO getById(UUID id);
    List<PecaResponseDTO> getAll();
    void delete(UUID id);
    PecaResponseDTO adicionarEstoque(UUID id, int quantidade);
}
