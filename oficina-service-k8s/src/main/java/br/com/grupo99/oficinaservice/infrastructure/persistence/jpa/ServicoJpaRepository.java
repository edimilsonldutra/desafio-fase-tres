package br.com.grupo99.oficinaservice.infrastructure.persistence.jpa;

import br.com.grupo99.oficinaservice.domain.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServicoJpaRepository extends JpaRepository<Servico, UUID> {
}
