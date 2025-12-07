package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoDetalhesDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.usecase.AtualizarStatusOrdemServicoUseCase;
import br.com.grupo99.oficinaservice.application.usecase.BuscarOrdemServicoDetalhesUseCase;
import br.com.grupo99.oficinaservice.application.usecase.CriarOrdemServicoUseCase;
import br.com.grupo99.oficinaservice.application.usecase.ListarOrdensServicoUseCase;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Ordem de Serviço na camada de Adapter.
 * Responsável por orquestrar chamadas aos casos de uso.
 * Segue o padrão Clean Architecture, delegando a lógica de negócio aos UseCases.
 */
@Component
public class OrdemServicoController {

    private final CriarOrdemServicoUseCase criarOrdemServicoUseCase;
    private final ListarOrdensServicoUseCase listarOrdensServicoUseCase;
    private final BuscarOrdemServicoDetalhesUseCase buscarOrdemServicoDetalhesUseCase;
    private final AtualizarStatusOrdemServicoUseCase atualizarStatusOrdemServicoUseCase;

    public OrdemServicoController(CriarOrdemServicoUseCase criarOrdemServicoUseCase,
                                  ListarOrdensServicoUseCase listarOrdensServicoUseCase,
                                  BuscarOrdemServicoDetalhesUseCase buscarOrdemServicoDetalhesUseCase,
                                  AtualizarStatusOrdemServicoUseCase atualizarStatusOrdemServicoUseCase) {
        this.criarOrdemServicoUseCase = criarOrdemServicoUseCase;
        this.listarOrdensServicoUseCase = listarOrdensServicoUseCase;
        this.buscarOrdemServicoDetalhesUseCase = buscarOrdemServicoDetalhesUseCase;
        this.atualizarStatusOrdemServicoUseCase = atualizarStatusOrdemServicoUseCase;
    }

    public OrdemServicoResponseDTO criar(OrdemServicoRequestDTO requestDTO) {
        return criarOrdemServicoUseCase.execute(requestDTO);
    }

    public List<OrdemServicoResponseDTO> listarTodos() {
        return listarOrdensServicoUseCase.execute();
    }

    public OrdemServicoDetalhesDTO buscarDetalhes(UUID id) {
        return buscarOrdemServicoDetalhesUseCase.execute(id);
    }

    public OrdemServicoResponseDTO atualizarStatus(UUID id, StatusOS novoStatus) {
        return atualizarStatusOrdemServicoUseCase.execute(id, novoStatus);
    }
}
