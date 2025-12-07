package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para a requisição de atualização de status de uma Ordem de Serviço.
 *
 * @param novoStatus O novo status para a OS.
 */
public record OrdemServicoStatusUpdateRequestDTO(
        @NotNull(message = "O novo status não pode ser nulo.")
        StatusOS novoStatus
) {}
