package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarVeiculoUseCase;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Veículo na camada de Adapter.
 * Responsável por orquestrar chamadas aos casos de uso.
 * Segue o padrão Clean Architecture, delegando a lógica de negócio aos UseCases.
 */
@Component
public class VeiculoController {

    private final GerenciarVeiculoUseCase gerenciarVeiculoUseCase;

    public VeiculoController(GerenciarVeiculoUseCase gerenciarVeiculoUseCase) {
        this.gerenciarVeiculoUseCase = gerenciarVeiculoUseCase;
    }

    public VeiculoResponseDTO criar(VeiculoRequestDTO requestDTO) {
        return gerenciarVeiculoUseCase.create(requestDTO);
    }

    public VeiculoResponseDTO buscarPorId(UUID id) {
        return gerenciarVeiculoUseCase.getById(id);
    }

    public List<VeiculoResponseDTO> listarTodos() {
        return gerenciarVeiculoUseCase.getAll();
    }

    public VeiculoResponseDTO atualizar(UUID id, VeiculoRequestDTO requestDTO) {
        return gerenciarVeiculoUseCase.update(id, requestDTO);
    }

    public void deletar(UUID id) {
        gerenciarVeiculoUseCase.delete(id);
    }
}

