package br.com.grupo99.oficinaservice.application.usecase.impl;

import br.com.grupo99.oficinaservice.adapter.gateway.ClienteGateway;
import br.com.grupo99.oficinaservice.adapter.presenter.ClientePresenter;
import br.com.grupo99.oficinaservice.application.dto.ClienteRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.BusinessException;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarClienteUseCase;
import br.com.grupo99.oficinaservice.application.util.DocumentoUtils;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;

import java.util.List;
import java.util.UUID;

/**
 * Implementação do caso de uso de Gerenciamento de Clientes.
 * Contém toda a lógica de negócio relacionada a clientes.
 * Utiliza Gateway para acesso a dados e Presenter para formatação de saída.
 */
public class GerenciarClienteUseCaseImpl implements GerenciarClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final ClientePresenter clientePresenter;

    public GerenciarClienteUseCaseImpl(ClienteGateway clienteGateway,
                                       ClientePresenter clientePresenter) {
        this.clienteGateway = clienteGateway;
        this.clientePresenter = clientePresenter;
    }

    @Override
    public ClienteResponseDTO create(ClienteRequestDTO requestDTO) {
        validarCamposObrigatorios(requestDTO);
        verificarDuplicidadeCpfCnpj(requestDTO.cpfCnpj());
        verificarDuplicidadeEmail(requestDTO.email());
        verificarDuplicidadeTelefone(requestDTO.telefone());

        Cliente cliente = mapearParaEntidade(requestDTO);
        Cliente clienteSalvo = clienteGateway.salvar(cliente);

        // Usa o Presenter para converter a entidade em DTO
        return clientePresenter.paraResponseDTO(clienteSalvo);
    }

    @Override
    public ClienteResponseDTO update(UUID id, ClienteRequestDTO requestDTO) {
        Cliente clienteExistente = buscarClientePorId(id);

        // Verifica duplicidade de e-mail e telefone para outros clientes
        if (!clienteExistente.getPessoa().getEmail().equals(requestDTO.email())) {
            verificarDuplicidadeEmail(requestDTO.email());
        }
        if (!clienteExistente.getPessoa().getPhone().equals(requestDTO.telefone())) {
            verificarDuplicidadeTelefone(requestDTO.telefone());
        }

        atualizarDadosCliente(clienteExistente, requestDTO);
        Cliente clienteAtualizado = clienteGateway.salvar(clienteExistente);

        return clientePresenter.paraResponseDTO(clienteAtualizado);
    }

    @Override
    public ClienteResponseDTO getById(UUID id) {
        Cliente cliente = buscarClientePorId(id);
        return clientePresenter.paraResponseDTO(cliente);
    }

    @Override
    public List<ClienteResponseDTO> getAll() {
        List<Cliente> clientes = clienteGateway.buscarTodos();
        return clientePresenter.paraListaResponseDTO(clientes);
    }

    @Override
    public void delete(UUID id) {
        if (!clienteGateway.existePorId(id)) {
            throw new ResourceNotFoundException("Cliente não encontrado com o id: " + id);
        }
        clienteGateway.deletarPorId(id);
    }

    // MÉTODOS AUXILIARES (Lógica de Negócio)

    private void validarCamposObrigatorios(ClienteRequestDTO dto) {
        if (isBlank(dto.nome())) throw new BusinessException("Nome é obrigatório.");
        if (isBlank(dto.cpfCnpj())) throw new BusinessException("CPF/CNPJ é obrigatório.");
        if (isBlank(dto.email())) throw new BusinessException("E-mail é obrigatório.");
        if (isBlank(dto.telefone())) throw new BusinessException("Telefone é obrigatório.");
    }

    private void verificarDuplicidadeCpfCnpj(String cpfCnpj) {
        if (clienteGateway.existePorCpfCnpj(DocumentoUtils.removerMascara(cpfCnpj))) {
            throw new BusinessException("Já existe um cliente com este CPF/CNPJ.");
        }
    }

    private void verificarDuplicidadeEmail(String email) {
        if (clienteGateway.existePorEmail(email)) {
            throw new BusinessException("Já existe um cliente com este e-mail.");
        }
    }

    private void verificarDuplicidadeTelefone(String telefone) {
        if (clienteGateway.existePorTelefone(telefone)) {
            throw new BusinessException("Já existe um cliente com este telefone.");
        }
    }

    private Cliente buscarClientePorId(UUID id) {
        return clienteGateway.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o id: " + id));
    }

    private Cliente mapearParaEntidade(ClienteRequestDTO dto) {
        Pessoa pessoa = new Pessoa(
            DocumentoUtils.removerMascara(dto.cpfCnpj()),
            dto.cpfCnpj().length() <= 11 ? TipoPessoa.FISICA : TipoPessoa.JURIDICA,
            dto.nome(),
            dto.email(),
            Perfil.CLIENTE
        );
        pessoa.setPhone(dto.telefone());
        Cliente cliente = new Cliente(pessoa);
        return cliente;
    }

    private void atualizarDadosCliente(Cliente cliente, ClienteRequestDTO dto) {
        Pessoa pessoa = cliente.getPessoa();
        pessoa.setName(dto.nome());
        pessoa.setNumeroDocumento(DocumentoUtils.removerMascara(dto.cpfCnpj()));
        pessoa.setEmail(dto.email());
        pessoa.setPhone(dto.telefone());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

