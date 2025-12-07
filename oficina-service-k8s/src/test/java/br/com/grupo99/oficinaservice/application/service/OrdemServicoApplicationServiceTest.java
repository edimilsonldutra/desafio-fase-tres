package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoDetalhesDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.BusinessException;
import br.com.grupo99.oficinaservice.application.exception.OrdemServicoAtivaException;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.domain.model.*;
import br.com.grupo99.oficinaservice.domain.repository.*;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Teste Unitário - OrdemServicoApplicationService")
class OrdemServicoApplicationServiceTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock private OrdemServicoRepository ordemServicoRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private VeiculoRepository veiculoRepository;
    @Mock private PecaRepository pecaRepository;
    @Mock private ServicoRepository servicoRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private OrdemServicoApplicationService service;

    private Cliente cliente;
    private Veiculo veiculo;
    private Peca peca;
    private Servico servico;
    private OrdemServico ordemServico;

    @BeforeEach
    void setUp() {
        cliente = criarClienteComPessoa("João Silva", "12345678901");
        cliente.setId(UUID.randomUUID());

        veiculo = new Veiculo("ABC-1234", "Toyota", "Corolla", 2020);
        veiculo.setId(UUID.randomUUID());
        veiculo.setCliente(cliente);

        peca = new Peca();
        peca.setId(UUID.randomUUID());
        peca.setNome("Filtro de Óleo");
        peca.setPreco(new BigDecimal("50.00"));
        peca.setEstoque(10);

        servico = new Servico("Serviço Teste", new BigDecimal("100.00"));
        servico.setId(UUID.randomUUID());

        ordemServico = new OrdemServico(cliente.getId(), veiculo.getId());
        ordemServico.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Deve criar ordem de serviço com sucesso")
    void deveCriarOrdemServicoComSucesso() {
        // Given
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                cliente.getPessoa().getNumeroDocumento(),
                veiculo.getPlaca(),
                List.of(servico.getId()),
                List.of(peca.getId())
        );

        when(clienteRepository.findByCpfCnpj(cliente.getPessoa().getNumeroDocumento())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlaca(veiculo.getPlaca())).thenReturn(Optional.of(veiculo));
        when(servicoRepository.findById(servico.getId())).thenReturn(Optional.of(servico));
        when(pecaRepository.findById(peca.getId())).thenReturn(Optional.of(peca));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemServico);

        // When
        OrdemServicoResponseDTO response = service.execute(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(ordemServico.getId());
        assertThat(response.clienteNome()).isEqualTo(cliente.getPessoa().getName());
        assertThat(response.placaVeiculo()).isEqualTo(veiculo.getPlaca());
        assertThat(response.status()).isEqualTo(StatusOS.RECEBIDA);

        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não for encontrado")
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {
        // Given
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                "99999999999",
                veiculo.getPlaca(),
                List.of(servico.getId()),
                List.of(peca.getId())
        );

        when(clienteRepository.findByCpfCnpj(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.execute(request));
    }

    @Test
    @DisplayName("Deve lançar exceção quando veículo não for encontrado")
    void deveLancarExcecaoQuandoVeiculoNaoForEncontrado() {
        // Given
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                cliente.getPessoa().getNumeroDocumento(),
                "XYZ-9999",
                List.of(servico.getId()),
                List.of(peca.getId())
        );

        when(clienteRepository.findByCpfCnpj(cliente.getPessoa().getNumeroDocumento())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlaca(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.execute(request));
    }

    @Test
    @DisplayName("Deve lançar exceção quando veículo não pertencer ao cliente")
    void deveLancarExcecaoQuandoVeiculoNaoPertencerAoCliente() {
        // Given
        Cliente outroCliente = criarClienteComPessoa("Maria", "98765432100");
        outroCliente.setId(UUID.randomUUID());
        veiculo.setCliente(outroCliente);

        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                cliente.getPessoa().getNumeroDocumento(),
                veiculo.getPlaca(),
                List.of(servico.getId()),
                List.of(peca.getId())
        );

        when(clienteRepository.findByCpfCnpj(cliente.getPessoa().getNumeroDocumento())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlaca(veiculo.getPlaca())).thenReturn(Optional.of(veiculo));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> service.execute(request));
        assertThat(exception.getMessage()).isEqualTo("A placa informada não pertence ao cliente.");
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço não for encontrado")
    void deveLancarExcecaoQuandoServicoNaoForEncontrado() {
        // Given
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                cliente.getPessoa().getNumeroDocumento(),
                veiculo.getPlaca(),
                List.of(UUID.randomUUID()),
                List.of(peca.getId())
        );

        when(clienteRepository.findByCpfCnpj(cliente.getPessoa().getNumeroDocumento())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlaca(veiculo.getPlaca())).thenReturn(Optional.of(veiculo));
        when(servicoRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.execute(request));
    }

    @Test
    @DisplayName("Deve lançar exceção quando peça não for encontrada")
    void deveLancarExcecaoQuandoPecaNaoForEncontrada() {
        // Given
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                cliente.getPessoa().getNumeroDocumento(),
                veiculo.getPlaca(),
                List.of(servico.getId()),
                List.of(UUID.randomUUID())
        );

        when(clienteRepository.findByCpfCnpj(cliente.getPessoa().getNumeroDocumento())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlaca(veiculo.getPlaca())).thenReturn(Optional.of(veiculo));
        when(servicoRepository.findById(servico.getId())).thenReturn(Optional.of(servico));
        when(pecaRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.execute(request));
    }

    @Test
    @DisplayName("Deve listar todas as ordens de serviço")
    void deveListarTodasAsOrdensDeServico() {
        // Given
        when(ordemServicoRepository.findAll()).thenReturn(List.of(ordemServico));
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(veiculo.getId())).thenReturn(Optional.of(veiculo));

        // When
        List<OrdemServicoResponseDTO> response = service.execute();

        // Then
        assertThat(response).hasSize(1);
        verify(ordemServicoRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar detalhes de ordem de serviço por ID")
    void deveBuscarDetalhesDeOrdemServicoPorId() {
        // Given
        UUID ordemId = ordemServico.getId();
        when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.of(ordemServico));
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(veiculo.getId())).thenReturn(Optional.of(veiculo));

        // When
        OrdemServicoDetalhesDTO response = service.execute(ordemId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(ordemId);
        verify(ordemServicoRepository).findById(ordemId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando ordem de serviço não for encontrada para busca de detalhes")
    void deveLancarExcecaoQuandoOrdemNaoForEncontradaParaDetalhes() {
        // Given
        UUID ordemId = UUID.randomUUID();
        when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.execute(ordemId));
    }

    @Test
    @DisplayName("Deve atualizar status da ordem de serviço com sucesso")
    void deveAtualizarStatusDaOrdemDeServicoComSucesso() {
        // Given
        UUID ordemId = ordemServico.getId();
        StatusOS novoStatus = StatusOS.EM_DIAGNOSTICO;

        when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemServico);
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(veiculo.getId())).thenReturn(Optional.of(veiculo));

        // When
        OrdemServicoResponseDTO response = service.execute(ordemId, novoStatus);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(ordemId);
        verify(ordemServicoRepository).save(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando tentar transição de status inválida")
    void deveLancarExcecaoQuandoTentarTransicaoDeStatusInvalida() {
        // Given
        UUID ordemId = ordemServico.getId();
        StatusOS statusInvalido = StatusOS.FINALIZADA; // Não pode ir direto de RECEBIDA para FINALIZADA

        when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.of(ordemServico));

        // When & Then
        assertThrows(BusinessException.class, () -> service.execute(ordemId, statusInvalido));
    }

    @Test
    @DisplayName("Deve lançar exceção quando ordem não for encontrada para atualização de status")
    void deveLancarExcecaoQuandoOrdemNaoForEncontradaParaAtualizacaoStatus() {
        // Given
        UUID ordemId = UUID.randomUUID();
        StatusOS novoStatus = StatusOS.EM_DIAGNOSTICO;
        when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> service.execute(ordemId, novoStatus));
    }

    @Test
    @DisplayName("Deve validar relacionamento cliente-veículo quando cliente do veículo for null")
    void deveValidarRelacionamentoClienteVeiculoQuandoClienteDoVeiculoForNull() {
        // Given
        veiculo.setCliente(null);
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                cliente.getPessoa().getNumeroDocumento(),
                veiculo.getPlaca(),
                List.of(servico.getId()),
                List.of(peca.getId())
        );

        when(clienteRepository.findByCpfCnpj(cliente.getPessoa().getNumeroDocumento())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlaca(veiculo.getPlaca())).thenReturn(Optional.of(veiculo));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> service.execute(request));
        assertThat(exception.getMessage()).isEqualTo("A placa informada não pertence ao cliente.");
    }

    @Test
    @DisplayName("Não deve criar ordem de serviço se já existe uma ativa para o mesmo cliente e veículo")
    void naoDeveCriarOrdemServicoSeJaExisteAtiva() {
        // Given
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
                cliente.getPessoa().getNumeroDocumento(),
                veiculo.getPlaca(),
                List.of(servico.getId()),
                List.of(peca.getId())
        );
        OrdemServico osAtiva = new OrdemServico(cliente.getId(), veiculo.getId());
        osAtiva.setId(UUID.randomUUID());
        osAtiva.setStatus(StatusOS.EM_DIAGNOSTICO);

        when(clienteRepository.findByCpfCnpj(cliente.getPessoa().getNumeroDocumento())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlaca(veiculo.getPlaca())).thenReturn(Optional.of(veiculo));
        when(ordemServicoRepository.findByClienteIdAndVeiculoIdAndStatusIn(
                eq(cliente.getId()), eq(veiculo.getId()), anyList())
        ).thenReturn(Optional.of(osAtiva));

        // When & Then
        OrdemServicoAtivaException ex = assertThrows(
                OrdemServicoAtivaException.class,
                () -> service.execute(request)
        );
        assertThat(ex.getMessage()).contains("Já existe uma ordem de serviço em andamento para este veículo.");
        assertThat(ex.getStatusAtivo()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        verify(ordemServicoRepository, never()).save(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Dado uma ordem de serviço válida, quando criada, então deve persistir e retornar sucesso")
    void dadoOrdemServicoValida_quandoCriar_entaoRetornaSucesso() {
        // Given
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(
            cliente.getPessoa().getNumeroDocumento(),
            veiculo.getPlaca(),
            List.of(servico.getId()),
            List.of(peca.getId())
        );
        when(clienteRepository.findByCpfCnpj(cliente.getPessoa().getNumeroDocumento())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByPlaca(veiculo.getPlaca())).thenReturn(Optional.of(veiculo));
        when(servicoRepository.findById(servico.getId())).thenReturn(Optional.of(servico));
        when(pecaRepository.findById(peca.getId())).thenReturn(Optional.of(peca));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemServico);
        // When
        OrdemServicoResponseDTO response = service.execute(request);
        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(ordemServico.getId());
        assertThat(response.clienteNome()).isEqualTo(cliente.getPessoa().getName());
        assertThat(response.placaVeiculo()).isEqualTo(veiculo.getPlaca());
        assertThat(response.status()).isEqualTo(StatusOS.RECEBIDA);
        verify(ordemServicoRepository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    @DisplayName("Dado uma ordem de serviço inexistente, quando buscar por ID, então deve lançar ResourceNotFoundException")
    void dadoOrdemServicoInexistente_quandoBuscarPorId_entaoLancaResourceNotFoundException() {
        // Given
        UUID idInexistente = UUID.randomUUID();
        when(ordemServicoRepository.findById(idInexistente)).thenReturn(Optional.empty());
        // When/Then
        assertThrows(ResourceNotFoundException.class, () -> service.execute(idInexistente));
    }

    @Test
    @DisplayName("Dado uma ordem de serviço, quando adicionar peça/serviço, então o valor total deve ser atualizado")
    void dadoOrdemServico_quandoAdicionarItens_entaoValorTotalAtualizado() {
        // Given
        ordemServico.adicionarPeca(peca, 2); // Supondo preço 50, total 100
        ordemServico.adicionarServico(servico, 1); // Supondo preço 100, total 100
        when(ordemServicoRepository.findById(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(veiculo.getId())).thenReturn(Optional.of(veiculo));
        // When
        OrdemServicoDetalhesDTO detalhes = service.execute(ordemServico.getId());
        // Then
        assertThat(detalhes.valorTotal()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Dado uma ordem de serviço, quando finalizar, então o status deve ser alterado e notificação enviada")
    void dadoOrdemServico_quandoFinalizar_entaoStatusAlteradoENotificacaoEnviada() {
        // Given
        ordemServico.iniciarDiagnostico();
        ordemServico.aguardarAprovacao();
        ordemServico.aprovar();
        when(ordemServicoRepository.findById(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenReturn(ordemServico);
        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findById(veiculo.getId())).thenReturn(Optional.of(veiculo));
        // When
        service.execute(ordemServico.getId(), StatusOS.FINALIZADA);
        // Then
        assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.FINALIZADA);
        verify(notificationService, times(1)).notificarAtualizacaoStatusOrdemServico(ordemServico, cliente);
    }

    @Test
    @DisplayName("Dado uma ordem de serviço já finalizada, quando tentar finalizar novamente, então deve lançar exceção")
    void dadoOrdemServicoFinalizada_quandoFinalizarNovamente_entaoLancaExcecao() {
        // Given
        ordemServico.iniciarDiagnostico();
        ordemServico.aguardarAprovacao();
        ordemServico.aprovar();
        ordemServico.finalizar();
        when(ordemServicoRepository.findById(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
        // When/Then
        assertThrows(BusinessException.class, () -> service.execute(ordemServico.getId(), StatusOS.FINALIZADA));
    }
}
