package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.application.util.DocumentoUtils;
import br.com.grupo99.oficinaservice.domain.model.Cliente;

import java.util.UUID;

/**
 * DTO para a resposta ao cliente, contendo os dados de um Cliente.
 *
 * @param id O identificador único do cliente.
 * @param nome O nome do cliente.
 * @param cpfCnpj O CPF ou CNPJ do cliente.
 * @param telefone O telefone de contato.
 * @param email O e-mail de contato.
 */
public record ClienteResponseDTO(
        UUID id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email
) {

    public static ClienteResponseDTO fromDomain(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getPessoa().getName(),
                DocumentoUtils.aplicarMascara(cliente.getPessoa().getNumeroDocumento()),
                cliente.getPessoa().getPhone(),
                cliente.getPessoa().getEmail()
        );
    }
}
