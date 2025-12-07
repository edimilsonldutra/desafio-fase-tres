package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.PessoaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PessoaResponseDTO;
import br.com.grupo99.oficinaservice.application.usecase.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Controller de Pessoa na camada de Adapter.
 * Responsável por orquestrar chamadas aos casos de uso.
 * Segue o padrão Clean Architecture, delegando a lógica de negócio aos UseCases.
 */
@Component
public class PessoaController {

    private final CreatePessoaUseCase createPessoaUseCase;
    private final GetPessoaByIdUseCase getPessoaByIdUseCase;
    private final ListAllPessoasUseCase listAllPessoasUseCase;
    private final UpdatePessoaUseCase updatePessoaUseCase;
    private final DeletePessoaUseCase deletePessoaUseCase;

    public PessoaController(CreatePessoaUseCase createPessoaUseCase,
                            GetPessoaByIdUseCase getPessoaByIdUseCase,
                            ListAllPessoasUseCase listAllPessoasUseCase,
                            UpdatePessoaUseCase updatePessoaUseCase,
                            DeletePessoaUseCase deletePessoaUseCase) {
        this.createPessoaUseCase = createPessoaUseCase;
        this.getPessoaByIdUseCase = getPessoaByIdUseCase;
        this.listAllPessoasUseCase = listAllPessoasUseCase;
        this.updatePessoaUseCase = updatePessoaUseCase;
        this.deletePessoaUseCase = deletePessoaUseCase;
    }

    public PessoaResponseDTO criar(PessoaRequestDTO request) {
        return createPessoaUseCase.execute(request);
    }

    public PessoaResponseDTO buscarPorId(UUID id) {
        return getPessoaByIdUseCase.execute(id);
    }

    public List<PessoaResponseDTO> listarTodos() {
        return listAllPessoasUseCase.execute();
    }

    public PessoaResponseDTO atualizar(UUID id, PessoaRequestDTO request) {
        return updatePessoaUseCase.execute(id, request);
    }

    public void deletar(UUID id) {
        deletePessoaUseCase.execute(id);
    }
}
