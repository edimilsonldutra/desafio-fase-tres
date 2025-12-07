package br.com.grupo99.oficinaservice.adapter.gateway;

import br.com.grupo99.oficinaservice.domain.model.Servico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gateway para acesso a dados de Serviço.
 */
public interface ServicoGateway {
    Servico salvar(Servico servico);
    Optional<Servico> buscarPorId(UUID id);
    List<Servico> buscarTodos();
    void deletarPorId(UUID id);
    boolean existePorId(UUID id);
}

