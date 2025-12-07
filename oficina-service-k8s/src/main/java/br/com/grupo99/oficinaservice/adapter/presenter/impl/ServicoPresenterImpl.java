package br.com.grupo99.oficinaservice.adapter.presenter.impl;

import br.com.grupo99.oficinaservice.adapter.presenter.ServicoPresenter;
import br.com.grupo99.oficinaservice.application.dto.ServicoResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Servico;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServicoPresenterImpl implements ServicoPresenter {

    @Override
    public ServicoResponseDTO paraResponseDTO(Servico servico) {
        if (servico == null) {
            return null;
        }

        return new ServicoResponseDTO(
                servico.getId(),
                servico.getDescricao(),
                servico.getPreco()
        );
    }

    @Override
    public List<ServicoResponseDTO> paraListaResponseDTO(List<Servico> servicos) {
        if (servicos == null) {
            return List.of();
        }

        return servicos.stream()
                .map(this::paraResponseDTO)
                .collect(Collectors.toList());
    }
}

