package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.ClienteRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ClienteResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.BusinessException;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários para ClienteApplicationService")
class ClienteApplicationServiceTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteApplicationService clienteApplicationService;

    private ClienteRequestDTO clienteRequestDTO;
    private Cliente cliente;
    private UUID clienteId;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        
        clienteRequestDTO = new ClienteRequestDTO(
                "João Silva",
                "111.444.777-35",
                "11999887766",
                "joao@email.com"
        );

        cliente = criarClienteComPessoa("Default Cliente", "12345678900");
        cliente.setId(clienteId);
        cliente.getPessoa().setName("João Silva");
        cliente.getPessoa().setNumeroDocumento("111.444.777-35");
        cliente.getPessoa().setPhone("11999887766");
        cliente.getPessoa().setEmail("joao@email.com");
    }

    @Test
    @DisplayName("Deve criar um novo cliente com sucesso")
    void shouldCreateClienteSuccessfully() {
        // Given
        when(clienteRepository.existsByCpfCnpj("111.444.777-35")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        // When
        ClienteResponseDTO result = clienteApplicationService.create(clienteRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("João Silva");
        assertThat(result.cpfCnpj()).isEqualTo("111.444.777-35");
        assertThat(result.telefone()).isEqualTo("11999887766");
        assertThat(result.email()).isEqualTo("joao@email.com");

        verify(clienteRepository).existsByCpfCnpj("111.444.777-35");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com nome vazio")
    void shouldThrowExceptionWhenCreatingClienteWithBlankNome() {
        // Given
        ClienteRequestDTO requestWithBlankNome = new ClienteRequestDTO(
                "",
                "111.444.777-35",
                "11999887766",
                "joao@email.com"
        );

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.create(requestWithBlankNome)
        );

        assertThat(exception.getMessage()).isEqualTo("Nome é obrigatório.");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com nome null")
    void shouldThrowExceptionWhenCreatingClienteWithNullNome() {
        // Given
        ClienteRequestDTO requestWithNullNome = new ClienteRequestDTO(
                null,
                "111.444.777-35",
                "11999887766",
                "joao@email.com"
        );

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.create(requestWithNullNome)
        );

        assertThat(exception.getMessage()).isEqualTo("Nome é obrigatório.");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com CPF/CNPJ vazio")
    void shouldThrowExceptionWhenCreatingClienteWithBlankCpfCnpj() {
        // Given
        ClienteRequestDTO requestWithBlankCpfCnpj = new ClienteRequestDTO(
                "João Silva",
                "",
                "11999887766",
                "joao@email.com"
        );

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.create(requestWithBlankCpfCnpj)
        );

        assertThat(exception.getMessage()).isEqualTo("CPF/CNPJ é obrigatório.");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com email vazio")
    void shouldThrowExceptionWhenCreatingClienteWithBlankEmail() {
        // Given
        ClienteRequestDTO requestWithBlankEmail = new ClienteRequestDTO(
                "João Silva",
                "111.444.777-35",
                "11999887766",
                ""
        );

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.create(requestWithBlankEmail)
        );

        assertThat(exception.getMessage()).isEqualTo("E-mail é obrigatório.");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com telefone vazio")
    void shouldThrowExceptionWhenCreatingClienteWithBlankTelefone() {
        // Given
        ClienteRequestDTO requestWithBlankTelefone = new ClienteRequestDTO(
                "João Silva",
                "111.444.777-35",
                "",
                "joao@email.com"
        );

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.create(requestWithBlankTelefone)
        );

        assertThat(exception.getMessage()).isEqualTo("Telefone é obrigatório.");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com CPF/CNPJ duplicado")
    void shouldThrowExceptionWhenCreatingClienteWithDuplicatedCpfCnpj() {
        // Given
        when(clienteRepository.existsByCpfCnpj("111.444.777-35")).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.create(clienteRequestDTO)
        );

        assertThat(exception.getMessage()).isEqualTo("Já existe um cliente com este CPF/CNPJ.");
        verify(clienteRepository).existsByCpfCnpj("111.444.777-35");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com e-mail já existente")
    void shouldThrowExceptionWhenCreatingClienteWithDuplicatedEmail() {
        when(clienteRepository.existsByCpfCnpj(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.create(clienteRequestDTO)
        );
        assertThat(exception.getMessage()).isEqualTo("Já existe um cliente com este e-mail.");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cliente com telefone já existente")
    void shouldThrowExceptionWhenCreatingClienteWithDuplicatedTelefone() {
        when(clienteRepository.existsByCpfCnpj(anyString())).thenReturn(false);
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByTelefone("11999887766")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.create(clienteRequestDTO)
        );
        assertThat(exception.getMessage()).isEqualTo("Já existe um cliente com este telefone.");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve atualizar cliente existente com sucesso")
    void shouldUpdateClienteSuccessfully() {
        // Given
        ClienteRequestDTO updateRequest = new ClienteRequestDTO(
                "João Silva Santos",
                "111.444.777-35",
                "11888776655",
                "joao.santos@email.com"
        );

        Cliente clienteAtualizado = criarClienteComPessoa("Default Cliente", "12345678900");
        clienteAtualizado.setId(clienteId);
        clienteAtualizado.getPessoa().setName("João Silva Santos");
        clienteAtualizado.getPessoa().setNumeroDocumento("111.444.777-35");
        clienteAtualizado.getPessoa().setPhone("11888776655");
        clienteAtualizado.getPessoa().setEmail("joao.santos@email.com");

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteAtualizado);

        // When
        ClienteResponseDTO result = clienteApplicationService.update(clienteId, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.nome()).isEqualTo("João Silva Santos");
        assertThat(result.telefone()).isEqualTo("11888776655");
        assertThat(result.email()).isEqualTo("joao.santos@email.com");

        verify(clienteRepository).findById(clienteId);
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar cliente inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentCliente() {
        // Given
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clienteApplicationService.update(clienteId, clienteRequestDTO)
        );

        assertThat(exception.getMessage()).contains("Cliente não encontrado com o id: " + clienteId);
        verify(clienteRepository).findById(clienteId);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cliente para e-mail já existente em outro cliente")
    void shouldThrowExceptionWhenUpdatingClienteWithDuplicatedEmail() {
        ClienteRequestDTO updateDTO = new ClienteRequestDTO(
                "João Silva",
                "111.444.777-35",
                "11999887766",
                "novo@email.com"
        );
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmail("novo@email.com")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.update(clienteId, updateDTO)
        );
        assertThat(exception.getMessage()).isEqualTo("Já existe um cliente com este e-mail.");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar cliente para telefone já existente em outro cliente")
    void shouldThrowExceptionWhenUpdatingClienteWithDuplicatedTelefone() {
        ClienteRequestDTO updateDTO = new ClienteRequestDTO(
                "João Silva",
                "111.444.777-35",
                "11988776655",
                "joao@email.com"
        );
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByTelefone("11988776655")).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> clienteApplicationService.update(clienteId, updateDTO)
        );
        assertThat(exception.getMessage()).isEqualTo("Já existe um cliente com este telefone.");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve buscar cliente por ID com sucesso")
    void shouldGetClienteByIdSuccessfully() {
        // Given
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // When
        ClienteResponseDTO result = clienteApplicationService.getById(clienteId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(clienteId);
        assertThat(result.nome()).isEqualTo("João Silva");

        verify(clienteRepository).findById(clienteId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar cliente inexistente por ID")
    void shouldThrowExceptionWhenGettingNonExistentClienteById() {
        // Given
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clienteApplicationService.getById(clienteId)
        );

        assertThat(exception.getMessage()).contains("Cliente não encontrado com o id: " + clienteId);
        verify(clienteRepository).findById(clienteId);
    }

    @Test
    @DisplayName("Deve listar todos os clientes com sucesso")
    void shouldGetAllClientesSuccessfully() {
        // Given
        Cliente cliente2 = criarClienteComPessoa("Default Cliente", "12345678900");
        cliente2.setId(UUID.randomUUID());
        cliente2.getPessoa().setName("Maria Santos");
        cliente2.getPessoa().setNumeroDocumento("111.222.333-96");
        cliente2.getPessoa().setPhone("11777665544");
        cliente2.getPessoa().setEmail("maria@email.com");

        List<Cliente> clientes = Arrays.asList(cliente, cliente2);
        when(clienteRepository.findAll()).thenReturn(clientes);

        // When
        List<ClienteResponseDTO> result = clienteApplicationService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).nome()).isEqualTo("João Silva");
        assertThat(result.get(1).nome()).isEqualTo("Maria Santos");

        verify(clienteRepository).findAll();
    }

    @Test
    @DisplayName("Deve deletar cliente existente com sucesso")
    void shouldDeleteClienteSuccessfully() {
        // Given
        when(clienteRepository.existsById(clienteId)).thenReturn(true);

        // When
        clienteApplicationService.delete(clienteId);

        // Then
        verify(clienteRepository).existsById(clienteId);
        verify(clienteRepository).deleteById(clienteId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar cliente inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentCliente() {
        // Given
        when(clienteRepository.existsById(clienteId)).thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> clienteApplicationService.delete(clienteId)
        );

        assertThat(exception.getMessage()).contains("Cliente não encontrado com o id: " + clienteId);
        verify(clienteRepository).existsById(clienteId);
        verify(clienteRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Dado um cliente válido, quando cadastrar, então deve persistir e retornar sucesso")
    void dadoClienteValido_quandoCadastrar_entaoRetornaSucesso() {
        // Given
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
        // When
        ClienteResponseDTO response = clienteApplicationService.create(clienteRequestDTO);
        // Then
        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo(clienteRequestDTO.nome());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Dado um cliente inválido, quando cadastrar, então deve lançar BusinessException")
    void dadoClienteInvalido_quandoCadastrar_entaoLancaBusinessException() {
        // Given
        ClienteRequestDTO dtoInvalido = new ClienteRequestDTO("", "", "", "");
        // When/Then
        assertThrows(BusinessException.class, () -> clienteApplicationService.create(dtoInvalido));
    }

    @Test
    @DisplayName("Dado um cliente existente, quando buscar por ID, então deve retornar os dados corretos")
    void dadoClienteExistente_quandoBuscarPorId_entaoRetornaDados() {
        // Given
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Optional.of(cliente));
        // When
        ClienteResponseDTO response = clienteApplicationService.getById(clienteId);
        // Then
        assertThat(response).isNotNull();
        assertThat(response.nome()).isEqualTo(cliente.getPessoa().getName());
        verify(clienteRepository, times(1)).findById(clienteId);
    }

    @Test
    @DisplayName("Dado um cliente inexistente, quando buscar por ID, então deve lançar ResourceNotFoundException")
    void dadoClienteInexistente_quandoBuscarPorId_entaoLancaResourceNotFoundException() {
        // Given
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> clienteApplicationService.getById(clienteId));
    }

    @Test
    @DisplayName("Dado clientes cadastrados, quando listar todos, então deve retornar a lista correta")
    void dadoClientesCadastrados_quandoListarTodos_entaoRetornaLista() {
        // Given
        Cliente cliente2 = criarClienteComPessoa("Maria", "22233344455");
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente, cliente2));
        // When
        List<ClienteResponseDTO> lista = clienteApplicationService.getAll();
        // Then
        assertThat(lista).hasSize(2);
        assertThat(lista.get(0).nome()).isEqualTo(cliente.getPessoa().getName());
        assertThat(lista.get(1).nome()).isEqualTo("Maria");
        verify(clienteRepository, times(1)).findAll();
    }
}
