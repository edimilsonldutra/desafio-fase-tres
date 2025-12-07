package br.com.grupo99.oficinaservice.adapter.gateway.impl;

import br.com.grupo99.oficinaservice.adapter.gateway.ServicoGateway;
import br.com.grupo99.oficinaservice.domain.model.Servico;
import br.com.grupo99.oficinaservice.domain.repository.ServicoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ServicoGatewayImpl implements ServicoGateway {

    private final ServicoRepository servicoRepository;

    public ServicoGatewayImpl(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Override
    public Servico salvar(Servico servico) {
        return servicoRepository.save(servico);
    }

    @Override
    public Optional<Servico> buscarPorId(UUID id) {
        return servicoRepository.findById(id);
    }

    @Override
    public List<Servico> buscarTodos() {
        return servicoRepository.findAll();
    }

    @Override
    public void deletarPorId(UUID id) {
        servicoRepository.deleteById(id);
    }

    @Override
    public boolean existePorId(UUID id) {
        return servicoRepository.existsById(id);
    }
}

