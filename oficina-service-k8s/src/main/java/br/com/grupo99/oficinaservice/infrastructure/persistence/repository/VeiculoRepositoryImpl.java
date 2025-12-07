package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import br.com.grupo99.oficinaservice.domain.repository.VeiculoRepository;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.VeiculoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VeiculoRepositoryImpl implements VeiculoRepository {

    private final VeiculoJpaRepository jpaRepository;

    public VeiculoRepositoryImpl(VeiculoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Veiculo save(Veiculo veiculo) {
        return jpaRepository.save(veiculo);
    }

    @Override
    public Optional<Veiculo> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Veiculo> findByPlaca(String placa) {
        return jpaRepository.findByPlaca(placa);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Veiculo> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
    @Override
    public boolean existsByRenavam(String renavam) { // Método adicionado
        return jpaRepository.existsByRenavam(renavam);
    }
}
