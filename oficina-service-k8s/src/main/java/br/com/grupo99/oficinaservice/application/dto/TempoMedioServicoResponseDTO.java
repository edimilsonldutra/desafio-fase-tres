package br.com.grupo99.oficinaservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record TempoMedioServicoResponseDTO(
        @Schema(description = "ID do serviço analisado")
        UUID servicoId,

        @Schema(description = "Descrição do serviço analisado")
        String servicoDescricao,

        @Schema(description = "Quantidade de ordens de serviço finalizadas que foram analisadas")
        long totalOrdensAnalisadas,

        @Schema(description = "Tempo médio de execução formatado (ex: '2 horas e 30 minutos')")
        String tempoMedioFormatado
) {}
