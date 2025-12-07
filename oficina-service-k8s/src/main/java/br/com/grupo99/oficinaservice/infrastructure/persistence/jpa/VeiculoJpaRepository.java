package br.com.grupo99.oficinaservice.infrastructure.persistence.jpa;

import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VeiculoJpaRepository extends JpaRepository<Veiculo, UUID> {
    Optional<Veiculo> findByPlaca(String placa);
    Optional<List<Veiculo>> findByClienteId(UUID clienteId);
    boolean existsByPlaca(String placa);
    boolean existsByRenavam(String renavam);
    Optional<Veiculo> findByRenavam(String renavam);
}
