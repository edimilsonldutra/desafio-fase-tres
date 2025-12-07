package br.com.grupo99.oficinaservice.adapter.presenter.impl;

import br.com.grupo99.oficinaservice.adapter.presenter.ClientePresenter;
import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;
import br.com.grupo99.oficinaservice.application.util.DocumentoUtils;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação do Presenter de Cliente.
 * Converte entidades de domínio (Cliente) em DTOs de resposta (ClienteResponseDTO).
 */
@Component
public class ClientePresenterImpl implements ClientePresenter {

    @Override
    public ClienteResponseDTO paraResponseDTO(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getPessoa().getName(),
                DocumentoUtils.aplicarMascara(cliente.getPessoa().getNumeroDocumento()),
                cliente.getPessoa().getPhone(),
                cliente.getPessoa().getEmail()
        );
    }

    @Override
    public List<ClienteResponseDTO> paraListaResponseDTO(List<Cliente> clientes) {
        if (clientes == null) {
            return List.of();
        }

        return clientes.stream()
                .map(this::paraResponseDTO)
                .collect(Collectors.toList());
    }
}

