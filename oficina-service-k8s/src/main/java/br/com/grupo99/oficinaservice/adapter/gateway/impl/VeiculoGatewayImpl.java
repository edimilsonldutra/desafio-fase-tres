package br.com.grupo99.oficinaservice.adapter.gateway.impl;

import br.com.grupo99.oficinaservice.adapter.gateway.VeiculoGateway;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import br.com.grupo99.oficinaservice.domain.repository.VeiculoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VeiculoGatewayImpl implements VeiculoGateway {

    private final VeiculoRepository veiculoRepository;

    public VeiculoGatewayImpl(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    @Override
    public Veiculo salvar(Veiculo veiculo) {
        return veiculoRepository.save(veiculo);
    }

    @Override
    public Optional<Veiculo> buscarPorId(UUID id) {
        return veiculoRepository.findById(id);
    }

    @Override
    public Optional<Veiculo> buscarPorPlaca(String placa) {
        return veiculoRepository.findByPlaca(placa);
    }

    @Override
    public List<Veiculo> buscarTodos() {
        return veiculoRepository.findAll();
    }

    @Override
    public void deletarPorId(UUID id) {
        veiculoRepository.deleteById(id);
    }

    @Override
    public boolean existePorId(UUID id) {
        return veiculoRepository.existsById(id);
    }
}

