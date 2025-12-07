package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarVeiculoUseCase;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import br.com.grupo99.oficinaservice.domain.repository.VeiculoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VeiculoApplicationService implements GerenciarVeiculoUseCase {

    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    public VeiculoApplicationService(
            VeiculoRepository veiculoRepository,
            ClienteRepository clienteRepository
    ) {
        this.veiculoRepository = veiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public VeiculoResponseDTO create(VeiculoRequestDTO requestDTO) {
        Cliente cliente = buscarClientePorId(requestDTO.clienteId());
        Veiculo veiculo = construirVeiculo(requestDTO, cliente);
        Veiculo salvo = veiculoRepository.save(veiculo);
        return VeiculoResponseDTO.fromDomain(salvo);
    }

    @Override
    @Transactional
    public VeiculoResponseDTO update(UUID id, VeiculoRequestDTO requestDTO) {
        Veiculo veiculo = buscarVeiculoPorId(id);
        Cliente cliente = buscarClientePorId(requestDTO.clienteId());
        atualizarVeiculo(veiculo, requestDTO, cliente);
        Veiculo atualizado = veiculoRepository.save(veiculo);
        return VeiculoResponseDTO.fromDomain(atualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public VeiculoResponseDTO getById(UUID id) {
        Veiculo veiculo = buscarVeiculoPorId(id);
        return VeiculoResponseDTO.fromDomain(veiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VeiculoResponseDTO> getAll() {
        return veiculoRepository.findAll().stream()
                .map(VeiculoResponseDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!veiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Veículo não encontrado com o id: " + id);
        }
        veiculoRepository.deleteById(id);
    }

    // MÉTODOS AUXILIARES

    private Cliente buscarClientePorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o id: " + id));
    }

    private Veiculo buscarVeiculoPorId(UUID id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com o id: " + id));
    }

    private Veiculo construirVeiculo(VeiculoRequestDTO dto, Cliente cliente) {
        Veiculo veiculo = new Veiculo();
        veiculo.setPlaca(dto.placa());
        veiculo.setMarca(dto.marca());
        veiculo.setModelo(dto.modelo());
        veiculo.setRenavam(dto.renavam());
        veiculo.setAno(dto.ano());
        veiculo.setCliente(cliente);
        return veiculo;
    }

    private void atualizarVeiculo(Veiculo veiculo, VeiculoRequestDTO dto, Cliente cliente) {
        veiculo.setPlaca(dto.placa());
        veiculo.setMarca(dto.marca());
        veiculo.setModelo(dto.modelo());
        veiculo.setRenavam(dto.renavam());
        veiculo.setAno(dto.ano());
        veiculo.setCliente(cliente);
    }
}
