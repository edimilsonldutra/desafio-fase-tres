package br.com.grupo99.oficinaservice.adapter.presenter;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoDetalhesDTO;
import br.com.grupo99.oficinaservice.domain.model.OrdemServico;

import java.util.List;

/**
 * Presenter para Ordem de Serviço.
 */
public interface OrdemServicoPresenter {
    OrdemServicoResponseDTO paraResponseDTO(OrdemServico ordemServico, String clienteNome, String placaVeiculo);
    OrdemServicoDetalhesDTO paraDetalhesDTO(OrdemServico ordemServico, String clienteNome, String placaVeiculo);
    List<OrdemServicoResponseDTO> paraListaResponseDTO(List<OrdemServico> ordensServico);
}

