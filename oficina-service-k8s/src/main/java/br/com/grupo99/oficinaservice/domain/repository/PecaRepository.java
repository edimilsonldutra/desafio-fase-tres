package br.com.grupo99.oficinaservice.domain.repository;

import br.com.grupo99.oficinaservice.domain.model.Peca;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PecaRepository {
    Peca save(Peca peca);
    Optional<Peca> findById(UUID id);
    void deleteById(UUID id);
    List<Peca> findAll();
    boolean existsById(UUID id);
}
