package br.com.grupo99.oficinaservice.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID clienteId;

    @Column(nullable = false)
    private UUID veiculoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOS status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ordem_servico_id")
    private List<ItemServico> servicos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "ordem_servico_id")
    private List<ItemPeca> pecas = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal valorTotal;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataFinalizacao;
    private LocalDateTime dataEntrega;

    public OrdemServico() {
    }

    public OrdemServico(UUID clienteId, UUID veiculoId) {
        if (clienteId == null) {
            throw new IllegalArgumentException("ClienteId não pode ser nulo.");
        }
        if (veiculoId == null) {
            throw new IllegalArgumentException("VeiculoId não pode ser nulo.");
        }
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.status = StatusOS.RECEBIDA;
        this.dataCriacao = java.time.LocalDateTime.now();
        this.valorTotal = java.math.BigDecimal.ZERO;
    }

    // --- Métodos de Negócio ---

    public void adicionarServico(Servico servico, Integer quantidade) {
        if (servico == null) {
            throw new IllegalArgumentException("Serviço não pode ser nulo.");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        ItemServico item = new ItemServico(servico, quantidade);
        this.servicos.add(item);
        atualizarValorTotal();
    }

    public void adicionarPeca(Peca peca, Integer quantidade) {
        if (peca == null) {
            throw new IllegalArgumentException("Peça não pode ser nula.");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (peca.getEstoque() < quantidade) {
            throw new IllegalStateException("Estoque insuficiente para a peça: " + peca.getNome());
        }
        ItemPeca item = new ItemPeca(peca, quantidade);
        this.pecas.add(item);
        peca.baixarEstoque(quantidade);
        atualizarValorTotal();
    }

    public void removerServico(UUID itemServicoId) {
        boolean removed = servicos.removeIf(item -> java.util.Objects.equals(item.getId(), itemServicoId));
        if (!removed) {
            throw new IllegalArgumentException("ItemServico não encontrado para remoção.");
        }
        atualizarValorTotal();
    }

    public void removerPeca(UUID itemPecaId) {
        boolean removed = pecas.removeIf(item -> java.util.Objects.equals(item.getId(), itemPecaId));
        if (!removed) {
            throw new IllegalArgumentException("ItemPeca não encontrado para remoção.");
        }
        atualizarValorTotal();
    }

    private void atualizarValorTotal() {
        BigDecimal totalServicos = servicos.stream()
            .map(ItemServico::getValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPecas = pecas.stream()
            .map(ItemPeca::getValorTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.valorTotal = totalServicos.add(totalPecas);
    }

    public void iniciarDiagnostico() {
        if (this.status != StatusOS.RECEBIDA) {
            throw new IllegalStateException("OS não pode iniciar diagnóstico pois não está no status 'Recebida'");
        }
        this.status = StatusOS.EM_DIAGNOSTICO;
    }

    public void aguardarAprovacao() {
        if (this.status != StatusOS.EM_DIAGNOSTICO) {
            throw new IllegalStateException("OS não pode aguardar aprovação pois não está em diagnóstico");
        }
        this.status = StatusOS.AGUARDANDO_APROVACAO;
    }

    public void aprovar() {
        if (this.status != StatusOS.AGUARDANDO_APROVACAO) {
            throw new IllegalStateException("OS não pode ser aprovada pois não está aguardando aprovação");
        }
        this.status = StatusOS.EM_EXECUCAO;
    }

    public void finalizar() {
        if (this.status != StatusOS.EM_EXECUCAO) {
            throw new IllegalStateException("OS não pode ser finalizada pois não está em execução");
        }
        this.status = StatusOS.FINALIZADA;
        this.dataFinalizacao = LocalDateTime.now();
    }

    public void entregar() {
        if (this.status != StatusOS.FINALIZADA) {
            throw new IllegalStateException("OS não pode ser entregue pois não foi finalizada");
        }
        this.status = StatusOS.ENTREGUE;
        this.dataEntrega = LocalDateTime.now();
    }

    // --- Getters e Setters ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public void setClienteId(UUID clienteId) {
        this.clienteId = clienteId;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public void setVeiculoId(UUID veiculoId) {
        this.veiculoId = veiculoId;
    }

    public StatusOS getStatus() {
        return status;
    }

    public void setStatus(StatusOS status) {
        this.status = status;
    }

    public List<ItemServico> getServicos() {
        return servicos;
    }

    public void setServicos(List<ItemServico> servicos) {
        this.servicos = servicos;
    }

    public List<ItemPeca> getPecas() {
        return pecas;
    }

    public void setPecas(List<ItemPeca> pecas) {
        this.pecas = pecas;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public LocalDateTime getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    // --- Equals, HashCode e ToString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrdemServico that = (OrdemServico) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrdemServico{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", veiculoId=" + veiculoId +
                ", status=" + status +
                ", valorTotal=" + valorTotal +
                '}';
    }
}
