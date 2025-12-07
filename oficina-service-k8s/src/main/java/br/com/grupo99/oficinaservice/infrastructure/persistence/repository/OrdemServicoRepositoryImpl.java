package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import br.com.grupo99.oficinaservice.domain.repository.OrdemServicoRepository;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.OrdemServicoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrdemServicoRepositoryImpl implements OrdemServicoRepository {

    private final OrdemServicoJpaRepository jpaRepository;

    public OrdemServicoRepositoryImpl(OrdemServicoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public OrdemServico save(OrdemServico ordemServico) {
        return jpaRepository.save(ordemServico);
    }

    @Override
    public Optional<OrdemServico> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<OrdemServico> findByClienteId(UUID clienteId) {
        return jpaRepository.findByClienteId(clienteId);
    }

    @Override
    public List<OrdemServico> findAll() {
        List<OrdemServico> ordens = new ArrayList<>(jpaRepository.findAll());
        // Excluir logicamente OS finalizadas e entregues
        ordens.removeIf(os -> os.getStatus() == StatusOS.FINALIZADA || os.getStatus() == StatusOS.ENTREGUE);
        ordens.sort(Comparator
            .comparing((OrdemServico os) -> getStatusOrder(os.getStatus()))
            .thenComparing(OrdemServico::getDataCriacao)
        );
        return ordens;
    }

    @Override
    public Optional<OrdemServico> findByClienteIdAndVeiculoIdAndStatusIn(UUID clienteId, UUID veiculoId, List<StatusOS> statusList) {
        return jpaRepository.findByClienteIdAndVeiculoIdAndStatusIn(clienteId, veiculoId, statusList);
    }

    private int getStatusOrder(StatusOS status) {
        if (status == StatusOS.EM_EXECUCAO) return 1;
        if (status == StatusOS.AGUARDANDO_APROVACAO) return 2;
        if (status == StatusOS.EM_DIAGNOSTICO) return 3;
        if (status == StatusOS.RECEBIDA) return 4;
        return 99;
    }
}
