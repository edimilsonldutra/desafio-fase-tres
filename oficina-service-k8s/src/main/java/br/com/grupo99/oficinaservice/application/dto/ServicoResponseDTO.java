package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.domain.model.Servico;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para a resposta ao cliente, contendo os dados de um Serviço.
 *
 * @param id O identificador único do serviço.
 * @param descricao A descrição do serviço.
 * @param preco O preço do serviço.
 */
public record ServicoResponseDTO(
        UUID id,
        String descricao,
        BigDecimal preco
) {
    /**
     * Método de fábrica para converter uma entidade Serviço em um ServicoResponseDTO.
     *
     * @param id O ID do serviço.
     * @param descricao A descrição do serviço.
     * @param preco O preço do serviço.
     * @return Um novo ServicoResponseDTO com os dados fornecidos.
     */
    public static ServicoResponseDTO fromDomain(Servico servico) {
        return new ServicoResponseDTO(servico.getId(), servico.getDescricao(), servico.getPreco());
    }
}
