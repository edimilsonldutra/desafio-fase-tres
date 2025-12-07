package br.com.grupo99.oficinaservice.application.service;

import br.com.grupo99.oficinaservice.application.dto.OrdemServicoDetalhesDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoRequestDTO;
import br.com.grupo99.oficinaservice.application.dto.OrdemServicoResponseDTO;
import br.com.grupo99.oficinaservice.application.exception.BusinessException;
import br.com.grupo99.oficinaservice.application.exception.ResourceNotFoundException;
import br.com.grupo99.oficinaservice.application.exception.OrdemServicoAtivaException;
import br.com.grupo99.oficinaservice.application.usecase.*;

import br.com.grupo99.oficinaservice.domain.model.*;
import br.com.grupo99.oficinaservice.domain.repository.*;
import br.com.grupo99.oficinaservice.domain.service.OrcamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrdemServicoApplicationService implements
        CriarOrdemServicoUseCase,
        ListarOrdensServicoUseCase,
        BuscarOrdemServicoDetalhesUseCase,
        AtualizarStatusOrdemServicoUseCase,
        AcompanharOrdemServicoUseCase {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final PecaRepository pecaRepository;
    private final ServicoRepository servicoRepository;
    private final OrcamentoService orcamentoService;
    private final NotificationService notificationService;

    public OrdemServicoApplicationService(
            OrdemServicoRepository ordemServicoRepository,
            ClienteRepository clienteRepository,
            VeiculoRepository veiculoRepository,
            PecaRepository pecaRepository,
            ServicoRepository servicoRepository,
            NotificationService notificationService
    ) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.pecaRepository = pecaRepository;
        this.servicoRepository = servicoRepository;
        this.notificationService = notificationService;
        this.orcamentoService = new OrcamentoService();
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO execute(OrdemServicoRequestDTO request) {
        Cliente cliente = buscarClientePorCpfCnpj(request.cpfCnpjCliente());
        Veiculo veiculo = buscarVeiculoPorPlaca(request.placaVeiculo());

        validarRelacionamentoClienteVeiculo(cliente, veiculo);

        // Regra: não criar OS se já existe uma ativa para o mesmo cliente e veículo
        List<StatusOS> statusAtivos = List.of(
            StatusOS.RECEBIDA,
            StatusOS.EM_DIAGNOSTICO,
            StatusOS.AGUARDANDO_APROVACAO,
            StatusOS.EM_EXECUCAO
        );
        ordemServicoRepository.findByClienteIdAndVeiculoIdAndStatusIn(cliente.getId(), veiculo.getId(), statusAtivos)
            .ifPresent(osAtiva -> {
                throw new OrdemServicoAtivaException(
                    "Já existe uma ordem de serviço em andamento para este veículo.",
                    osAtiva.getStatus()
                );
            });

        OrdemServico ordemServico = new OrdemServico(cliente.getId(), veiculo.getId());

        adicionarServicos(ordemServico, request.servicosIds());
        adicionarPecas(ordemServico, request.pecasIds());


        OrdemServico salvo = ordemServicoRepository.save(ordemServico);

        return OrdemServicoResponseDTO.fromDomain(salvo, cliente.getPessoa().getName(), veiculo.getPlaca());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdemServicoResponseDTO> execute() {
        return ordemServicoRepository.findAll().stream()
                .map(this::mapearParaResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrdemServicoDetalhesDTO execute(UUID id) {
        OrdemServico ordem = buscarOrdemPorId(id);
        Cliente cliente = buscarClientePorId(ordem.getClienteId());
        Veiculo veiculo = buscarVeiculoPorId(ordem.getVeiculoId());

        return OrdemServicoDetalhesDTO.fromDomain(ordem, cliente, veiculo);
    }

    @Override
    @Transactional
    public OrdemServicoResponseDTO execute(UUID id, StatusOS novoStatus) {
        OrdemServico ordem = buscarOrdemPorId(id);

        aplicarTransicaoStatus(ordem, novoStatus);

        OrdemServico salvo = ordemServicoRepository.save(ordem);

        Cliente cliente = buscarClientePorId(salvo.getClienteId());
        Veiculo veiculo = buscarVeiculoPorId(salvo.getVeiculoId());

        // Notifica o cliente por e-mail sobre a atualização de status
        notificationService.notificarAtualizacaoStatusOrdemServico(salvo, cliente);

        return OrdemServicoResponseDTO.fromDomain(salvo, cliente.getPessoa().getName(), veiculo.getPlaca());
    }

    // MÉTODOS AUXILIARES

    private Cliente buscarClientePorCpfCnpj(String cpfCnpj) {
        return clienteRepository.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
    }

    private Cliente buscarClientePorId(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente associado à OS não encontrado"));
    }

    private Veiculo buscarVeiculoPorPlaca(String placa) {
        return veiculoRepository.findByPlaca(placa)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo não encontrado"));
    }

    private Veiculo buscarVeiculoPorId(UUID id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veículo associado à OS não encontrado"));
    }

    private OrdemServico buscarOrdemPorId(UUID id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço não encontrada com o id: " + id));
    }

    private OrdemServico buscarOrdemPorClienteCpfCnpj(String cpfCnpj) {
        Cliente cliente = buscarClientePorCpfCnpj(cpfCnpj);
        List<OrdemServico> ordens = ordemServicoRepository.findByClienteId(cliente.getId());
        if (ordens.isEmpty()) {
            throw new ResourceNotFoundException("Ordem de Serviço não encontrada para o cliente com CPF/CNPJ: " + cpfCnpj);
        }
        return ordens.get(0); // ou retorne a lista, se necessário
    }

    private void validarRelacionamentoClienteVeiculo(Cliente cliente, Veiculo veiculo) {
        boolean clienteNaoBate = veiculo.getCliente() == null ||
                !veiculo.getCliente().getId().equals(cliente.getId());
        if (clienteNaoBate) {
            throw new BusinessException("A placa informada não pertence ao cliente.");
        }
    }

    private void adicionarServicos(OrdemServico ordem, List<UUID> servicosIds) {
        if (servicosIds == null) return;
        for (UUID servicoId : servicosIds) {
            Servico servico = servicoRepository.findById(servicoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado: " + servicoId));
            ordem.adicionarServico(servico, 1);
        }
    }

    private void adicionarPecas(OrdemServico ordem, List<UUID> pecasIds) {
        if (pecasIds == null) return;
        for (UUID pecaId : pecasIds) {
            Peca peca = pecaRepository.findById(pecaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Peça não encontrada: " + pecaId));
            ordem.adicionarPeca(peca, 1);
        }
    }

    private void aplicarTransicaoStatus(OrdemServico ordem, StatusOS status) {
        try {
            switch (status) {
                case EM_DIAGNOSTICO -> ordem.iniciarDiagnostico();
                case AGUARDANDO_APROVACAO -> {
                    ordem.aguardarAprovacao();
                    notificationService.notificarClienteParaAprovacao(ordem);
                }
                case EM_EXECUCAO -> ordem.aprovar();
                case FINALIZADA -> ordem.finalizar();
                case ENTREGUE -> ordem.entregar();
                default -> throw new BusinessException("Transição de status inválida: " + status);
            }
        } catch (IllegalStateException e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private OrdemServicoResponseDTO mapearParaResponseDTO(OrdemServico ordem) {
        Cliente cliente = clienteRepository.findById(ordem.getClienteId()).orElse(null);
        Veiculo veiculo = veiculoRepository.findById(ordem.getVeiculoId()).orElse(null);

        String nomeCliente = (cliente != null) ? cliente.getPessoa().getName() : "Cliente não encontrado";
        String placa = (veiculo != null) ? veiculo.getPlaca() : "Veículo não encontrado";

        return OrdemServicoResponseDTO.fromDomain(ordem, nomeCliente, placa);
    }
}