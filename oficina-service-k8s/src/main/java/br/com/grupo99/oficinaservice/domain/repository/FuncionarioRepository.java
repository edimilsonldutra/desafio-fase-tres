package br.com.grupo99.oficinaservice.domain.repository;

import br.com.grupo99.oficinaservice.domain.model.Funcionario;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, UUID> {
    
    Optional<Funcionario> findByPessoa(Pessoa pessoa);
    
    Optional<Funcionario> findByPessoaId(UUID pessoaId);
}
