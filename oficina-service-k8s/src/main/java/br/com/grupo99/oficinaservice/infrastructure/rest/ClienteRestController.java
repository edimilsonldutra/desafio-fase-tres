package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.ClienteController;
import br.com.grupo99.oficinaservice.application.dto.ClienteRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;
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
 * Controller REST para Cliente.
 * Responsável apenas por receber requisições HTTP e delegar para o Controller da Clean Architecture.
 * 
 * Controle de acesso:
 * - Apenas MECANICO e ADMIN podem gerenciar clientes
 */
@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "APIs para Gerenciamento de Clientes")
@RequiresRole({Perfil.MECANICO, Perfil.ADMIN})
public class ClienteRestController {

    private final ClienteController clienteController;

    public ClienteRestController(ClienteController clienteController) {
        this.clienteController = clienteController;
    }

    @PostMapping
    @Operation(summary = "Cria um novo cliente")
    public ResponseEntity<ClienteResponseDTO> create(@Valid @RequestBody ClienteRequestDTO requestDTO) {
        ClienteResponseDTO response = clienteController.criar(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um cliente pelo ID")
    public ResponseEntity<ClienteResponseDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteController.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Lista todos os clientes")
    public ResponseEntity<List<ClienteResponseDTO>> getAll() {
        return ResponseEntity.ok(clienteController.listarTodos());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um cliente existente")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable UUID id, @Valid @RequestBody ClienteRequestDTO requestDTO) {
        return ResponseEntity.ok(clienteController.atualizar(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta um cliente")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        clienteController.deletar(id);
    }
}
