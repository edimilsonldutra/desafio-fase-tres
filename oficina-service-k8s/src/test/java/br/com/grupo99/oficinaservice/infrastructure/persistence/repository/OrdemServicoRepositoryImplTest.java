package br.com.grupo99.oficinaservice.infrastructure.persistence.repository;

import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import br.com.grupo99.oficinaservice.infrastructure.persistence.jpa.OrdemServicoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@DisplayName("OrdemServicoRepositoryImpl Tests")
class OrdemServicoRepositoryImplTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa(documento, TipoPessoa.FISICA, nome, 
            nome.toLowerCase().replace(" ", "") + "@email.com", Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private OrdemServicoJpaRepository jpaRepository;

    @Mock(lenient = true)
    private ClienteRepository clienteRepository;

    @InjectMocks
    private OrdemServicoRepositoryImpl ordemServicoRepository;

    private OrdemServico ordemServico;
    private UUID ordemServicoId;
    private UUID clienteId;
    private UUID veiculoId;

    private void setValorTotalOrdemServico(OrdemServico ordemServico, BigDecimal valor) {
        try {
            java.lang.reflect.Field field = OrdemServico.class.getDeclaredField("valorTotal");
            field.setAccessible(true);
            field.set(ordemServico, valor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        ordemServicoId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        veiculoId = UUID.randomUUID();
        
        ordemServico = new OrdemServico(clienteId, veiculoId);
        ordemServico.setId(ordemServicoId);
        ordemServico.setStatus(StatusOS.RECEBIDA);
        setValorTotalOrdemServico(ordemServico, BigDecimal.valueOf(500.00));
        ordemServico.setDataCriacao(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve salvar ordem de serviço com sucesso")
    void shouldSaveOrdemServicoSuccessfully() {
        // Given
        when(jpaRepository.save(any(OrdemServico.class))).thenReturn(ordemServico);

        // When
        OrdemServico result = ordemServicoRepository.save(ordemServico);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ordemServicoId);
        assertThat(result.getClienteId()).isEqualTo(clienteId);
        assertThat(result.getVeiculoId()).isEqualTo(veiculoId);
        assertThat(result.getStatus()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(result.getValorTotal()).isEqualTo(BigDecimal.valueOf(500.00));
        
        verify(jpaRepository).save(ordemServico);
    }

    @Test
    @DisplayName("Deve buscar ordem de serviço por ID com sucesso")
    void shouldFindOrdemServicoByIdSuccessfully() {
        // Given
        when(jpaRepository.findById(ordemServicoId)).thenReturn(Optional.of(ordemServico));

        // When
        Optional<OrdemServico> result = ordemServicoRepository.findById(ordemServicoId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(ordemServicoId);
        assertThat(result.get().getClienteId()).isEqualTo(clienteId);
        
        verify(jpaRepository).findById(ordemServicoId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando ordem de serviço não encontrada por ID")
    void shouldReturnEmptyOptionalWhenOrdemServicoNotFoundById() {
        // Given
        when(jpaRepository.findById(ordemServicoId)).thenReturn(Optional.empty());

        // When
        Optional<OrdemServico> result = ordemServicoRepository.findById(ordemServicoId);

        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findById(ordemServicoId);
    }

    @Test
    @DisplayName("Deve buscar ordens de serviço por cliente ID com sucesso")
    void shouldFindOrdemServicoByClienteIdSuccessfully() {
        // Given
        OrdemServico ordemServico2 = new OrdemServico(clienteId, UUID.randomUUID());
        List<OrdemServico> ordens = Arrays.asList(ordemServico, ordemServico2);
        when(jpaRepository.findByClienteId(clienteId)).thenReturn(ordens);

        // When
        List<OrdemServico> result = ordemServicoRepository.findByClienteId(clienteId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(ordemServico, ordemServico2);
        assertThat(result.get(0).getClienteId()).isEqualTo(clienteId);
        assertThat(result.get(1).getClienteId()).isEqualTo(clienteId);
        
        verify(jpaRepository).findByClienteId(clienteId);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há ordens para o cliente")
    void shouldReturnEmptyListWhenNoOrdensForCliente() {
        // Given
        when(jpaRepository.findByClienteId(clienteId)).thenReturn(List.of());

        // When
        List<OrdemServico> result = ordemServicoRepository.findByClienteId(clienteId);

        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findByClienteId(clienteId);
    }

    @Test
    @DisplayName("Deve listar todas as ordens de serviço ordenadas por status e data de criação")
    void shouldFindAllOrdensServicoOrdered() {
        // Given
        OrdemServico os1 = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        os1.setStatus(StatusOS.RECEBIDA);
        os1.setDataCriacao(LocalDateTime.of(2023, 1, 1, 10, 0));

        OrdemServico os2 = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        os2.setStatus(StatusOS.EM_DIAGNOSTICO);
        os2.setDataCriacao(LocalDateTime.of(2023, 1, 2, 10, 0));

        OrdemServico os3 = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        os3.setStatus(StatusOS.AGUARDANDO_APROVACAO);
        os3.setDataCriacao(LocalDateTime.of(2023, 1, 3, 10, 0));

        OrdemServico os4 = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        os4.setStatus(StatusOS.EM_EXECUCAO);
        os4.setDataCriacao(LocalDateTime.of(2023, 1, 4, 10, 0));

        OrdemServico os5 = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        os5.setStatus(StatusOS.EM_EXECUCAO);
        os5.setDataCriacao(LocalDateTime.of(2023, 1, 1, 9, 0)); // Mais antiga entre EM_EXECUCAO

        List<OrdemServico> ordens = Arrays.asList(os1, os2, os3, os4, os5);
        when(jpaRepository.findAll()).thenReturn(ordens);

        // When
        List<OrdemServico> result = ordemServicoRepository.findAll();

        // Then
        assertThat(result).hasSize(5);
        // Esperado: EM_EXECUCAO (mais antiga), EM_EXECUCAO, AGUARDANDO_APROVACAO, EM_DIAGNOSTICO, RECEBIDA
        assertThat(result.get(0)).isEqualTo(os5); // EM_EXECUCAO mais antiga
        assertThat(result.get(1)).isEqualTo(os4); // EM_EXECUCAO mais nova
        assertThat(result.get(2)).isEqualTo(os3); // AGUARDANDO_APROVACAO
        assertThat(result.get(3)).isEqualTo(os2); // EM_DIAGNOSTICO
        assertThat(result.get(4)).isEqualTo(os1); // RECEBIDA
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há ordens de serviço")
    void shouldReturnEmptyListWhenNoOrdensServico() {
        // Given
        when(jpaRepository.findAll()).thenReturn(List.of());

        // When
        List<OrdemServico> result = ordemServicoRepository.findAll();

        // Then
        assertThat(result).isEmpty();
        
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar ordens de serviço por CPF/CNPJ do cliente")
    void shouldFindByClienteCpfCnpj() {
        // Given
        String cpfCnpj = "123.456.789-00";
        
        Pessoa pessoa = new Pessoa();
        pessoa.setName("Teste");
        pessoa.setNumeroDocumento(cpfCnpj);
        pessoa.setPerfil(Perfil.CLIENTE);
        Cliente cliente = new Cliente(pessoa);
        cliente.setId(UUID.randomUUID());
        List<OrdemServico> ordensEsperadas = List.of(new OrdemServico(cliente.getId(), UUID.randomUUID()));

        // Simula busca do cliente
        lenient().when(clienteRepository.findByCpfCnpj(cpfCnpj)).thenReturn(Optional.of(cliente));
        // Simula busca das ordens
        when(jpaRepository.findByClienteId(cliente.getId())).thenReturn(ordensEsperadas);

        // When
        List<OrdemServico> ordens = ordemServicoRepository.findByClienteId(cliente.getId());

        // Then
        assertThat(ordens).isEqualTo(ordensEsperadas);
        verify(jpaRepository).findByClienteId(cliente.getId());
    }

    @Test
    @DisplayName("Deve excluir logicamente ordens de serviço FINALIZADA e ENTREGUE da listagem")
    void shouldExcludeFinalizadaAndEntregueFromFindAll() {
        OrdemServico osFinalizada = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        osFinalizada.setStatus(StatusOS.FINALIZADA);
        osFinalizada.setDataCriacao(LocalDateTime.of(2023, 1, 5, 10, 0));

        OrdemServico osEntregue = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        osEntregue.setStatus(StatusOS.ENTREGUE);
        osEntregue.setDataCriacao(LocalDateTime.of(2023, 1, 6, 10, 0));

        OrdemServico osRecebida = new OrdemServico(UUID.randomUUID(), UUID.randomUUID());
        osRecebida.setStatus(StatusOS.RECEBIDA);
        osRecebida.setDataCriacao(LocalDateTime.of(2023, 1, 7, 10, 0));

        List<OrdemServico> ordens = Arrays.asList(osFinalizada, osEntregue, osRecebida);
        when(jpaRepository.findAll()).thenReturn(ordens);

        List<OrdemServico> result = ordemServicoRepository.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(osRecebida);
        verify(jpaRepository).findAll();
    }
}
