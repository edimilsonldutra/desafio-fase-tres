package br.com.grupo99.oficinaservice.adapter.controller;

import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.application.usecase.GerenciarVeiculoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para VeiculoController (Clean Architecture)")
class VeiculoControllerTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private GerenciarVeiculoUseCase gerenciarVeiculoUseCase;

    private VeiculoController controller;
    private UUID veiculoId;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        controller = new VeiculoController(gerenciarVeiculoUseCase);
        veiculoId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Deve injetar dependências corretamente")
    void shouldInjectDependenciesCorrectly() {
        assertThat(controller).isNotNull();
    }
}

