package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.ServicoController;
import br.com.grupo99.oficinaservice.application.dto.ServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ServicoResponseDTO;
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
 * Controller REST para Serviços.
 * Responsável apenas por receber requisições HTTP e delegar para o Controller da Clean Architecture.
 * 
 * Controle de acesso:
 * - Apenas MECANICO e ADMIN podem gerenciar serviços
 */
@RestController
@RequestMapping("/api/v1/servicos")
@Tag(name = "Serviços", description = "APIs para Gerenciamento de Serviços")
@RequiresRole({Perfil.MECANICO, Perfil.ADMIN})
public class ServicoRestController {

    private final ServicoController servicoController;

    public ServicoRestController(ServicoController servicoController) {
        this.servicoController = servicoController;
    }

    @PostMapping
    @Operation(summary = "Cria um novo serviço")
    public ResponseEntity<ServicoResponseDTO> create(@Valid @RequestBody ServicoRequestDTO requestDTO) {
        ServicoResponseDTO response = servicoController.criar(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um serviço pelo ID")
    public ResponseEntity<ServicoResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(servicoController.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista todos os serviços")
    public ResponseEntity<List<ServicoResponseDTO>> getAll() {
        return ResponseEntity.ok(servicoController.listarTodos());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um serviço existente")
    public ResponseEntity<ServicoResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody ServicoRequestDTO requestDTO) {
        return ResponseEntity.ok(servicoController.atualizar(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta um serviço")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        servicoController.deletar(id);
    }
}
