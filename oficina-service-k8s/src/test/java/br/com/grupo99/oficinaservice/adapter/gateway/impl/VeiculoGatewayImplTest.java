package br.com.grupo99.oficinaservice.adapter.gateway.impl;

import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import br.com.grupo99.oficinaservice.domain.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para VeiculoGatewayImpl")
class VeiculoGatewayImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private VeiculoGatewayImpl veiculoGateway;

    private Veiculo veiculo;
    private UUID veiculoId;

    @BeforeEach
    void setUp() {
        veiculoId = UUID.randomUUID();
        veiculo = new Veiculo("ABC1D23", "Toyota", "Corolla", 2023);
        veiculo.setId(veiculoId);
        veiculo.setRenavam("12345678901");
    }

    @Test
    @DisplayName("Deve salvar veículo através do repositório")
    void shouldSaveVeiculo() {
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);
        Veiculo result = veiculoGateway.salvar(veiculo);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(veiculoId);
        verify(veiculoRepository).save(veiculo);
    }

    @Test
    @DisplayName("Deve buscar veículo por ID")
    void shouldFindVeiculoById() {
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));
        Optional<Veiculo> result = veiculoGateway.buscarPorId(veiculoId);
        assertThat(result).isPresent();
        verify(veiculoRepository).findById(veiculoId);
    }
}

