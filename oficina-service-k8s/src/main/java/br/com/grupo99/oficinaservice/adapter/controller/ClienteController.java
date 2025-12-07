package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.ClienteRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarClienteUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Cliente na camada de Adapter.
 * Responsável por orquestrar chamadas aos casos de uso.
 * Segue o padrão Clean Architecture, delegando a lógica de negócio aos UseCases.
 */
@Component
public class ClienteController {

    private final GerenciarClienteUseCase gerenciarClienteUseCase;

    public ClienteController(GerenciarClienteUseCase gerenciarClienteUseCase) {
        this.gerenciarClienteUseCase = gerenciarClienteUseCase;
    }

    public ClienteResponseDTO criar(ClienteRequestDTO requestDTO) {
        return gerenciarClienteUseCase.create(requestDTO);
    }

    public ClienteResponseDTO buscarPorId(UUID id) {
        return gerenciarClienteUseCase.getById(id);
    }

    public List<ClienteResponseDTO> listarTodos() {
        return gerenciarClienteUseCase.getAll();
    }

    public ClienteResponseDTO atualizar(UUID id, ClienteRequestDTO requestDTO) {
        return gerenciarClienteUseCase.update(id, requestDTO);
    }

    public void deletar(UUID id) {
        gerenciarClienteUseCase.delete(id);
    }
}

