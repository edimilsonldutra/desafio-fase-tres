package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.VeiculoController;
import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
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
 * Controller REST para Veículo.
 * Responsável apenas por receber requisições HTTP e delegar para o Controller da Clean Architecture.
 * 
 * Controle de acesso:
 * - Apenas MECANICO e ADMIN podem gerenciar veículos
 */
@RestController
@RequestMapping("/api/v1/veiculos")
@Tag(name = "Veículos", description = "APIs para Gerenciamento de Veículos")
@RequiresRole({Perfil.MECANICO, Perfil.ADMIN})
public class VeiculoRestController {

    private final VeiculoController veiculoController;

    public VeiculoRestController(VeiculoController veiculoController) {
        this.veiculoController = veiculoController;
    }

    @PostMapping
    @Operation(summary = "Cria um novo veículo")
    public ResponseEntity<VeiculoResponseDTO> create(@Valid @RequestBody VeiculoRequestDTO requestDTO) {
        VeiculoResponseDTO response = veiculoController.criar(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um veículo pelo ID")
    public ResponseEntity<VeiculoResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(veiculoController.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista todos os veículos")
    public ResponseEntity<List<VeiculoResponseDTO>> getAll() {
        return ResponseEntity.ok(veiculoController.listarTodos());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um veículo existente")
    public ResponseEntity<VeiculoResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody VeiculoRequestDTO requestDTO) {
        return ResponseEntity.ok(veiculoController.atualizar(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta um veículo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        veiculoController.deletar(id);
    }
}
