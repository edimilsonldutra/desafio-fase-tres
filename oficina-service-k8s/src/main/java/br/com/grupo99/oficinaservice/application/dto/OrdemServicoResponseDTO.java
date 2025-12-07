package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para a resposta resumida de uma Ordem de Serviço.
 *
 * @param id O ID da OS.
 * @param clienteNome Nome do cliente.
 * @param placaVeiculo Placa do veículo.
 * @param status O status atual da OS.
 * @param valorTotal O valor total do orçamento/OS.
 * @param dataCriacao A data de criação da OS.
 */
public record OrdemServicoResponseDTO(
        UUID id,
        String clienteNome,
        String placaVeiculo,
        StatusOS status,
        String statusDescricao,
        BigDecimal valorTotal,
        LocalDateTime dataCriacao
) {

    /**
     * Método de fábrica para converter uma entidade OrdemServico em um OrdemServicoResponseDTO.
     * Este é o método que estava faltando.
     */
    public static OrdemServicoResponseDTO fromDomain(OrdemServico os, String clienteNome, String placaVeiculo) {
        return new OrdemServicoResponseDTO(
                os.getId(),
                clienteNome,
                placaVeiculo,
                os.getStatus(),
                getStatusDescricao(os.getStatus()),
                os.getValorTotal(),
                os.getDataCriacao()
        );
    }

    private static String getStatusDescricao(StatusOS status) {
        return switch (status) {
            case RECEBIDA -> "Recebida";
            case EM_DIAGNOSTICO -> "Diagnóstico";
            case AGUARDANDO_APROVACAO -> "Aguardando Aprovação";
            case EM_EXECUCAO -> "Execução";
            case FINALIZADA -> "Finalizada";
            case ENTREGUE -> "Entregue";
            case CANCELADA -> "Cancelada";
        };
    }

}
