package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.ServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarServicoUseCase;
import br.com.grupo99.oficinaservice.domain.model.Servico;
import br.com.grupo99.oficinaservice.domain.repository.ServicoRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServicoApplicationService implements GerenciarServicoUseCase {

    private final ServicoRepository servicoRepository;

    public ServicoApplicationService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    @Override
    @Transactional
    public ServicoResponseDTO create(ServicoRequestDTO requestDTO) {
        Servico servico = construirServico(requestDTO);
        Servico salvo = servicoRepository.save(servico);
        return ServicoResponseDTO.fromDomain(salvo);
    }

    @Override
    @Transactional
    public ServicoResponseDTO update(UUID id, ServicoRequestDTO requestDTO) {
        Servico servico = buscarServicoPorId(id);
        atualizarServico(servico, requestDTO);
        Servico atualizado = servicoRepository.save(servico);
        return ServicoResponseDTO.fromDomain(atualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ServicoResponseDTO getById(UUID id) {
        Servico servico = buscarServicoPorId(id);
        return ServicoResponseDTO.fromDomain(servico);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> getAll() {
        return servicoRepository.findAll().stream()
                .map(ServicoResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!servicoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Serviço não encontrado com o id: " + id);
        }
        servicoRepository.deleteById(id);
    }

    // MÉTODOS AUXILIARES

    private Servico buscarServicoPorId(UUID id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado com o id: " + id));
    }

    private Servico construirServico(ServicoRequestDTO dto) {
        Servico servico = new Servico();
        servico.setDescricao(dto.descricao());
        servico.setPreco(dto.preco());
        return servico;
    }

    private void atualizarServico(Servico servico, ServicoRequestDTO dto) {
        servico.setDescricao(dto.descricao());
        servico.setPreco(dto.preco());
    }
}
