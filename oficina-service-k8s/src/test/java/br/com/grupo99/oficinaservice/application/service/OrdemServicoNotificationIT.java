package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import br.com.grupo99.oficinaservice.domain.model.Veiculo;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import br.com.grupo99.oficinaservice.domain.repository.OrdemServicoRepository;
import br.com.grupo99.oficinaservice.domain.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Teste de Integração BDD - Notificação da Ordem de Serviço")
class OrdemServicoNotificationIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Autowired
    private OrdemServicoApplicationService osService;

    @Autowired
    private OrdemServicoRepository osRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    // Usamos @MockBean para substituir a implementação real do NotificationService
    // por um mock controlado pelo Mockito.
    @MockBean
    private NotificationService notificationService;

    private OrdemServico os;

    @BeforeEach
    void setUp() {
        // Prepara uma Ordem de Serviço com o status inicial necessário para o teste
        Cliente cliente = clienteRepository.save(criarClienteComPessoa("Cliente Notificação", "55566677788"));
        Veiculo veiculo = new Veiculo("NOT-0001", "Marca", "Modelo", 2021);
        veiculo.setCliente(cliente);
        veiculo = veiculoRepository.save(veiculo);

        os = new OrdemServico(cliente.getId(), veiculo.getId());
        os.setStatus(StatusOS.EM_DIAGNOSTICO); // O status precisa estar em diagnóstico para a próxima transição
        os = osRepository.save(os);
    }

    @Test
    @DisplayName("Dado uma OS em diagnóstico, Quando o status muda para AGUARDANDO_APROVACAO, Então o serviço de notificação deve ser chamado")
    void givenOrderInDiagnosis_whenStatusChangesToAwaitingApproval_thenNotificationServiceShouldBeCalled() {
        // Given (Dado)
        // A OS já foi preparada no método setUp()

        // When (Quando)
        osService.execute(os.getId(), StatusOS.AGUARDANDO_APROVACAO);

        // Then (Então)
        // Verificamos se o método notificarClienteParaAprovacao foi chamado exatamente uma vez,
        // passando a Ordem de Serviço correta como argumento.
        verify(notificationService, times(1)).notificarClienteParaAprovacao(os);
    }

    @Test
    @DisplayName("Dado uma OS em diagnóstico, Quando o status muda para outro estado (não AGUARDANDO_APROVACAO), Então o serviço de notificação NÃO deve ser chamado")
    void givenOrderInDiagnosis_whenStatusChangesToAnotherState_thenNotificationServiceShouldNotBeCalled() {
        // Given (Dado)
        // A OS já foi preparada no método setUp()

        // When (Quando)
        // Tentamos uma transição que não dispara a notificação
        os.setStatus(StatusOS.AGUARDANDO_APROVACAO); // Pré-condição para aprovar
        osRepository.save(os);
        osService.execute(os.getId(), StatusOS.EM_EXECUCAO);

        // Then (Então)
        // Verificamos que o método de notificação nunca foi chamado para esta transição.
        verify(notificationService, never()).notificarClienteParaAprovacao(any(OrdemServico.class));
    }
}

