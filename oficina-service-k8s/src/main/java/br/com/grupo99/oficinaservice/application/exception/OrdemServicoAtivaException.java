package br.com.grupo99.oficinaservice.application.exception;

import br.com.grupo99.oficinaservice.domain.model.StatusOS;

public class OrdemServicoAtivaException extends RuntimeException {
    private final StatusOS statusAtivo;

    public OrdemServicoAtivaException(String message, StatusOS statusAtivo) {
        super(message);
        this.statusAtivo = statusAtivo;
    }

    public StatusOS getStatusAtivo() {
        return statusAtivo;
    }
}

