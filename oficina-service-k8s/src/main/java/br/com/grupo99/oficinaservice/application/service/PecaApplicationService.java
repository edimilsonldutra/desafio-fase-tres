package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.PecaRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.PecaResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarPecaUseCase;
import br.com.grupo99.oficinaservice.domain.model.Peca;
import br.com.grupo99.oficinaservice.domain.repository.PecaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PecaApplicationService implements GerenciarPecaUseCase {

    private final PecaRepository pecaRepository;

    public PecaApplicationService(PecaRepository pecaRepository) {
        this.pecaRepository = pecaRepository;
    }

    @Override
    @Transactional
    public PecaResponseDTO create(PecaRequestDTO requestDTO) {
        Peca peca = construirPeca(requestDTO);
        Peca salva = pecaRepository.save(peca);
        return PecaResponseDTO.fromDomain(salva);
    }

    @Override
    @Transactional
    public PecaResponseDTO update(UUID id, PecaRequestDTO requestDTO) {
        Peca peca = buscarPecaPorId(id);
        atualizarPeca(peca, requestDTO);
        Peca atualizada = pecaRepository.save(peca);
        return PecaResponseDTO.fromDomain(atualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public PecaResponseDTO getById(UUID id) {
        Peca peca = buscarPecaPorId(id);
        return PecaResponseDTO.fromDomain(peca);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PecaResponseDTO> getAll() {
        return pecaRepository.findAll().stream()
                .map(PecaResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!pecaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Peça não encontrada com o id: " + id);
        }
        pecaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PecaResponseDTO adicionarEstoque(UUID id, int quantidade) {
        Peca peca = buscarPecaPorId(id);
        peca.adicionarEstoque(quantidade);
        Peca atualizada = pecaRepository.save(peca);
        return PecaResponseDTO.fromDomain(atualizada);
    }

    // MÉTODOS AUXILIARES

    private Peca buscarPecaPorId(UUID id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Peça não encontrada com o id: " + id));
    }

    private Peca construirPeca(PecaRequestDTO dto) {
        Peca peca = new Peca();
        peca.setNome(dto.nome());
        peca.setFabricante(dto.fabricante());
        peca.setPreco(dto.preco());
        peca.setEstoque(dto.estoque());
        return peca;
    }

    private void atualizarPeca(Peca peca, PecaRequestDTO dto) {
        peca.setNome(dto.nome());
        peca.setFabricante(dto.fabricante());
        peca.setPreco(dto.preco());
        peca.setEstoque(dto.estoque());
    }
}
