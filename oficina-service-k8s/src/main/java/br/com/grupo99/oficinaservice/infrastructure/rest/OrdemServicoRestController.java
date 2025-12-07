package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.OrdemServicoController;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoDetalhesDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoStatusUpdateRequestDTO;
import br.com.grupo99.oficinaservice.application.exception.OrdemServicoAtivaException;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.infrastructure.security.annotation.RequiresRole;
import br.com.grupo99.oficinaservice.infrastructure.security.jwt.JwtUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Controller REST para Ordens de Serviço.
 * Responsável apenas por receber requisições HTTP e delegar para o Controller da Clean Architecture.
 * 
 * Controle de acesso:
 * - CLIENTE: Pode apenas consultar suas próprias ordens (GET)
 * - MECANICO/ADMIN: Acesso total (GET, POST, PATCH)
 */
@RestController
@RequestMapping("/api/v1/ordens-servico")
@Tag(name = "Ordens de Serviço", description = "APIs para Gerenciamento de Ordens de Serviço")
public class OrdemServicoRestController {

    private final OrdemServicoController ordemServicoController;

    public OrdemServicoRestController(OrdemServicoController ordemServicoController) {
        this.ordemServicoController = ordemServicoController;
    }

    @PostMapping
    @RequiresRole({Perfil.MECANICO, Perfil.ADMIN})
    @Operation(summary = "Cria uma nova Ordem de Serviço", description = "Apenas mecânicos e administradores podem criar ordens de serviço")
    public ResponseEntity<OrdemServicoResponseDTO> create(@Valid @RequestBody OrdemServicoRequestDTO requestDTO) {
        OrdemServicoResponseDTO response = ordemServicoController.criar(requestDTO);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista Ordens de Serviço", description = "Clientes veem apenas suas ordens. Mecânicos veem todas.")
    public ResponseEntity<List<OrdemServicoResponseDTO>> getAll(
            @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        // Se for cliente, filtra apenas suas ordens
        // TODO: Implementar filtro por clienteId no UseCase
        return ResponseEntity.ok(ordemServicoController.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca detalhes de uma Ordem de Serviço", description = "Clientes podem ver apenas suas próprias ordens")
    public ResponseEntity<OrdemServicoDetalhesDTO> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal JwtUserDetails userDetails
    ) {
        // Busca a ordem de serviço
        OrdemServicoDetalhesDTO ordemServico = ordemServicoController.buscarDetalhes(id);
        
        // Se for cliente, valida se a ordem pertence a ele
        if (userDetails.isCliente()) {
            UUID clienteIdFromOS = ordemServico.cliente().id();
            
            if (!userDetails.isOwnerOrMecanico(clienteIdFromOS)) {
                throw new AccessDeniedException("Você só pode acessar suas próprias ordens de serviço");
            }
        }
        
        return ResponseEntity.ok(ordemServico);
    }

    @PatchMapping("/{id}/status")
    @RequiresRole({Perfil.MECANICO, Perfil.ADMIN})
    @Operation(summary = "Atualiza o status de uma Ordem de Serviço", description = "Apenas mecânicos e administradores podem atualizar status")
    public ResponseEntity<OrdemServicoResponseDTO> updateStatus(
            @PathVariable UUID id, 
            @Valid @RequestBody OrdemServicoStatusUpdateRequestDTO requestDTO
    ) {
        OrdemServicoResponseDTO response = ordemServicoController.atualizarStatus(id, requestDTO.novoStatus());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(OrdemServicoAtivaException.class)
    public ResponseEntity<?> handleOrdemServicoAtivaException(OrdemServicoAtivaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new HashMap<>() {{
                put("mensagem", ex.getMessage());
                put("statusAtivo", ex.getStatusAtivo().name());
            }}
        );
    }
}
