package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.application.validator.DocumentoValido;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para a requisição de criação ou atualização de um Cliente.
 *
 * @param nome O nome completo do cliente.
 * @param cpfCnpj O CPF ou CNPJ do cliente (deve ser validado).
 * @param telefone O telefone de contato.
 * @param email O e-mail de contato (deve ser um formato válido).
 */
public record ClienteRequestDTO(
        @NotBlank(message = "O nome não pode ser vazio.")
        String nome,
        @NotBlank(message = "O CPF/CNPJ não pode ser vazio.")
        @DocumentoValido
        String cpfCnpj,
        String telefone,
        @Email(message = "O formato do e-mail é inválido.")
        String email
) {}
