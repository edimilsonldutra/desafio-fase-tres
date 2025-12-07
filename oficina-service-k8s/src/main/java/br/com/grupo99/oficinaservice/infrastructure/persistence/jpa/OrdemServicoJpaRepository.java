package br.com.grupo99.oficinaservice.infrastructure.persistence.jpa;

import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServico, UUID> {
    List<OrdemServico> findByClienteId(UUID clienteId);
    Optional<OrdemServico> findByClienteIdAndVeiculoIdAndStatusIn(UUID clienteId, UUID veiculoId, List<StatusOS> statusList);
}
