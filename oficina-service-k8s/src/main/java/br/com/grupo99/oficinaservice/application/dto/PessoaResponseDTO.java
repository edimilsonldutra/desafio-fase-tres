package br.com.grupo99.oficinaservice.application.dto;

import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de resposta com os dados da Pessoa
 */
public record PessoaResponseDTO(
    UUID id,
    String numeroDocumento,
    TipoPessoa tipoPessoa,
    String name,
    String email,
    String phone,
    String cargo,
    Perfil perfil,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    // Dados do funcionário (se aplicável)
    UUID funcionarioId,
    LocalDate dataAdmissao,
    String setor,
    Double salario
) {
    public static PessoaResponseDTO from(Pessoa pessoa) {
        if (pessoa == null) {
            return null;
        }

        UUID funcionarioId = null;
        LocalDate dataAdmissao = null;
        String setor = null;
        Double salario = null;

        if (pessoa.getFuncionario() != null) {
            funcionarioId = pessoa.getFuncionario().getId();
            dataAdmissao = pessoa.getFuncionario().getDataAdmissao();
            setor = pessoa.getFuncionario().getSetor();
            salario = pessoa.getFuncionario().getSalario();
        }

        return new PessoaResponseDTO(
            pessoa.getId(),
            pessoa.getNumeroDocumento(),
            pessoa.getTipoPessoa(),
            pessoa.getName(),
            pessoa.getEmail(),
            pessoa.getPhone(),
            pessoa.getCargo(),
            pessoa.getPerfil(),
            pessoa.getCreatedAt(),
            pessoa.getUpdatedAt(),
            funcionarioId,
            dataAdmissao,
            setor,
            salario
        );
    }
}
