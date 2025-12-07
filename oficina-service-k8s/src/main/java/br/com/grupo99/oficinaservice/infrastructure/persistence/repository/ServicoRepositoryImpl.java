package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.Servico;
import br.com.grupo99.oficinaservice.domain.repository.ServicoRepository;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.ServicoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ServicoRepositoryImpl implements ServicoRepository {

    private final ServicoJpaRepository jpaRepository;

    public ServicoRepositoryImpl(ServicoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Servico save(Servico servico) {
        return jpaRepository.save(servico);
    }

    @Override
    public Optional<Servico> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Servico> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
