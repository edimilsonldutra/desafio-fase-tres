package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.domain.model.Peca;

import java.math.BigDecimal;
import java.util.UUID;

public record PecaResponseDTO(
        UUID id,
        String nome,
        String fabricante,
        BigDecimal preco,
        int estoque
) {
    /**
     * Método de fábrica para converter uma entidade Peca em um PecaResponseDTO.
     */
    public static PecaResponseDTO fromDomain(Peca peca) {
        return new PecaResponseDTO(
                peca.getId(),
                peca.getNome(),
                peca.getFabricante(),
                peca.getPreco(),
                peca.getEstoque()
        );
    }
}
