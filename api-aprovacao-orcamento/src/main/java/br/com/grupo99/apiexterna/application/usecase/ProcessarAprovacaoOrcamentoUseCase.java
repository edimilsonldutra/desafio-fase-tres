package br.com.grupo99.apiexterna.application.usecase;

import br.com.grupo99.apiexterna.dto.AprovacaoOrcamentoRequestDTO;
import br.com.grupo99.apiexterna.infrastructure.service.OrdemServicoGateway;
import org.springframework.stereotype.Service;

@Service
public class ProcessarAprovacaoOrcamentoUseCase {
    private final OrdemServicoGateway ordemServicoGateway;

    public ProcessarAprovacaoOrcamentoUseCase(OrdemServicoGateway ordemServicoGateway) {
        this.ordemServicoGateway = ordemServicoGateway;
    }

    public void execute(AprovacaoOrcamentoRequestDTO dto) {
        String status = dto.isAprovado() ? "EM_EXECUCAO" : "CANCELADA";
        ordemServicoGateway.atualizarStatusOrdemServico(dto.getOrdemServicoId(), status, dto.getMotivoRecusa());
    }
}
