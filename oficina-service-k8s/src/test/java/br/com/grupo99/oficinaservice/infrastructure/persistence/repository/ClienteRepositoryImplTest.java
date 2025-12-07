package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.ClienteJpaRepository;
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
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteRepositoryImpl - Testes Unitários")
class ClienteRepositoryImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private ClienteJpaRepository jpaRepository;

    @InjectMocks
    private ClienteRepositoryImpl clienteRepository;

    private Cliente cliente;
    private UUID clienteId;
    private String cpfCnpj;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cpfCnpj = "12345678901";
        
        cliente = criarClienteComPessoa("Default Cliente", "12345678900");
        cliente.setId(clienteId);
        cliente.getPessoa().setName("João Silva");
        cliente.getPessoa().setNumeroDocumento(cpfCnpj);
        cliente.getPessoa().setPhone("(11) 99999-9999");
        cliente.getPessoa().setEmail("joao.silva@email.com");
    }

    @Test
    @DisplayName("Deve salvar um cliente com sucesso")
    void deveSalvarClienteComSucesso() {
        // Given
        when(jpaRepository.save(any(Cliente.class))).thenReturn(cliente);

        // When
        Cliente clienteSalvo = clienteRepository.save(cliente);

        // Then
        assertThat(clienteSalvo).isNotNull();
        assertThat(clienteSalvo.getId()).isEqualTo(clienteId);
        assertThat(clienteSalvo.getPessoa().getName()).isEqualTo("João Silva");
        assertThat(clienteSalvo.getPessoa().getNumeroDocumento()).isEqualTo(cpfCnpj);
        verify(jpaRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void deveBuscarClientePorIdComSucesso() {
        // Given
        when(jpaRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // When
        Optional<Cliente> clienteEncontrado = clienteRepository.findById(clienteId);

        // Then
        assertThat(clienteEncontrado).isPresent();
        assertThat(clienteEncontrado.get().getId()).isEqualTo(clienteId);
        assertThat(clienteEncontrado.get().getPessoa().getName()).isEqualTo("João Silva");
        verify(jpaRepository, times(1)).findById(clienteId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando cliente não encontrado por ID")
    void deveRetornarOptionalVazioQuandoClienteNaoEncontradoPorId() {
        // Given
        UUID idInexistente = UUID.randomUUID();
        when(jpaRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // When
        Optional<Cliente> clienteEncontrado = clienteRepository.findById(idInexistente);

        // Then
        assertThat(clienteEncontrado).isEmpty();
        verify(jpaRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve buscar cliente por CPF/CNPJ com sucesso")
    void deveBuscarClientePorCpfCnpjComSucesso() {
        // Given
        when(jpaRepository.findByPessoaNumeroDocumento(cpfCnpj)).thenReturn(Optional.of(cliente));

        // When
        Optional<Cliente> clienteEncontrado = clienteRepository.findByCpfCnpj(cpfCnpj);

        // Then
        assertThat(clienteEncontrado).isPresent();
        assertThat(clienteEncontrado.get().getPessoa().getNumeroDocumento()).isEqualTo(cpfCnpj);
        assertThat(clienteEncontrado.get().getPessoa().getName()).isEqualTo("João Silva");
        verify(jpaRepository, times(1)).findByPessoaNumeroDocumento(cpfCnpj);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando cliente não encontrado por CPF/CNPJ")
    void deveRetornarOptionalVazioQuandoClienteNaoEncontradoPorCpfCnpj() {
        // Given
        String cpfInexistente = "99999999999";
        when(jpaRepository.findByPessoaNumeroDocumento(cpfInexistente)).thenReturn(Optional.empty());

        // When
        Optional<Cliente> clienteEncontrado = clienteRepository.findByCpfCnpj(cpfInexistente);

        // Then
        assertThat(clienteEncontrado).isEmpty();
        verify(jpaRepository, times(1)).findByPessoaNumeroDocumento(cpfInexistente);
    }

    @Test
    @DisplayName("Deve buscar cliente por CPF/CNPJ usando findByCpfCnpjCliente")
    void deveBuscarClientePorCpfCnpjClienteComSucesso() {
        // Given
        when(jpaRepository.findByPessoaNumeroDocumento(cpfCnpj)).thenReturn(Optional.of(cliente));

        // When
        Optional<Cliente> clienteEncontrado = clienteRepository.findByCpfCnpjCliente(cpfCnpj);

        // Then
        assertThat(clienteEncontrado).isPresent();
        assertThat(clienteEncontrado.get().getPessoa().getNumeroDocumento()).isEqualTo(cpfCnpj);
        verify(jpaRepository, times(1)).findByPessoaNumeroDocumento(cpfCnpj);
    }

    @Test
    @DisplayName("Deve deletar cliente por ID com sucesso")
    void deveDeletarClientePorIdComSucesso() {
        // Given
        doNothing().when(jpaRepository).deleteById(clienteId);

        // When
        clienteRepository.deleteById(clienteId);

        // Then
        verify(jpaRepository, times(1)).deleteById(clienteId);
    }

    @Test
    @DisplayName("Deve buscar todos os clientes com sucesso")
    void deveBuscarTodosOsClientesComSucesso() {
        // Given
        Cliente outroCliente = criarClienteComPessoa("Default Cliente", "12345678900");
        outroCliente.setId(UUID.randomUUID());
        outroCliente.getPessoa().setName("Maria Santos");
        outroCliente.getPessoa().setNumeroDocumento("98765432100");
        outroCliente.getPessoa().setPhone("(11) 88888-8888");
        outroCliente.getPessoa().setEmail("maria.santos@email.com");

        List<Cliente> clientes = Arrays.asList(cliente, outroCliente);
        when(jpaRepository.findAll()).thenReturn(clientes);

        // When
        List<Cliente> clientesEncontrados = clienteRepository.findAll();

        // Then
        assertThat(clientesEncontrados).hasSize(2);
        assertThat(clientesEncontrados).containsExactlyInAnyOrder(cliente, outroCliente);
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há clientes")
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        // Given
        when(jpaRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<Cliente> clientesEncontrados = clienteRepository.findAll();

        // Then
        assertThat(clientesEncontrados).isEmpty();
        verify(jpaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve verificar se cliente existe por ID - retorna true")
    void deveVerificarSeClienteExistePorIdRetornaTrue() {
        // Given
        when(jpaRepository.existsById(clienteId)).thenReturn(true);

        // When
        boolean existe = clienteRepository.existsById(clienteId);

        // Then
        assertThat(existe).isTrue();
        verify(jpaRepository, times(1)).existsById(clienteId);
    }

    @Test
    @DisplayName("Deve verificar se cliente existe por ID - retorna false")
    void deveVerificarSeClienteExistePorIdRetornaFalse() {
        // Given
        UUID idInexistente = UUID.randomUUID();
        when(jpaRepository.existsById(idInexistente)).thenReturn(false);

        // When
        boolean existe = clienteRepository.existsById(idInexistente);

        // Then
        assertThat(existe).isFalse();
        verify(jpaRepository, times(1)).existsById(idInexistente);
    }

    @Test
    @DisplayName("Deve verificar se cliente existe por CPF/CNPJ - retorna true")
    void deveVerificarSeClienteExistePorCpfCnpjRetornaTrue() {
        // Given
        when(jpaRepository.existsByPessoaNumeroDocumento(cpfCnpj)).thenReturn(true);

        // When
        boolean existe = clienteRepository.existsByCpfCnpj(cpfCnpj);

        // Then
        assertThat(existe).isTrue();
        verify(jpaRepository, times(1)).existsByPessoaNumeroDocumento(cpfCnpj);
    }

    @Test
    @DisplayName("Deve verificar se cliente existe por CPF/CNPJ - retorna false")
    void deveVerificarSeClienteExistePorCpfCnpjRetornaFalse() {
        // Given
        String cpfInexistente = "99999999999";
        when(jpaRepository.existsByPessoaNumeroDocumento(cpfInexistente)).thenReturn(false);

        // When
        boolean existe = clienteRepository.existsByCpfCnpj(cpfInexistente);

        // Then
        assertThat(existe).isFalse();
        verify(jpaRepository, times(1)).existsByPessoaNumeroDocumento(cpfInexistente);
    }
}