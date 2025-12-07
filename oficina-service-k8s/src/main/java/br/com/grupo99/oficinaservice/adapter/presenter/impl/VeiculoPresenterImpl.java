package br.com.grupo99.oficinaservice.adapter.presenter.impl;

import br.com.grupo99.oficinaservice.adapter.presenter.VeiculoPresenter;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VeiculoPresenterImpl implements VeiculoPresenter {

    @Override
    public VeiculoResponseDTO paraResponseDTO(Veiculo veiculo) {
        if (veiculo == null) {
            return null;
        }

        return new VeiculoResponseDTO(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getRenavam(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno()
        );
    }

    @Override
    public List<VeiculoResponseDTO> paraListaResponseDTO(List<Veiculo> veiculos) {
        if (veiculos == null) {
            return List.of();
        }

        return veiculos.stream()
                .map(this::paraResponseDTO)
                .collect(Collectors.toList());
    }
}

