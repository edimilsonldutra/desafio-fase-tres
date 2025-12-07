package br.com.grupo99.oficinaservice.adapter.presenter.impl;

import br.com.grupo99.oficinaservice.adapter.presenter.PecaPresenter;
import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Peca;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PecaPresenterImpl implements PecaPresenter {

    @Override
    public PecaResponseDTO paraResponseDTO(Peca peca) {
        if (peca == null) {
            return null;
        }

        return new PecaResponseDTO(
                peca.getId(),
                peca.getNome(),
                peca.getFabricante(),
                peca.getPreco(),
                peca.getEstoque()
        );
    }

    @Override
    public List<PecaResponseDTO> paraListaResponseDTO(List<Peca> pecas) {
        if (pecas == null) {
            return List.of();
        }

        return pecas.stream()
                .map(this::paraResponseDTO)
                .collect(Collectors.toList());
    }
}

