package br.com.grupo99.oficinaservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para a requisição de criação ou atualização de um Serviço.
 *
 * @param descricao A descrição do serviço.
 * @param preco O preço do serviço.
 */
public record ServicoRequestDTO(
        @NotBlank(message = "A descrição não pode ser vazia.")
        String descricao,

        @NotNull(message = "O preço não pode ser nulo.")
        @Positive(message = "O preço deve ser um valor positivo.")
        BigDecimal preco
) {}
