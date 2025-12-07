package br.com.grupo99.oficinaservice.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
public class ItemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false)
    private BigDecimal valorUnitario;

    @Column(nullable = false)
    private BigDecimal valorTotal;

    // Construtores
    public ItemServico() {
    }

    public ItemServico(Servico servico, Integer quantidade) {
        if (servico == null) {
            throw new IllegalArgumentException("Serviço não pode ser nulo.");
        }
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        this.servico = servico;
        this.quantidade = quantidade;
        this.valorUnitario = servico.getPreco();
        this.valorTotal = this.valorUnitario.multiply(new BigDecimal(quantidade));
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Servico getServico() { return servico; }
    public void setServico(Servico servico) { this.servico = servico; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        if (this.valorUnitario == null) {
            throw new IllegalStateException("Valor unitário deve ser definido antes de alterar a quantidade.");
        }
        this.quantidade = quantidade;
        this.valorTotal = this.valorUnitario.multiply(new BigDecimal(quantidade));
    }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemServico that = (ItemServico) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ItemServico{" +
                "id=" + id +
                ", servico=" + (servico != null ? servico.getDescricao() : null) +
                ", quantidade=" + quantidade +
                ", valorUnitario=" + valorUnitario +
                ", valorTotal=" + valorTotal +
                '}';
    }
}