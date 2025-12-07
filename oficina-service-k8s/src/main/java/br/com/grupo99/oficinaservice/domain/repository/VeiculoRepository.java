package br.com.grupo99.oficinaservice.domain.repository;

import br.com.grupo99.oficinaservice.domain.model.Veiculo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VeiculoRepository {
    Veiculo save(Veiculo veiculo);
    Optional<Veiculo> findById(UUID id);
    Optional<Veiculo> findByPlaca(String placa);
    void deleteById(UUID id);
    List<Veiculo> findAll();
    boolean existsById(UUID id);
    boolean existsByRenavam(String renavam);
}
