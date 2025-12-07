package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoDetalhesDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.BusinessException;
import br.com.grupo99.oficinaservice.domain.model.*;
import br.com.grupo99.oficinaservice.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import br.com.grupo99.oficinaservice.domain.model.Cliente;
import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.domain.model.TipoPessoa;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Teste de Integração BDD - OrdemServicoApplicationService")
class OrdemServicoApplicationServiceIT {
    private Cliente criarClienteComPessoa(String nome, String documento) {
        Pessoa pessoa = new Pessoa();
        pessoa.setName(nome);
        pessoa.setNumeroDocumento(documento);
        pessoa.setTipoPessoa(TipoPessoa.FISICA);
        pessoa.setPerfil(Perfil.CLIENTE);
        return new Cliente(pessoa);
    }


    @Autowired private OrdemServicoApplicationService osService;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private PecaRepository pecaRepository;
    @Autowired private ServicoRepository servicoRepository;
    @Autowired private OrdemServicoRepository osRepository;

    private Cliente cliente;
    private Veiculo veiculo;
    private Peca peca;
    private Servico servico;

    @BeforeEach
    void setUp() {
        cliente = criarClienteComPessoa("Cliente OS", "33333333333");
        veiculo = new Veiculo("OS-1234", "Ford", "Ka", 2018);
        veiculo.setCliente(cliente);
        cliente.getVeiculos().add(veiculo);
        cliente = clienteRepository.save(cliente);
        veiculo = cliente.getVeiculos().get(0);

        peca = new Peca();
        peca.setEstoque(10);
        peca.setPreco(new BigDecimal("100.00"));
        peca.setNome("Peca Teste");
        peca = pecaRepository.save(peca);

        servico = new Servico("Serviço Teste", new BigDecimal("100.00"));
        servico = servicoRepository.save(servico);
    }

    @Test
    @DisplayName("Dado um DTO de OS válido, Quando criar, Então a OS deve ser salva com os IDs corretos")
    void givenValidOsRequest_whenCreate_thenShouldSaveOs() {
        // Given
        OrdemServicoRequestDTO request = new OrdemServicoRequestDTO(cliente.getPessoa().getNumeroDocumento(), veiculo.getPlaca(), List.of(servico.getId()), List.of(peca.getId()));

        // When
        OrdemServicoResponseDTO response = osService.execute(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(response.valorTotal()).isEqualByComparingTo(new BigDecimal("300.00"));

        OrdemServico osSalva = osRepository.findById(response.id()).get();
        assertThat(osSalva.getClienteId()).isEqualTo(cliente.getId());
        assertThat(osSalva.getVeiculoId()).isEqualTo(veiculo.getId());
    }

    @Test
    @DisplayName("Dado um veículo que não pertence ao cliente, Quando criar OS, Então deve lançar BusinessException")
    void givenVeiculoFromAnotherCliente_whenCreateOs_thenShouldThrowBusinessException() {
        // Given
        Cliente outroCliente = clienteRepository.save(criarClienteComPessoa("Outro Cliente", "44444444444"));

        OrdemServicoRequestDTO osRequest = new OrdemServicoRequestDTO(outroCliente.getPessoa().getNumeroDocumento(), veiculo.getPlaca(), Collections.emptyList(), Collections.emptyList());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> osService.execute(osRequest));
        assertThat(exception.getMessage()).isEqualTo("A placa informada não pertence ao cliente.");
    }

    @Test
    @DisplayName("Dado uma OS existente, Quando buscar por ID, Então deve retornar os detalhes completos")
    void givenExistingOs_whenGetById_thenShouldReturnDetails() {
        // Given
        OrdemServico os = new OrdemServico(cliente.getId(), veiculo.getId());
        os.adicionarPeca(peca, 1);
        os = osRepository.save(os);

        // When
        OrdemServicoDetalhesDTO detalhes = osService.execute(os.getId());

        // Then
        assertThat(detalhes).isNotNull();
        assertThat(detalhes.cliente().id()).isEqualTo(cliente.getId());
        assertThat(detalhes.veiculo().id()).isEqualTo(veiculo.getId());
        assertThat(detalhes.pecas()).hasSize(1);
    }

    @Test
    @DisplayName("Dado que existem Ordens de Serviço, Quando listar todas, Então deve retornar a lista de OS")
    void givenOsExists_whenGetAll_thenShouldReturnOsList() {
        // Given
        osRepository.save(new OrdemServico(cliente.getId(), veiculo.getId()));
        osRepository.save(new OrdemServico(cliente.getId(), veiculo.getId()));

        // When
        List<OrdemServicoResponseDTO> lista = osService.execute();

        // Then
        assertThat(lista).hasSize(2);
    }

    @Test
    @DisplayName("Dado uma OS existente, Quando atualizar o status para um estado válido, Então o status deve ser atualizado")
    void givenExistingOs_whenUpdateStatusToValidState_thenShouldUpdateStatus() {
        // Given
        OrdemServico os = osRepository.save(new OrdemServico(cliente.getId(), veiculo.getId())); // Status inicial: RECEBIDA

        // When
        osService.execute(os.getId(), StatusOS.EM_DIAGNOSTICO);
        OrdemServicoResponseDTO response = osService.execute(os.getId(), StatusOS.AGUARDANDO_APROVACAO);

        // Then
        assertThat(response.status()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
    }

    @Test
    @DisplayName("Dado uma OS existente, Quando atualizar o status para um estado inválido, Então deve lançar BusinessException")
    void givenExistingOs_whenUpdateStatusToInvalidState_thenShouldThrowBusinessException() {
        // Given
        OrdemServico os = osRepository.save(new OrdemServico(cliente.getId(), veiculo.getId())); // Status inicial: RECEBIDA

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            osService.execute(os.getId(), StatusOS.FINALIZADA); // Não pode ir de RECEBIDA para FINALIZADA
        });
        assertThat(exception.getMessage()).contains("OS não pode ser finalizada pois não está em execução");
    }
}