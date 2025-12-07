package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.ClienteRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Teste de Integração Completo - ClienteApplicationService")
class ClienteApplicationServiceIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Autowired
    private ClienteApplicationService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    @DisplayName("Dado um DTO de cliente válido, Quando criar, Então o cliente deve ser salvo")
    void givenValidClienteRequest_whenCreate_thenShouldSaveCliente() {
        // Given (Dado)
        ClienteRequestDTO request = new ClienteRequestDTO("Cliente de Teste", "123.456.789-00", "11999998888", "teste@email.com");

        // When (Quando)
        ClienteResponseDTO response = clienteService.create(request);

        // Then (Então)
        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo("Cliente de Teste");
        assertTrue(clienteRepository.findById(response.id()).isPresent());
    }

    @Test
    @DisplayName("Dado um CPF/CNPJ existente, Quando criar um novo cliente com o mesmo CPF/CNPJ, Então deve lançar exceção")
    void givenExistingCpfCnpj_whenCreateWithSameCpfCnpj_thenShouldThrowException() {
        // Given (Dado)
        clienteRepository.save(criarClienteComPessoa("Primeiro Cliente", "11122233344"));
        ClienteRequestDTO requestDuplicado = new ClienteRequestDTO("Segundo Cliente", "111.222.333-44", "11987654321", "segundo@email.com");

        // When & Then (Quando e Então)
        assertThrows(DataIntegrityViolationException.class, () -> {
            clienteService.create(requestDuplicado);
        });
    }

    @Test
    @DisplayName("Dado um cliente existente, Quando buscar pelo ID, Então deve retornar o cliente correto")
    void givenExistingCliente_whenGetById_thenShouldReturnCliente() {
        // Given (Dado)
        Cliente cliente = clienteRepository.save(criarClienteComPessoa("Cliente para Busca", "11111111111"));

        // When (Quando)
        ClienteResponseDTO response = clienteService.getById(cliente.getId());

        // Then (Então)
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(cliente.getId());
    }

    @Test
    @DisplayName("Dado um ID inexistente, Quando buscar pelo ID, Então deve lançar exceção")
    void givenNonExistingId_whenGetById_thenShouldThrowException() {
        // Given (Dado)
        UUID idInexistente = UUID.randomUUID();

        // When & Then (Quando e Então)
        assertThrows(ResourceNotFoundException.class, () -> clienteService.getById(idInexistente));
    }

    @Test
    @DisplayName("Dado que existem clientes no banco, Quando listar todos, Então deve retornar a lista de clientes")
    void givenClientesExist_whenGetAll_thenShouldReturnClienteList() {
        // Given (Dado)
        clienteRepository.save(criarClienteComPessoa("Cliente A", "22222222222"));
        clienteRepository.save(criarClienteComPessoa("Cliente B", "33333333333"));

        // When (Quando)
        List<ClienteResponseDTO> clientes = clienteService.getAll();

        // Then (Então)
        assertThat(clientes).hasSize(2);
    }

    @Test
    @DisplayName("Dado um cliente existente e um DTO com novos dados, Quando atualizar, Então o cliente deve ser atualizado")
    void givenExistingClienteAndNewData_whenUpdate_thenShouldUpdateCliente() {
        // Given (Dado)
        Cliente clienteExistente = clienteRepository.save(criarClienteComPessoa("Nome Antigo", "44444444444"));
        ClienteRequestDTO request = new ClienteRequestDTO("Nome Novo", "444.444.444-44", "11222223333", "novo@email.com");

        // When (Quando)
        ClienteResponseDTO response = clienteService.update(clienteExistente.getId(), request);

        // Then (Então)
        assertThat(response.id()).isEqualTo(clienteExistente.getId());
        assertThat(response.nome()).isEqualTo("Nome Novo");
    }

    @Test
    @DisplayName("Dado um ID inexistente, Quando atualizar, Então deve lançar exceção")
    void givenNonExistingId_whenUpdate_thenShouldThrowException() {
        // Given (Dado)
        ClienteRequestDTO request = new ClienteRequestDTO("Nome", "cpf", "tel", "email");
        UUID idInexistente = UUID.randomUUID();

        // When & Then (Quando e Então)
        assertThrows(ResourceNotFoundException.class, () -> clienteService.update(idInexistente, request));
    }

    @Test
    @DisplayName("Dado um cliente existente, Quando deletar, Então o cliente não deve mais existir")
    void givenExistingCliente_whenDelete_thenShouldNotExistAnymore() {
        // Given (Dado)
        Cliente cliente = clienteRepository.save(criarClienteComPessoa("Cliente a ser Deletado", "55555555555"));
        UUID clienteId = cliente.getId();

        // When (Quando)
        assertDoesNotThrow(() -> clienteService.delete(clienteId));

        // Then (Então)
        assertFalse(clienteRepository.existsById(clienteId));
    }

    @Test
    @DisplayName("Dado um ID inexistente, Quando deletar, Então deve lançar exceção")
    void givenNonExistingId_whenDelete_thenShouldThrowException() {
        // Given (Dado)
        UUID idInexistente = UUID.randomUUID();

        // When & Then (Quando e Então)
        assertThrows(ResourceNotFoundException.class, () -> clienteService.delete(idInexistente));
    }
}