package br.com.grupo99.oficinaservice.domain.repository;

import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {
    
    Optional<Pessoa> findByNumeroDocumento(String numeroDocumento);
    
    Optional<Pessoa> findByEmail(String email);
    
    boolean existsByNumeroDocumento(String numeroDocumento);
    
    boolean existsByEmail(String email);
}
