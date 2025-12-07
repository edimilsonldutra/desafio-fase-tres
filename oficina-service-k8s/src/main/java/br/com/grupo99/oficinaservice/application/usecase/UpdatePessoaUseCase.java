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

import java.util.UUID;

/**
 * UseCase para atualizar uma Pessoa existente
 */
@Service
public class UpdatePessoaUseCase {

    private final PessoaRepository pessoaRepository;
    private final FuncionarioRepository funcionarioRepository;

    public UpdatePessoaUseCase(PessoaRepository pessoaRepository, FuncionarioRepository funcionarioRepository) {
        this.pessoaRepository = pessoaRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    @Transactional
    public PessoaResponseDTO execute(UUID id, PessoaRequestDTO request) {
        Pessoa pessoa = pessoaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada com ID: " + id));

        // Validar se o documento já existe para outra pessoa
        pessoaRepository.findByNumeroDocumento(request.numeroDocumento())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Já existe outra pessoa com este documento");
                }
            });

        // Validar se o email já existe para outra pessoa
        pessoaRepository.findByEmail(request.email())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Já existe outra pessoa com este email");
                }
            });

        // Atualizar campos básicos
        pessoa.setNumeroDocumento(request.numeroDocumento());
        pessoa.setTipoPessoa(request.tipoPessoa());
        pessoa.setName(request.name());
        pessoa.setEmail(request.email());
        pessoa.setPhone(request.phone());
        pessoa.setCargo(request.cargo());
        pessoa.setPerfil(request.perfil());

        // Gerenciar registro de Funcionário
        if (request.perfil() == Perfil.MECANICO || request.perfil() == Perfil.ADMIN) {
            Funcionario funcionario = pessoa.getFuncionario();
            if (funcionario == null) {
                // Criar novo funcionário se não existir
                funcionario = new Funcionario(pessoa);
                funcionarioRepository.save(funcionario);
                pessoa.setFuncionario(funcionario);
            }
            // Atualizar dados do funcionário
            funcionario.setSetor(request.setor());
            funcionario.setSalario(request.salario());
        } else if (pessoa.getFuncionario() != null) {
            // Se mudou para CLIENTE, remover registro de funcionário
            funcionarioRepository.delete(pessoa.getFuncionario());
            pessoa.setFuncionario(null);
        }

        pessoa = pessoaRepository.save(pessoa);
        return PessoaResponseDTO.from(pessoa);
    }
}
