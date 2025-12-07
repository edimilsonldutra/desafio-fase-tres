package br.com.grupo99.oficinaservice.application.usecase;

import br.com.grupo99.oficinaservice.application.dto.PessoaResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.repository.PessoaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * UseCase para listar todas as Pessoas
 */
@Service
public class ListAllPessoasUseCase {

    private final PessoaRepository pessoaRepository;

    public ListAllPessoasUseCase(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    public List<PessoaResponseDTO> execute() {
        List<Pessoa> pessoas = pessoaRepository.findAll();
        return pessoas.stream()
            .map(PessoaResponseDTO::from)
            .collect(Collectors.toList());
    }
}
