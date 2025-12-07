package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;

import java.util.List;
import java.util.UUID;

public interface GerenciarVeiculoUseCase {
    VeiculoResponseDTO create(VeiculoRequestDTO requestDTO);
    VeiculoResponseDTO update(UUID id, VeiculoRequestDTO requestDTO);
    VeiculoResponseDTO getById(UUID id);
    List<VeiculoResponseDTO> getAll();
    void delete(UUID id);
}
