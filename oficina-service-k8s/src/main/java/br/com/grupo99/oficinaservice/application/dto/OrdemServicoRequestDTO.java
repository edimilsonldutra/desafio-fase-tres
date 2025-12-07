package br.com.grupo99.oficinaservice.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

/**
 * DTO para a requisição de criação de uma nova Ordem de Serviço.
 *
 * @param cpfCnpjCliente CPF/CNPJ do cliente para identificação.
 * @param placaVeiculo Placa do veículo a ser atendido.
 * @param servicosIds Lista de IDs dos serviços solicitados.
 * @param pecasIds Lista de IDs das peças a serem utilizadas.
 */
public record OrdemServicoRequestDTO(
        @NotBlank String cpfCnpjCliente,
        @NotBlank String placaVeiculo,
        List<UUID> servicosIds,
        List<UUID> pecasIds
) {}

