package br.com.grupo99.oficinaservice.adapter.gateway;

import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gateway para acesso a dados de Ordem de Serviço.
 */
public interface OrdemServicoGateway {
    OrdemServico salvar(OrdemServico ordemServico);
    Optional<OrdemServico> buscarPorId(UUID id);
    List<OrdemServico> buscarTodos();
    Optional<OrdemServico> buscarPorClienteVeiculoEStatus(UUID clienteId, UUID veiculoId, List<StatusOS> status);
}

