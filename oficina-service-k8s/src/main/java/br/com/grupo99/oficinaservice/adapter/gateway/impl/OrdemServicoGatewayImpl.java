package br.com.grupo99.oficinaservice.adapter.gateway.impl;

import br.com.grupo99.oficinaservice.adapter.gateway.OrdemServicoGateway;
import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import br.com.grupo99.oficinaservice.domain.repository.OrdemServicoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação do Gateway de Ordem de Serviço.
 * Evita vazamento de domínio convertendo Entities em representações externas.
 */
@Component
public class OrdemServicoGatewayImpl implements OrdemServicoGateway {

    private final OrdemServicoRepository ordemServicoRepository;

    public OrdemServicoGatewayImpl(OrdemServicoRepository ordemServicoRepository) {
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @Override
    public OrdemServico salvar(OrdemServico ordemServico) {
        return ordemServicoRepository.save(ordemServico);
    }

    @Override
    public Optional<OrdemServico> buscarPorId(UUID id) {
        return ordemServicoRepository.findById(id);
    }

    @Override
    public List<OrdemServico> buscarTodos() {
        return ordemServicoRepository.findAll();
    }

    @Override
    public Optional<OrdemServico> buscarPorClienteVeiculoEStatus(UUID clienteId, UUID veiculoId, List<StatusOS> status) {
        return ordemServicoRepository.findByClienteIdAndVeiculoIdAndStatusIn(clienteId, veiculoId, status);
    }
}

