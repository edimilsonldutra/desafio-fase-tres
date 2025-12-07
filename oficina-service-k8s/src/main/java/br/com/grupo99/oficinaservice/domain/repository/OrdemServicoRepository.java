package br.com.grupo99.oficinaservice.domain.repository;

import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {
    OrdemServico save(OrdemServico ordemServico);
    Optional<OrdemServico> findById(UUID id);
    List<OrdemServico> findAll();
    List<OrdemServico> findByClienteId(UUID clienteId);
    Optional<OrdemServico> findByClienteIdAndVeiculoIdAndStatusIn(UUID clienteId, UUID veiculoId, List<StatusOS> statusList);
}
