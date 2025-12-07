package br.com.grupo99.apiexterna.domain;

import java.util.UUID;

public class AprovacaoOrcamento {
    private UUID ordemServicoId;
    private boolean aprovado;
    private String motivoRecusa;

    public AprovacaoOrcamento(UUID ordemServicoId, boolean aprovado, String motivoRecusa) {
        this.ordemServicoId = ordemServicoId;
        this.aprovado = aprovado;
        this.motivoRecusa = motivoRecusa;
    }

    public UUID getOrdemServicoId() {
        return ordemServicoId;
    }
    public boolean isAprovado() {
        return aprovado;
    }
    public String getMotivoRecusa() {
        return motivoRecusa;
    }
}

