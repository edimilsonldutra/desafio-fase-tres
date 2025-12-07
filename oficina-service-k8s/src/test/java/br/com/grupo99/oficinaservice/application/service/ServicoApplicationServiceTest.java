package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.ServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.ServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.Servico;
import br.com.grupo99.oficinaservice.domain.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste Unitário - ServicoApplicationService")
class ServicoApplicationServiceTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock private ServicoRepository servicoRepository;

    @InjectMocks private ServicoApplicationService service;

    private Servico servico;
    private ServicoRequestDTO servicoRequestDTO;

    @BeforeEach
    void setUp() {
        servico = new Servico("Serviço Teste", new BigDecimal("100.00"));
        servico.setId(UUID.randomUUID());
        servico.setDescricao("Troca de Óleo");
        servico.setPreco(new BigDecimal("100.00"));

        servicoRequestDTO = new ServicoRequestDTO(
                "Troca de Óleo",
                new BigDecimal("100.00")
        );
    }

    @Test
    @DisplayName("Deve criar serviço com sucesso")
    void deveCriarServicoComSucesso() {
        // Given
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        // When
        ServicoResponseDTO response = service.create(servicoRequestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(servico.getId());
        assertThat(response.descricao()).isEqualTo(servico.getDescricao());
        assertThat(response.preco()).isEqualByComparingTo(servico.getPreco());

        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve buscar serviço por ID com sucesso")
    void deveBuscarServicoPorIdComSucesso() {
        // Given
        UUID servicoId = servico.getId();
        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));

        // When
        ServicoResponseDTO response = service.getById(servicoId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(servicoId);
        assertThat(response.descricao()).isEqualTo(servico.getDescricao());
        assertThat(response.preco()).isEqualByComparingTo(servico.getPreco());
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço não for encontrado por ID")
    void deveLancarExcecaoQuandoServicoNaoForEncontradoPorId() {
        // Given
        UUID servicoId = UUID.randomUUID();
        when(servicoRepository.findById(servicoId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.getById(servicoId));
    }

    @Test
    @DisplayName("Deve listar todos os serviços")
    void deveListarTodosOsServicos() {
        // Given
        List<Servico> servicos = List.of(servico);
        when(servicoRepository.findAll()).thenReturn(servicos);

        // When
        List<ServicoResponseDTO> response = service.getAll();

        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(servico.getId());
        assertThat(response.get(0).descricao()).isEqualTo(servico.getDescricao());
        verify(servicoRepository).findAll();
    }

    @Test
    @DisplayName("Deve atualizar serviço com sucesso")
    void deveAtualizarServicoComSucesso() {
        // Given
        UUID servicoId = servico.getId();
        ServicoRequestDTO updateRequest = new ServicoRequestDTO(
                "Troca de Filtro",
                new BigDecimal("150.00")
        );

        when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servico);

        // When
        ServicoResponseDTO response = service.update(servicoId, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar serviço inexistente")
    void deveLancarExcecaoAoTentarAtualizarServicoInexistente() {
        // Given
        UUID servicoId = UUID.randomUUID();
        when(servicoRepository.findById(servicoId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.update(servicoId, servicoRequestDTO));
        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    @DisplayName("Deve deletar serviço com sucesso")
    void deveDeletarServicoComSucesso() {
        // Given
        UUID servicoId = servico.getId();
        when(servicoRepository.existsById(servicoId)).thenReturn(true);

        // When
        service.delete(servicoId);

        // Then
        verify(servicoRepository).deleteById(servicoId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar serviço inexistente")
    void deveLancarExcecaoAoTentarDeletarServicoInexistente() {
        // Given
        UUID servicoId = UUID.randomUUID();
        when(servicoRepository.existsById(servicoId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.delete(servicoId));
        verify(servicoRepository, never()).deleteById(any(UUID.class));
    }
}