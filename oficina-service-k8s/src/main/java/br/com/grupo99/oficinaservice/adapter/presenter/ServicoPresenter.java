package br.com.grupo99.oficinaservice.adapter.presenter;

import br.com.grupo99.oficinaservice.application.dto.ServicoResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Servico;

import java.util.List;

/**
 * Presenter para Serviço.
 */
public interface ServicoPresenter {
    ServicoResponseDTO paraResponseDTO(Servico servico);
    List<ServicoResponseDTO> paraListaResponseDTO(List<Servico> servicos);
}

