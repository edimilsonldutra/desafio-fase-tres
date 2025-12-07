package br.com.grupo99.oficinaservice.adapter.presenter.impl;

import br.com.grupo99.oficinaservice.adapter.presenter.OrdemServicoPresenter;
import br.com.grupo99.oficinaservice.application.dto.*;
import br.com.grupo99.oficinaservice.domain.model.OrdemServico;
import br.com.grupo99.oficinaservice.domain.model.StatusOS;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrdemServicoPresenterImpl implements OrdemServicoPresenter {

    @Override
    public OrdemServicoResponseDTO paraResponseDTO(OrdemServico ordemServico, String clienteNome, String placaVeiculo) {
        if (ordemServico == null) {
            return null;
        }

        return new OrdemServicoResponseDTO(
                ordemServico.getId(),
                clienteNome,
                placaVeiculo,
                ordemServico.getStatus(),
                getStatusDescricao(ordemServico.getStatus()),
                ordemServico.getValorTotal(),
                ordemServico.getDataCriacao()
        );
    }

    @Override
    public OrdemServicoDetalhesDTO paraDetalhesDTO(OrdemServico ordemServico, String clienteNome, String placaVeiculo) {
        if (ordemServico == null) {
            return null;
        }

        List<ServicoResponseDTO> servicosDTO = ordemServico.getServicos().stream()
                .map(item -> new ServicoResponseDTO(
                        item.getServico().getId(),
                        item.getServico().getDescricao(),
                        item.getServico().getPreco()
                ))
                .collect(Collectors.toList());

        List<PecaResponseDTO> pecasDTO = ordemServico.getPecas().stream()
                .map(item -> new PecaResponseDTO(
                        item.getPeca().getId(),
                        item.getPeca().getNome(),
                        item.getPeca().getFabricante(),
                        item.getPeca().getPreco(),
                        item.getPeca().getEstoque()
                ))
                .collect(Collectors.toList());

        // Criar DTOs simplificados de Cliente e Veículo
        ClienteResponseDTO clienteDTO = new ClienteResponseDTO(
                ordemServico.getClienteId(),
                clienteNome,
                null,
                null,
                null
        );

        VeiculoResponseDTO veiculoDTO = new VeiculoResponseDTO(
                ordemServico.getVeiculoId(),
                placaVeiculo,
                null,
                null,
                null,
                null
        );

        return new OrdemServicoDetalhesDTO(
                ordemServico.getId(),
                ordemServico.getStatus(),
                ordemServico.getValorTotal(),
                ordemServico.getDataCriacao(),
                ordemServico.getDataFinalizacao(),
                ordemServico.getDataEntrega(),
                clienteDTO,
                veiculoDTO,
                servicosDTO,
                pecasDTO
        );
    }

    @Override
    public List<OrdemServicoResponseDTO> paraListaResponseDTO(List<OrdemServico> ordensServico) {
        if (ordensServico == null) {
            return List.of();
        }

        // Nota: Este método requer informações de Cliente e Veículo que não estão disponíveis aqui
        // Em um cenário real, você precisaria buscar essas informações ou repensar o design
        return ordensServico.stream()
                .map(os -> paraResponseDTO(os, "Cliente", "Placa"))
                .collect(Collectors.toList());
    }

    private String getStatusDescricao(StatusOS status) {
        return switch (status) {
            case RECEBIDA -> "Recebida";
            case EM_DIAGNOSTICO -> "Diagnóstico";
            case AGUARDANDO_APROVACAO -> "Aguardando Aprovação";
            case EM_EXECUCAO -> "Execução";
            case FINALIZADA -> "Finalizada";
            case ENTREGUE -> "Entregue";
            case CANCELADA -> "Cancelada";
        };
    }
}

