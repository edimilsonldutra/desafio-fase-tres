package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record OrdemServicoDetalhesDTO(
        UUID id,
        StatusOS status,
        BigDecimal valorTotal,
        LocalDateTime dataCriacao,
        LocalDateTime dataFinalizacao,
        LocalDateTime dataEntrega,
        ClienteResponseDTO cliente,
        VeiculoResponseDTO veiculo,
        List<ServicoResponseDTO> servicos,
        List<PecaResponseDTO> pecas
) {
    public static OrdemServicoDetalhesDTO fromDomain(OrdemServico os, Cliente cliente, Veiculo veiculo) {
        List<ServicoResponseDTO> servicosDTO = os.getServicos().stream()
                .map(item -> ServicoResponseDTO.fromDomain(item.getServico()))
                .collect(Collectors.toList());

        List<PecaResponseDTO> pecasDTO = os.getPecas().stream()
                .map(item -> PecaResponseDTO.fromDomain(item.getPeca()))
                .collect(Collectors.toList());

        return new OrdemServicoDetalhesDTO(
                os.getId(),
                os.getStatus(),
                os.getValorTotal(),
                os.getDataCriacao(),
                os.getDataFinalizacao(),
                os.getDataEntrega(),
                ClienteResponseDTO.fromDomain(cliente),
                VeiculoResponseDTO.fromDomain(veiculo),
                servicosDTO,
                pecasDTO
        );
    }
}
