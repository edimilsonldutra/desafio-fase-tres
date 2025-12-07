package br.com.grupo99.apiexterna.presenter;

import java.util.UUID;

public class AprovacaoOrcamentoResponseDTO {
    private UUID ordemServicoId;
    private String status;
    private String mensagem;

    public AprovacaoOrcamentoResponseDTO(UUID ordemServicoId, String status, String mensagem) {
        this.ordemServicoId = ordemServicoId;
        this.status = status;
        this.mensagem = mensagem;
    }

    public UUID getOrdemServicoId() {
        return ordemServicoId;
    }
    public String getStatus() {
        return status;
    }
    public String getMensagem() {
        return mensagem;
    }
}

