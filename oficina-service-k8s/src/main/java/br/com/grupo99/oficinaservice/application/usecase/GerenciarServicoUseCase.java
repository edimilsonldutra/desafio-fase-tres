package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.ServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ServicoResponseDTO;

import java.util.List;
import java.util.UUID;

public interface GerenciarServicoUseCase {
    ServicoResponseDTO create(ServicoRequestDTO requestDTO);
    ServicoResponseDTO update(UUID id, ServicoRequestDTO requestDTO);
    ServicoResponseDTO getById(UUID id);
    List<ServicoResponseDTO> getAll();
    void delete(UUID id);
}
