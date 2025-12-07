package br.com.grupo99.oficinaservice.application.usecase.impl;

import br.com.grupo99.oficinaservice.adapter.gateway.ClienteGateway;
import br.com.grupo99.oficinaservice.adapter.presenter.ClientePresenter;
import br.com.grupo99.oficinaservice.application.dto.ClienteRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para GerenciarClienteUseCaseImpl")
class GerenciarClienteUseCaseImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private ClientePresenter clientePresenter;

    private GerenciarClienteUseCaseImpl useCase;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        useCase = new GerenciarClienteUseCaseImpl(clienteGateway, clientePresenter);
        clienteId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve criar cliente com sucesso")
    void shouldCreateClienteSuccessfully() {
        // Given
        ClienteRequestDTO requestDTO = new ClienteRequestDTO(
            "João Silva",
            "12345678900",
            "11999887766",
            "joao@email.com"
        );

        Cliente cliente = criarClienteComPessoa("Default Cliente", "12345678900");
        cliente.setId(clienteId);
        cliente.getPessoa().setName("João Silva");

        ClienteResponseDTO responseDTO = new ClienteResponseDTO(
            clienteId,
            "João Silva",
            "123.456.789-00",
            "11999887766",
            "joao@email.com"
        );

        when(clienteGateway.existePorCpfCnpj(anyString())).thenReturn(false);
        when(clienteGateway.existePorEmail(anyString())).thenReturn(false);
        when(clienteGateway.existePorTelefone(anyString())).thenReturn(false);
        when(clienteGateway.salvar(any(Cliente.class))).thenReturn(cliente);
        when(clientePresenter.paraResponseDTO(any(Cliente.class))).thenReturn(responseDTO);

        // When
        ClienteResponseDTO result = useCase.create(requestDTO);

        // Then
        assertThat(result).isNotNull();
        verify(clienteGateway).salvar(any(Cliente.class));
        verify(clientePresenter).paraResponseDTO(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve buscar cliente por ID")
    void shouldGetClienteById() {
        // Given
        Cliente cliente = criarClienteComPessoa("Default Cliente", "12345678900");
        cliente.setId(clienteId);

        ClienteResponseDTO responseDTO = new ClienteResponseDTO(
            clienteId,
            "João Silva",
            "123.456.789-00",
            "11999887766",
            "joao@email.com"
        );

        when(clienteGateway.buscarPorId(clienteId)).thenReturn(Optional.of(cliente));
        when(clientePresenter.paraResponseDTO(cliente)).thenReturn(responseDTO);

        // When
        ClienteResponseDTO result = useCase.getById(clienteId);

        // Then
        assertThat(result).isNotNull();
        verify(clienteGateway).buscarPorId(clienteId);
    }
}

