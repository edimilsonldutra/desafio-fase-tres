package br.com.grupo99.oficinaservice.infrastructure.rest;

import br.com.grupo99.oficinaservice.adapter.controller.PessoaController;
import br.com.grupo99.oficinaservice.application.dto.PessoaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PessoaResponseDTO;
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
 * Controller REST para Pessoas.
 * Responsável apenas por receber requisições HTTP e delegar para o Controller da Clean Architecture.
 */
@RestController
@RequestMapping("/api/v1/pessoas")
@Tag(name = "Pessoas", description = "APIs para Gerenciamento de Pessoas")
public class PessoaRestController {

    private final PessoaController pessoaController;

    public PessoaRestController(PessoaController pessoaController) {
        this.pessoaController = pessoaController;
    }

    @PostMapping
    @RequiresRole({Perfil.ADMIN})
    @Operation(summary = "Cria uma nova Pessoa", description = "Apenas administradores podem criar pessoas")
    public ResponseEntity<PessoaResponseDTO> createPessoa(@Valid @RequestBody PessoaRequestDTO request) {
        PessoaResponseDTO response = pessoaController.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @RequiresRole({Perfil.MECANICO, Perfil.ADMIN})
    @Operation(summary = "Busca uma Pessoa por ID", description = "Requer perfil MECANICO ou ADMIN")
    public ResponseEntity<PessoaResponseDTO> getPessoaById(@PathVariable UUID id) {
        PessoaResponseDTO response = pessoaController.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @RequiresRole({Perfil.MECANICO, Perfil.ADMIN})
    @Operation(summary = "Lista todas as Pessoas", description = "Requer perfil MECANICO ou ADMIN")
    public ResponseEntity<List<PessoaResponseDTO>> listAllPessoas() {
        List<PessoaResponseDTO> pessoas = pessoaController.listarTodos();
        return ResponseEntity.ok(pessoas);
    }

    @PutMapping("/{id}")
    @RequiresRole({Perfil.ADMIN})
    @Operation(summary = "Atualiza uma Pessoa existente", description = "Apenas administradores podem atualizar pessoas")
    public ResponseEntity<PessoaResponseDTO> updatePessoa(
            @PathVariable UUID id,
            @Valid @RequestBody PessoaRequestDTO request) {
        PessoaResponseDTO response = pessoaController.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @RequiresRole({Perfil.ADMIN})
    @Operation(summary = "Deleta uma Pessoa", description = "Apenas administradores podem deletar pessoas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePessoa(@PathVariable UUID id) {
        pessoaController.deletar(id);
    }
}
