package br.com.grupo99.oficinaservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PecaRequestDTO(
        @NotBlank(message = "O nome da peça não pode ser vazio.")
        String nome,
        String fabricante,
        @NotNull(message = "O preço não pode ser nulo.")
        @Positive(message = "O preço deve ser um valor positivo.")
        BigDecimal preco,
        @NotNull(message = "O estoque não pode ser nulo.")
        @PositiveOrZero(message = "O estoque deve ser um valor positivo ou zero.")
        int estoque
) {}
