package br.com.grupo99.apiexterna.service;

import br.com.grupo99.apiexterna.domain.AprovacaoOrcamento;
import br.com.grupo99.apiexterna.presenter.AprovacaoOrcamentoResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AprovacaoOrcamentoService {

    @Value("${api.principal.url}")
    private String apiPrincipalUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public AprovacaoOrcamentoResponseDTO processarNotificacao(AprovacaoOrcamento aprovacao) {
        String status = aprovacao.isAprovado() ? "EM_EXECUCAO" : "CANCELADA";
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        if (!aprovacao.isAprovado() && aprovacao.getMotivoRecusa() != null) {
            body.put("motivoRecusa", aprovacao.getMotivoRecusa());
        }
        restTemplate.patchForObject(
            apiPrincipalUrl + "/api/v1/ordens-servico/" + aprovacao.getOrdemServicoId() + "/status",
            body,
            Void.class
        );
        String mensagem = aprovacao.isAprovado() ? "Orçamento aprovado e ordem em execução." : "Orçamento recusado e ordem cancelada.";
        return new AprovacaoOrcamentoResponseDTO(aprovacao.getOrdemServicoId(), status, mensagem);
    }
}
