package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.ServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarServicoUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Serviço na camada de Adapter.
 * Responsável por orquestrar chamadas aos casos de uso.
 * Segue o padrão Clean Architecture, delegando a lógica de negócio aos UseCases.
 */
@Component
public class ServicoController {

    private final GerenciarServicoUseCase gerenciarServicoUseCase;

    public ServicoController(GerenciarServicoUseCase gerenciarServicoUseCase) {
        this.gerenciarServicoUseCase = gerenciarServicoUseCase;
    }

    public ServicoResponseDTO criar(ServicoRequestDTO requestDTO) {
        return gerenciarServicoUseCase.create(requestDTO);
    }

    public ServicoResponseDTO buscarPorId(UUID id) {
        return gerenciarServicoUseCase.getById(id);
    }

    public List<ServicoResponseDTO> listarTodos() {
        return gerenciarServicoUseCase.getAll();
    }

    public ServicoResponseDTO atualizar(UUID id, ServicoRequestDTO requestDTO) {
        return gerenciarServicoUseCase.update(id, requestDTO);
    }

    public void deletar(UUID id) {
        gerenciarServicoUseCase.delete(id);
    }
}
