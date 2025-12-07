package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.PecaController;
import br.com.grupo99.oficinaservice.application.dto.PecaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaUpdateEstoqueRequestDTO;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.infrastructure.security.annotation.RequiresRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller REST para Peças.
 * Responsável apenas por receber requisições HTTP e delegar para o Controller da Clean Architecture.
 * 
 * Controle de acesso:
 * - Apenas MECANICO e ADMIN podem gerenciar peças
 */
@RestController
@RequestMapping("/api/v1/pecas")
@Tag(name = "Peças", description = "APIs para Gerenciamento de Peças e Estoque")
@RequiresRole({Perfil.MECANICO, Perfil.ADMIN})
public class PecaRestController {

    private final PecaController pecaController;

    public PecaRestController(PecaController pecaController) {
        this.pecaController = pecaController;
    }

    @PostMapping
    @Operation(summary = "Cria uma nova peça")
    public ResponseEntity<PecaResponseDTO> create(@Valid @RequestBody PecaRequestDTO requestDTO) {
        PecaResponseDTO response = pecaController.criar(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma peça pelo ID")
    public ResponseEntity<PecaResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(pecaController.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista todas as peças")
    public ResponseEntity<List<PecaResponseDTO>> getAll() {
        return ResponseEntity.ok(pecaController.listarTodos());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma peça existente")
    public ResponseEntity<PecaResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody PecaRequestDTO requestDTO) {
        return ResponseEntity.ok(pecaController.atualizar(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta uma peça")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        pecaController.deletar(id);
    }

    @PatchMapping("/{id}/estoque")
    @Operation(summary = "Adiciona ou remove itens do estoque de uma peça")
    public ResponseEntity<PecaResponseDTO> updateEstoque(@PathVariable UUID id, @Valid @RequestBody PecaUpdateEstoqueRequestDTO requestDTO) {
        PecaResponseDTO response = pecaController.adicionarEstoque(id, requestDTO.quantidade());
        return ResponseEntity.ok(response);
    }
}
