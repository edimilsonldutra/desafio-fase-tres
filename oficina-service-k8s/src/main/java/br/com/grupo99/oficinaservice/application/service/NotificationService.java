package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.domain.model.OrdemServico;

public interface NotificationService {
    void notificarClienteParaAprovacao(OrdemServico os);
    void notificarAtualizacaoStatusOrdemServico(br.com.grupo99.oficinaservice.domain.model.OrdemServico ordemServico, br.com.grupo99.oficinaservice.domain.model.Cliente cliente);
}
