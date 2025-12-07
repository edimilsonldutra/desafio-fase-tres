package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.PecaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarPecaUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Peça na camada de Adapter.
 * Responsável por orquestrar chamadas aos casos de uso.
 * Segue o padrão Clean Architecture, delegando a lógica de negócio aos UseCases.
 */
@Component
public class PecaController {

    private final GerenciarPecaUseCase gerenciarPecaUseCase;

    public PecaController(GerenciarPecaUseCase gerenciarPecaUseCase) {
        this.gerenciarPecaUseCase = gerenciarPecaUseCase;
    }

    public PecaResponseDTO criar(PecaRequestDTO requestDTO) {
        return gerenciarPecaUseCase.create(requestDTO);
    }

    public PecaResponseDTO buscarPorId(UUID id) {
        return gerenciarPecaUseCase.getById(id);
    }

    public List<PecaResponseDTO> listarTodos() {
        return gerenciarPecaUseCase.getAll();
    }

    public PecaResponseDTO atualizar(UUID id, PecaRequestDTO requestDTO) {
        return gerenciarPecaUseCase.update(id, requestDTO);
    }

    public void deletar(UUID id) {
        gerenciarPecaUseCase.delete(id);
    }

    public PecaResponseDTO adicionarEstoque(UUID id, Integer quantidade) {
        return gerenciarPecaUseCase.adicionarEstoque(id, quantidade);
    }
}
