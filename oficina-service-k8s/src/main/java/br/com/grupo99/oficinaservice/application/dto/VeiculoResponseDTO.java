package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.domain.model.Veiculo;

import java.util.UUID;

/**
 * DTO para a resposta ao cliente, contendo os dados de um Veículo.
 *
 * @param id O identificador único do veículo.
 * @param placa A placa do veículo.
 * @param marca A marca do veículo.
 * @param modelo O modelo do veículo.
 * @param ano O ano de fabricação do veículo.
 */
public record VeiculoResponseDTO(
        UUID id,
        String placa,
        String renavam,
        String marca,
        String modelo,
        Integer ano
) {
    public static VeiculoResponseDTO fromDomain(Veiculo veiculo) {
        return new VeiculoResponseDTO(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getRenavam(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno()
        );
    }
}
