package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.Peca;
import br.com.grupo99.oficinaservice.domain.repository.PecaRepository;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.PecaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PecaRepositoryImpl implements PecaRepository {

    private final PecaJpaRepository jpaRepository;

    public PecaRepositoryImpl(PecaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Peca save(Peca peca) {
        return jpaRepository.save(peca);
    }

    @Override
    public Optional<Peca> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Peca> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
