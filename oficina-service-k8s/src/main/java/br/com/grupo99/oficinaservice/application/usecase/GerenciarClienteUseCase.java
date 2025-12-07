package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.ClienteRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;

import java.util.List;
import java.util.UUID;

public interface GerenciarClienteUseCase {
    ClienteResponseDTO create(ClienteRequestDTO requestDTO);
    ClienteResponseDTO update(UUID id, ClienteRequestDTO requestDTO);
    ClienteResponseDTO getById(UUID id);
    List<ClienteResponseDTO> getAll();
    void delete(UUID id);
}
