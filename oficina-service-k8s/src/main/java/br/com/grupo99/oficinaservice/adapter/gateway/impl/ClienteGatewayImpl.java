package br.com.grupo99.oficinaservice.adapter.gateway.impl;

import br.com.grupo99.oficinaservice.adapter.gateway.ClienteGateway;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementação do Gateway de Cliente.
 * Responsável por converter entidades de domínio para persistência e vice-versa,
 * garantindo que não haja vazamento de detalhes de infraestrutura para o domínio.
 */
@Component
public class ClienteGatewayImpl implements ClienteGateway {

    private final ClienteRepository clienteRepository;

    public ClienteGatewayImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Cliente salvar(Cliente cliente) {
        // O Gateway recebe uma Entity do UseCase e a persiste
        // Pode realizar conversões se necessário (Entity -> DAO)
        return clienteRepository.save(cliente);
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID id) {
        return clienteRepository.findById(id);
    }

    @Override
    public Optional<Cliente> buscarPorCpfCnpj(String cpfCnpj) {
        return clienteRepository.findByCpfCnpj(cpfCnpj);
    }

    @Override
    public List<Cliente> buscarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public void deletarPorId(UUID id) {
        clienteRepository.deleteById(id);
    }

    @Override
    public boolean existePorId(UUID id) {
        return clienteRepository.existsById(id);
    }

    @Override
    public boolean existePorCpfCnpj(String cpfCnpj) {
        return clienteRepository.existsByCpfCnpj(cpfCnpj);
    }

    @Override
    public boolean existePorEmail(String email) {
        return clienteRepository.existsByEmail(email);
    }

    @Override
    public boolean existePorTelefone(String telefone) {
        return clienteRepository.existsByTelefone(telefone);
    }
}

