package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.PessoaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PessoaResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Funcionario;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.repository.FuncionarioRepository;
import br.com.grupo99.oficinaservice.domain.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UseCase para criar uma nova Pessoa
 */
@Service
public class CreatePessoaUseCase {

    private final PessoaRepository pessoaRepository;
    private final FuncionarioRepository funcionarioRepository;

    public CreatePessoaUseCase(PessoaRepository pessoaRepository, FuncionarioRepository funcionarioRepository) {
        this.pessoaRepository = pessoaRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional
    public PessoaResponseDTO execute(PessoaRequestDTO request) {
        // Validar se já existe pessoa com mesmo documento
        if (pessoaRepository.existsByNumeroDocumento(request.numeroDocumento())) {
            throw new IllegalArgumentException("Já existe uma pessoa cadastrada com este documento");
        }

        // Validar se já existe pessoa com mesmo email
        if (pessoaRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Já existe uma pessoa cadastrada com este email");
        }

        // Criar a entidade Pessoa
        // TODO: Implementar lógica de senha (ex: gerar senha temporária e enviar por
        // email)
        Pessoa pessoa = new Pessoa(
                request.numeroDocumento(),
                request.tipoPessoa(),
                request.name(),
                request.email(),
                "$2a$10$tempPasswordNeedsToBeChanged123456789", // Senha temporária que deve ser alterada
                request.perfil());
        pessoa.setPhone(request.phone());
        pessoa.setCargo(request.cargo());
        pessoa.setAtivo(true);

        // Salvar a pessoa
        pessoa = pessoaRepository.save(pessoa);

        // Se for MECANICO ou ADMIN, criar registro de Funcionário
        if (request.perfil() == Perfil.MECANICO || request.perfil() == Perfil.ADMIN) {
            Funcionario funcionario = new Funcionario(pessoa);
            funcionario.setSetor(request.setor());
            funcionario.setSalario(request.salario());
            funcionarioRepository.save(funcionario);
            pessoa.setFuncionario(funcionario);
        }

        return PessoaResponseDTO.from(pessoa);
    }
}
