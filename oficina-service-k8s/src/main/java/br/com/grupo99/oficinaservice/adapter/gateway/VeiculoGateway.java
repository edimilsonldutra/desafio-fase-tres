package br.com.grupo99.oficinaservice.adapter.gateway;

import br.com.grupo99.oficinaservice.domain.model.Veiculo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gateway para acesso a dados de Veículo.
 */
public interface VeiculoGateway {
    Veiculo salvar(Veiculo veiculo);
    Optional<Veiculo> buscarPorId(UUID id);
    Optional<Veiculo> buscarPorPlaca(String placa);
    List<Veiculo> buscarTodos();
    void deletarPorId(UUID id);
    boolean existePorId(UUID id);
}

