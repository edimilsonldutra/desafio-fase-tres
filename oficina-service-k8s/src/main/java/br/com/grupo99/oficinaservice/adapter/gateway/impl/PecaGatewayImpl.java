package br.com.grupo99.oficinaservice.adapter.gateway.impl;

import br.com.grupo99.oficinaservice.adapter.gateway.PecaGateway;
import br.com.grupo99.oficinaservice.domain.model.Peca;
import br.com.grupo99.oficinaservice.domain.repository.PecaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PecaGatewayImpl implements PecaGateway {

    private final PecaRepository pecaRepository;

    public PecaGatewayImpl(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }

    @Override
    public Peca salvar(Peca peca) {
        return pecaRepository.save(peca);
    }

    @Override
    public Optional<Peca> buscarPorId(UUID id) {
        return pecaRepository.findById(id);
    }

    @Override
    public List<Peca> buscarTodos() {
        return pecaRepository.findAll();
    }

    @Override
    public void deletarPorId(UUID id) {
        pecaRepository.deleteById(id);
    }

    @Override
    public boolean existePorId(UUID id) {
        return pecaRepository.existsById(id);
    }
}

