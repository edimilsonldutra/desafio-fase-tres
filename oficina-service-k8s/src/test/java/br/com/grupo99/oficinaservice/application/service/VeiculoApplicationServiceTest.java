package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.VeiculoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.VeiculoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import br.com.grupo99.oficinaservice.domain.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@DisplayName("Teste Unitário - VeiculoApplicationService")
class VeiculoApplicationServiceTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock private VeiculoRepository veiculoRepository;
    @Mock private ClienteRepository clienteRepository;

    @InjectMocks private VeiculoApplicationService service;

    private Cliente cliente;
    private Veiculo veiculo;
    private VeiculoRequestDTO veiculoRequestDTO;

    @BeforeEach
    void setUp() {
        cliente = criarClienteComPessoa("João Silva", "12345678901");
        cliente.setId(UUID.randomUUID());

        veiculo = new Veiculo("ABC-1234", "Toyota", "Corolla", 2020);
        veiculo.setId(UUID.randomUUID());
        veiculo.setCliente(cliente);

        veiculoRequestDTO = new VeiculoRequestDTO(
                "ABC-1234",
                "12345678901",
                "Toyota",
                "Corolla",
                2020,
                cliente.getId()
        );
    }

    @Test
    @DisplayName("Deve criar veículo com sucesso")
    void deveCriarVeiculoComSucesso() {
        // Given
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

        // When
        VeiculoResponseDTO response = service.create(veiculoRequestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(veiculo.getId());
        assertThat(response.placa()).isEqualTo(veiculo.getPlaca());
        assertThat(response.marca()).isEqualTo(veiculo.getMarca());
        assertThat(response.modelo()).isEqualTo(veiculo.getModelo());
        assertThat(response.ano()).isEqualTo(veiculo.getAno());

        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não for encontrado")
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {
        // Given
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.create(veiculoRequestDTO));
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve buscar veículo por ID com sucesso")
    void deveBuscarVeiculoPorIdComSucesso() {
        // Given
        UUID veiculoId = veiculo.getId();
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));

        // When
        VeiculoResponseDTO response = service.getById(veiculoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(veiculoId);
        assertThat(response.placa()).isEqualTo(veiculo.getPlaca());
    }

    @Test
    @DisplayName("Deve lançar exceção quando veículo não for encontrado por ID")
    void deveLancarExcecaoQuandoVeiculoNaoForEncontradoPorId() {
        // Given
        UUID veiculoId = UUID.randomUUID();
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.getById(veiculoId));
    }

    @Test
    @DisplayName("Deve listar todos os veículos")
    void deveListarTodosOsVeiculos() {
        // Given
        List<Veiculo> veiculos = List.of(veiculo);
        when(veiculoRepository.findAll()).thenReturn(veiculos);

        // When
        List<VeiculoResponseDTO> response = service.getAll();

        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(veiculo.getId());
        verify(veiculoRepository).findAll();
    }

    @Test
    @DisplayName("Deve atualizar veículo com sucesso")
    void deveAtualizarVeiculoComSucesso() {
        // Given
        UUID veiculoId = veiculo.getId();
        VeiculoRequestDTO updateRequest = new VeiculoRequestDTO(
                "XYZ-9999",
                "12345678901",
                "Honda",
                "Civic",
                2022,
                cliente.getId()
        );

        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.save(any(Veiculo.class))).thenReturn(veiculo);

        // When
        VeiculoResponseDTO response = service.update(veiculoId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar veículo inexistente")
    void deveLancarExcecaoAoTentarAtualizarVeiculoInexistente() {
        // Given
        UUID veiculoId = UUID.randomUUID();
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.update(veiculoId, veiculoRequestDTO));
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar veículo com cliente inexistente")
    void deveLancarExcecaoAoTentarAtualizarVeiculoComClienteInexistente() {
        // Given
        UUID veiculoId = veiculo.getId();
        when(veiculoRepository.findById(veiculoId)).thenReturn(Optional.of(veiculo));
        when(clienteRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.update(veiculoId, veiculoRequestDTO));
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve deletar veículo com sucesso")
    void deveDeletarVeiculoComSucesso() {
        // Given
        UUID veiculoId = veiculo.getId();
        when(veiculoRepository.existsById(veiculoId)).thenReturn(true);

        // When
        service.delete(veiculoId);

        // Then
        verify(veiculoRepository).deleteById(veiculoId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar veículo inexistente")
    void deveLancarExcecaoAoTentarDeletarVeiculoInexistente() {
        // Given
        UUID veiculoId = UUID.randomUUID();
        when(veiculoRepository.existsById(veiculoId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.delete(veiculoId));
        verify(veiculoRepository, never()).deleteById(any(UUID.class));
    }
}