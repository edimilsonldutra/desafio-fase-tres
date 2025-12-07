package br.com.grupo99.oficinaservice.application.dto;

import jakarta.validation.constraints.NotNull;

public record PecaUpdateEstoqueRequestDTO(
        @NotNull(message = "A quantidade não pode ser nula.")
        int quantidade
) {}
