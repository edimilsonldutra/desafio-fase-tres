package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.RelatorioController;
import br.com.grupo99.oficinaservice.application.dto.TempoMedioServicoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller REST para Relatórios.
 * Responsável apenas por receber requisições HTTP e delegar para o Controller da Clean Architecture.
 */
@RestController
@RequestMapping("/api/v1/relatorios")
@Tag(name = "Relatórios", description = "APIs para geração de relatórios e métricas")
public class RelatorioRestController {

    private final RelatorioController relatorioController;

    public RelatorioRestController(RelatorioController relatorioController) {
        this.relatorioController = relatorioController;
    }

    @GetMapping("/tempo-medio/servicos/{servicoId}")
    @Operation(summary = "Calcula o tempo médio de execução para um tipo de serviço")
    public ResponseEntity<TempoMedioServicoResponseDTO> getTempoMedioPorServico(@PathVariable UUID servicoId) {
        TempoMedioServicoResponseDTO response = relatorioController.calcularTempoMedioPorServico(servicoId);
        return ResponseEntity.ok(response);
    }
}
