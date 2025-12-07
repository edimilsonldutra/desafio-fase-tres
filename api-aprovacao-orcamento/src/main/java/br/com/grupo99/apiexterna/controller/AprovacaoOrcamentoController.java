package br.com.grupo99.apiexterna.controller;

import br.com.grupo99.apiexterna.dto.AprovacaoOrcamentoRequestDTO;
import br.com.grupo99.apiexterna.domain.AprovacaoOrcamento;
import br.com.grupo99.apiexterna.presenter.AprovacaoOrcamentoResponseDTO;
import br.com.grupo99.apiexterna.service.AprovacaoOrcamentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notificacoes-aprovacao")
public class AprovacaoOrcamentoController {

    private final AprovacaoOrcamentoService service;

    public AprovacaoOrcamentoController(AprovacaoOrcamentoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AprovacaoOrcamentoResponseDTO> receberNotificacao(@RequestBody @Valid AprovacaoOrcamentoRequestDTO dto) {
        AprovacaoOrcamento aprovacao = new AprovacaoOrcamento(dto.getOrdemServicoId(), dto.isAprovado(), dto.getMotivoRecusa());
        AprovacaoOrcamentoResponseDTO response = service.processarNotificacao(aprovacao);
        return ResponseEntity.ok(response);
    }
}
