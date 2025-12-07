package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import br.com.grupo99.oficinaservice.domain.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Teste de Integração Completo - VeiculoApplicationService")
class VeiculoApplicationServiceIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Autowired
    private VeiculoApplicationService veiculoService;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private VeiculoRepository veiculoRepository;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = clienteRepository.save(criarClienteComPessoa("Dono de Veículo", "77788899900"));
    }

    @Test
    @DisplayName("Deve criar um veículo para um cliente existente")
    void deveCriarVeiculo() {
        VeiculoRequestDTO request = new VeiculoRequestDTO("VEI-2024", "12345678901", "VW", "Nivus", 2024, cliente.getId());
        VeiculoResponseDTO response = veiculoService.create(request);

        assertThat(response).isNotNull();
        assertThat(response.placa()).isEqualTo("VEI-2024");
        assertThat(veiculoRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar veículo para cliente inexistente")
    void deveLancarExcecaoAoCriarVeiculoParaClienteInexistente() {
        VeiculoRequestDTO request = new VeiculoRequestDTO("VEI-FAIL", "12345678901", "Marca", "Modelo", 2024, UUID.randomUUID());
        assertThrows(ResourceNotFoundException.class, () -> veiculoService.create(request));
    }
}