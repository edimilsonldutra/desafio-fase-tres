package br.com.grupo99.oficinaservice.application.usecase.impl;

import br.com.grupo99.oficinaservice.adapter.gateway.ClienteGateway;
import br.com.grupo99.oficinaservice.adapter.gateway.VeiculoGateway;
import br.com.grupo99.oficinaservice.adapter.presenter.VeiculoPresenter;
import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;
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
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para GerenciarVeiculoUseCaseImpl")
class GerenciarVeiculoUseCaseImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private VeiculoGateway veiculoGateway;

    @Mock
    private ClienteGateway clienteGateway;

    @Mock
    private VeiculoPresenter veiculoPresenter;

    private GerenciarVeiculoUseCaseImpl useCase;
    private UUID veiculoId;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        useCase = new GerenciarVeiculoUseCaseImpl(veiculoGateway, clienteGateway, veiculoPresenter);
        veiculoId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve criar veículo com sucesso")
    void shouldCreateVeiculoSuccessfully() {
        // Given
        VeiculoRequestDTO requestDTO = new VeiculoRequestDTO(
            "ABC1D23",
            "12345678901",
            "Toyota",
            "Corolla",
            2023,
            clienteId
        );

        Cliente cliente = criarClienteComPessoa("Default Cliente", "12345678900");
        cliente.setId(clienteId);

        Veiculo veiculo = new Veiculo("ABC1D23", "Toyota", "Corolla", 2023);
        veiculo.setId(veiculoId);

        VeiculoResponseDTO responseDTO = new VeiculoResponseDTO(
            veiculoId,
            "ABC1D23",
            "12345678901",
            "Toyota",
            "Corolla",
            2023
        );

        when(clienteGateway.buscarPorId(clienteId)).thenReturn(Optional.of(cliente));
        when(veiculoGateway.salvar(any(Veiculo.class))).thenReturn(veiculo);
        when(veiculoPresenter.paraResponseDTO(any(Veiculo.class))).thenReturn(responseDTO);

        // When
        VeiculoResponseDTO result = useCase.create(requestDTO);

        // Then
        assertThat(result).isNotNull();
        verify(clienteGateway).buscarPorId(clienteId);
        verify(veiculoGateway).salvar(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve buscar veículo por ID")
    void shouldGetVeiculoById() {
        // Given
        Veiculo veiculo = new Veiculo("ABC1D23", "Toyota", "Corolla", 2023);
        veiculo.setId(veiculoId);

        VeiculoResponseDTO responseDTO = new VeiculoResponseDTO(
            veiculoId,
            "ABC1D23",
            "12345678901",
            "Toyota",
            "Corolla",
            2023
        );

        when(veiculoGateway.buscarPorId(veiculoId)).thenReturn(Optional.of(veiculo));
        when(veiculoPresenter.paraResponseDTO(veiculo)).thenReturn(responseDTO);

        // When
        VeiculoResponseDTO result = useCase.getById(veiculoId);

        // Then
        assertThat(result).isNotNull();
        verify(veiculoGateway).buscarPorId(veiculoId);
    }
}

