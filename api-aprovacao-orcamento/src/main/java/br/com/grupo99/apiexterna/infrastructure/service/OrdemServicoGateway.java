package br.com.grupo99.apiexterna.infrastructure.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class OrdemServicoGateway {
    @Value("${api.principal.url}")
    private String apiPrincipalUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void atualizarStatusOrdemServico(UUID ordemServicoId, String status, String motivoRecusa) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        if (motivoRecusa != null) {
            body.put("motivoRecusa", motivoRecusa);
        }
        restTemplate.patchForObject(
            apiPrincipalUrl + "/api/v1/ordens-servico/" + ordemServicoId + "/status",
            body,
            Void.class
        );
    }
}

