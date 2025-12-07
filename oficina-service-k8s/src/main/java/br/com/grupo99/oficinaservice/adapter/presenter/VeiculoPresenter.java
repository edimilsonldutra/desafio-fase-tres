package br.com.grupo99.oficinaservice.adapter.presenter;

import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;

import java.util.List;

/**
 * Presenter para Veículo.
 */
public interface VeiculoPresenter {
    VeiculoResponseDTO paraResponseDTO(Veiculo veiculo);
    List<VeiculoResponseDTO> paraListaResponseDTO(List<Veiculo> veiculos);
}

