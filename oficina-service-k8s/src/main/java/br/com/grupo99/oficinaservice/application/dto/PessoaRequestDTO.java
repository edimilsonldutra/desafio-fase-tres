package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

/**
 * DTO para criação de uma nova Pessoa
 */
public record PessoaRequestDTO(
    String numeroDocumento,
    TipoPessoa tipoPessoa,
    String name,
    String email,
    String phone,
    String cargo,
    Perfil perfil,
    // Dados adicionais para Funcionário (se perfil = MECANICO ou ADMIN)
    String setor,
    Double salario
) {
    public PessoaRequestDTO {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new IllegalArgumentException("Número do documento é obrigatório");
        }
        if (tipoPessoa == null) {
            throw new IllegalArgumentException("Tipo de pessoa é obrigatório");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil é obrigatório");
        }
    }
}
