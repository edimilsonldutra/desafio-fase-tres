package br.com.grupo99.oficinaservice.application.usecase.impl;

import br.com.grupo99.oficinaservice.adapter.gateway.VeiculoGateway;
import br.com.grupo99.oficinaservice.adapter.gateway.ClienteGateway;
import br.com.grupo99.oficinaservice.adapter.presenter.VeiculoPresenter;
import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.BusinessException;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarVeiculoUseCase;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;

import java.util.List;
import java.util.UUID;

/**
 * Implementação do caso de uso de Gerenciamento de Veículos.
 */
public class GerenciarVeiculoUseCaseImpl implements GerenciarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final ClienteGateway clienteGateway;
    private final VeiculoPresenter veiculoPresenter;

    public GerenciarVeiculoUseCaseImpl(VeiculoGateway veiculoGateway,
                                       ClienteGateway clienteGateway,
                                       VeiculoPresenter veiculoPresenter) {
        this.veiculoGateway = veiculoGateway;
        this.clienteGateway = clienteGateway;
        this.veiculoPresenter = veiculoPresenter;
    }

    @Override
    public VeiculoResponseDTO create(VeiculoRequestDTO requestDTO) {
        validarCamposObrigatorios(requestDTO);

        // Buscar cliente pelo ID
        Cliente cliente = clienteGateway.buscarPorId(requestDTO.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente não encontrado com ID: " + requestDTO.clienteId()));

        Veiculo veiculo = new Veiculo(
                requestDTO.placa(),
                requestDTO.marca(),
                requestDTO.modelo(),
                requestDTO.ano()
        );
        veiculo.setRenavam(requestDTO.renavam());
        veiculo.setCliente(cliente);

        Veiculo veiculoSalvo = veiculoGateway.salvar(veiculo);
        return veiculoPresenter.paraResponseDTO(veiculoSalvo);
    }

    @Override
    public VeiculoResponseDTO update(UUID id, VeiculoRequestDTO requestDTO) {
        Veiculo veiculoExistente = buscarVeiculoPorId(id);

        veiculoExistente.setPlaca(requestDTO.placa());
        veiculoExistente.setMarca(requestDTO.marca());
        veiculoExistente.setModelo(requestDTO.modelo());
        veiculoExistente.setAno(requestDTO.ano());
        veiculoExistente.setRenavam(requestDTO.renavam());

        Veiculo veiculoAtualizado = veiculoGateway.salvar(veiculoExistente);
        return veiculoPresenter.paraResponseDTO(veiculoAtualizado);
    }

    @Override
    public VeiculoResponseDTO getById(UUID id) {
        Veiculo veiculo = buscarVeiculoPorId(id);
        return veiculoPresenter.paraResponseDTO(veiculo);
    }

    @Override
    public List<VeiculoResponseDTO> getAll() {
        List<Veiculo> veiculos = veiculoGateway.buscarTodos();
        return veiculoPresenter.paraListaResponseDTO(veiculos);
    }

    @Override
    public void delete(UUID id) {
        if (!veiculoGateway.existePorId(id)) {
            throw new ResourceNotFoundException("Veículo não encontrado com o id: " + id);
        }
        veiculoGateway.deletarPorId(id);
    }

    // Métodos auxiliares

    private void validarCamposObrigatorios(VeiculoRequestDTO dto) {
        if (isBlank(dto.placa())) throw new BusinessException("Placa é obrigatória.");
        if (isBlank(dto.marca())) throw new BusinessException("Marca é obrigatória.");
        if (isBlank(dto.modelo())) throw new BusinessException("Modelo é obrigatório.");
        if (dto.ano() == null) throw new BusinessException("Ano é obrigatório.");
    }

    private Veiculo buscarVeiculoPorId(UUID id) {
        return veiculoGateway.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado com o id: " + id));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

