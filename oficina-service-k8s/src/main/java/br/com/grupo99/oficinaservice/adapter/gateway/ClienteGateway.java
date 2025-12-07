package br.com.grupo99.oficinaservice.adapter.gateway;

import br.com.grupo99.oficinaservice.domain.model.Cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Gateway para acesso a dados de Cliente.
 */
public interface ClienteGateway {
    Cliente salvar(Cliente cliente);
    Optional<Cliente> buscarPorId(UUID id);
    Optional<Cliente> buscarPorCpfCnpj(String cpfCnpj);
    List<Cliente> buscarTodos();
    void deletarPorId(UUID id);
    boolean existePorId(UUID id);
    boolean existePorCpfCnpj(String cpfCnpj);
    boolean existePorEmail(String email);
    boolean existePorTelefone(String telefone);
}

