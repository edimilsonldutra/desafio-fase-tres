package br.com.grupo99.oficinaservice.adapter.presenter;

import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Peca;

import java.util.List;

/**
 * Presenter para Peça.
 */
public interface PecaPresenter {
    PecaResponseDTO paraResponseDTO(Peca peca);
    List<PecaResponseDTO> paraListaResponseDTO(List<Peca> pecas);
}

