package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.repository.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * UseCase para deletar uma Pessoa
 */
@Service
public class DeletePessoaUseCase {

    private final PessoaRepository pessoaRepository;

    public DeletePessoaUseCase(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public void execute(UUID id) {
        Pessoa pessoa = pessoaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada com ID: " + id));
        
        // O cascade vai deletar automaticamente o funcionário se existir
        pessoaRepository.delete(pessoa);
    }
}
