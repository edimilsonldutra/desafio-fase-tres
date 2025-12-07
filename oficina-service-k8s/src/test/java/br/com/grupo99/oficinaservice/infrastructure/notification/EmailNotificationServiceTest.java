package br.com.grupo99.oficinaservice.infrastructure.notification;

import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import br.com.grupo99.oficinaservice.domain.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes para EmailNotificationService")
class EmailNotificationServiceTest {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private OrdemServico ordemServico;
    private Cliente cliente;
    private UUID clienteId;
    private UUID ordemServicoId;

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
        clienteId = UUID.randomUUID();
        ordemServicoId = UUID.randomUUID();

        cliente = criarClienteComPessoa("Default Cliente", "12345678900");
        cliente.setId(clienteId);
        cliente.getPessoa().setName("João Silva");
        cliente.getPessoa().setEmail("joao.silva@email.com");
        cliente.getPessoa().setNumeroDocumento("12345678901");

        ordemServico = new OrdemServico();
        ordemServico.setId(ordemServicoId);
        ordemServico.setClienteId(clienteId);
        setValorTotalOrdemServico(ordemServico, BigDecimal.valueOf(500.00));
        ordemServico.setStatus(StatusOS.EM_DIAGNOSTICO);
        ordemServico.setDataCriacao(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve enviar email com sucesso quando cliente possui email válido")
    void deveEnviarEmailComSucessoQuandoClientePossuiEmailValido() {
        // Arrange
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals("nao-responda@oficina.com", capturedMessage.getFrom());
        assertEquals("joao.silva@email.com", capturedMessage.getTo()[0]);
        assertTrue(capturedMessage.getSubject().contains("Orçamento da sua Ordem de Serviço"));
        assertTrue(capturedMessage.getText().contains("Olá, João Silva!"));
        assertTrue(capturedMessage.getText().matches("(?s).*Valor Total: R\\$ 500([.,]00).*"));
        assertTrue(capturedMessage.getText().contains("O orçamento para a sua Ordem de Serviço está pronto para aprovação."));
        assertTrue(capturedMessage.getText().contains("Atenciosamente,"));
        assertTrue(capturedMessage.getText().contains("Equipe da Oficina"));
        verify(clienteRepository).findById(clienteId);
    }

    @Test
    @DisplayName("Deve formatar corretamente o assunto do email com ID da OS")
    void deveFormatarCorretamenteOAssuntoDoEmailComIdDaOS() {
        // Arrange
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        String expectedSubjectPrefix = "Orçamento da sua Ordem de Serviço #" + 
            ordemServicoId.toString().substring(0, 8);
        assertEquals(expectedSubjectPrefix, capturedMessage.getSubject());
    }

    @Test
    @DisplayName("Deve formatar corretamente o corpo da mensagem")
    void deveFormatarCorretamenteOCorpoDaMensagem() {
        // Arrange
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        String messageText = capturedMessage.getText();
        assertTrue(messageText.contains("Olá, João Silva!"));
        assertTrue(messageText.matches("(?s).*Valor Total: R\\$ 500([.,]00).*"));
        assertTrue(messageText.contains("O orçamento para a sua Ordem de Serviço está pronto para aprovação."));
        assertTrue(messageText.contains("Por favor, entre em contato para aprovar o serviço."));
        assertTrue(messageText.contains("Atenciosamente,"));
        assertTrue(messageText.contains("Equipe da Oficina"));
    }

    @Test
    @DisplayName("Não deve enviar email quando cliente não possui email")
    void naoDeveEnviarEmailQuandoClienteNaoPossuiEmail() {
        // Arrange
        cliente.getPessoa().setEmail(null);
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(clienteRepository).findById(clienteId);
    }

    @Test
    @DisplayName("Não deve enviar email quando cliente possui email vazio")
    void naoDeveEnviarEmailQuandoClientePossuiEmailVazio() {
        // Arrange
        cliente.getPessoa().setEmail("");
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(clienteRepository).findById(clienteId);
    }

    @Test
    @DisplayName("Não deve enviar email quando cliente possui email apenas com espaços")
    void naoDeveEnviarEmailQuandoClientePossuiEmailApenasComEspacos() {
        // Arrange
        cliente.getPessoa().setEmail("   ");
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(clienteRepository).findById(clienteId);
    }

    @Test
    @DisplayName("Não deve enviar email quando cliente não for encontrado")
    void naoDeveEnviarEmailQuandoClienteNaoForEncontrado() {
        // Arrange
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verify(clienteRepository).findById(clienteId);
    }

    @Test
    @DisplayName("Deve capturar exceção quando houver erro no envio do email")
    void deveCapturaExcecaoQuandoHouverErroNoEnvioDoEmail() {
        // Arrange
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        doThrow(new RuntimeException("Erro de conexão SMTP"))
            .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> emailNotificationService.notificarClienteParaAprovacao(ordemServico));

        verify(clienteRepository).findById(clienteId);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Deve formatar valor monetário corretamente no email")
    void deveFormatarValorMonetarioCorretamenteNoEmail() {
        // Arrange
        setValorTotalOrdemServico(ordemServico, BigDecimal.valueOf(1250.75));
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.getText().matches("(?s).*Valor Total: R\\$ 1250([.,]75).*"));
    }

    @Test
    @DisplayName("Deve processar corretamente valores monetários com zero centavos")
    void deveProcessarCorretamenteValoresMonetariosComZeroCentavos() {
        // Arrange
        setValorTotalOrdemServico(ordemServico, BigDecimal.valueOf(1000.00));
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.getText().matches("(?s).*Valor Total: R\\$ 1000([.,]00).*"));
    }

    @Test
    @DisplayName("Deve usar email remetente correto")
    void deveUsarEmailRemetenteCorreto() {
        // Arrange
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertEquals("nao-responda@oficina.com", capturedMessage.getFrom());
    }

    @Test
    @DisplayName("Deve funcionar com diferentes tamanhos de nomes de cliente")
    void deveFuncionarComDiferentesTamanhosDeNomesDeCliente() {
        // Arrange
        cliente.getPessoa().setName("A");
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        // Act
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        // Assert
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertTrue(capturedMessage.getText().contains("Olá, A!"));

        // Teste com nome muito longo
        cliente.getPessoa().setName("João Carlos da Silva Santos de Oliveira Junior");
        emailNotificationService.notificarClienteParaAprovacao(ordemServico);

        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }
}