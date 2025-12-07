package br.com.grupo99.oficinaservice.adapter.presenter;

import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Cliente;

import java.util.List;

/**
 * Presenter para Cliente.
 * Responsável por converter entidades de domínio em DTOs de resposta,
 * mantendo a separação entre as camadas.
 */
public interface ClientePresenter {
    ClienteResponseDTO paraResponseDTO(Cliente cliente);
    List<ClienteResponseDTO> paraListaResponseDTO(List<Cliente> clientes);
}

