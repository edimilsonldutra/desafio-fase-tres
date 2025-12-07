package br.com.grupo99.oficinaservice.adapter.gateway;

import br.com.grupo99.oficinaservice.domain.model.Peca;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gateway para acesso a dados de Peça.
 */
public interface PecaGateway {
    Peca salvar(Peca peca);
    Optional<Peca> buscarPorId(UUID id);
    List<Peca> buscarTodos();
    void deletarPorId(UUID id);
    boolean existePorId(UUID id);
}

