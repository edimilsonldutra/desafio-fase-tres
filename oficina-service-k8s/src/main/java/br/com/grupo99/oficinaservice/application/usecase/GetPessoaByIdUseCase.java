package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.PessoaResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.repository.PessoaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * UseCase para buscar uma Pessoa por ID
 */
@Service
public class GetPessoaByIdUseCase {

    private final PessoaRepository pessoaRepository;

    public GetPessoaByIdUseCase(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    public PessoaResponseDTO execute(UUID id) {
        Pessoa pessoa = pessoaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pessoa não encontrada com ID: " + id));
        
        return PessoaResponseDTO.from(pessoa);
    }
}
