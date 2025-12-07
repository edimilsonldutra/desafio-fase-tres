package br.com.grupo99.oficinaservice.domain.repository;

import br.com.grupo99.oficinaservice.domain.model.Servico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicoRepository {
    Servico save(Servico servico);
    Optional<Servico> findById(UUID id);
    void deleteById(UUID id);
    List<Servico> findAll();
    boolean existsById(UUID id);
}
