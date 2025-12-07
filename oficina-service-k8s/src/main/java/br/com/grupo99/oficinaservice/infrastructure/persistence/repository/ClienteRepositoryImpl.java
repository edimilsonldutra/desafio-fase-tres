package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.ClienteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ClienteRepositoryImpl implements ClienteRepository {

    private final ClienteJpaRepository jpaRepository;

    public ClienteRepositoryImpl(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Cliente save(Cliente cliente) {
        return jpaRepository.save(cliente);
    }

    @Override
    public Optional<Cliente> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Cliente> findByCpfCnpj(String cpfCnpj) {
        return jpaRepository.findByPessoaNumeroDocumento(cpfCnpj);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Cliente> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByCpfCnpj(String s) { return jpaRepository.existsByPessoaNumeroDocumento(s); }

    @Override
    public Optional<Cliente> findByCpfCnpjCliente(String cpfCnpj) {
        return jpaRepository.findByPessoaNumeroDocumento(cpfCnpj);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByPessoaEmail(email);
    }

    @Override
    public boolean existsByTelefone(String telefone) {
        return jpaRepository.existsByPessoaTelefone(telefone);
    }
}
