package br.com.grupo99.apiexterna.dto;

import java.util.UUID;

public class AprovacaoOrcamentoRequestDTO {
    private UUID ordemServicoId;
    private boolean aprovado;
    private String motivoRecusa;

    public UUID getOrdemServicoId() {
        return ordemServicoId;
    }
    public void setOrdemServicoId(UUID ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }
    public boolean isAprovado() {
        return aprovado;
    }
    public void setAprovado(boolean aprovado) {
        this.aprovado = aprovado;
    }
    public String getMotivoRecusa() {
        return motivoRecusa;
    }
    public void setMotivoRecusa(String motivoRecusa) {
        this.motivoRecusa = motivoRecusa;
    }
}
