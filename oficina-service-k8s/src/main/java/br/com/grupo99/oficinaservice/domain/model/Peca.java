package br.com.grupo99.oficinaservice.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Peca {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String nome;
    private String fabricante;
    private BigDecimal preco;
    private int estoque;

    // Construtor
    public Peca() {
    }

    public Peca(String nome, String fabricante, BigDecimal preco, int estoque) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da peça não pode ser nulo ou vazio.");
        }
        this.nome = nome;
        this.fabricante = fabricante;
        this.preco = preco;
        this.estoque = estoque;
    }

    // Métodos de negócio
    public void baixarEstoque(int quantidade) {
        if (this.estoque < quantidade) {
            throw new IllegalStateException("Estoque insuficiente.");
        }
        this.estoque -= quantidade;
    }

    public void adicionarEstoque(int quantidade) {
        this.estoque += quantidade;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome da peça não pode ser nulo ou vazio.");
        }
        this.nome = nome;
    }
    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    // Equals e HashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peca peca = (Peca) o;
        return Objects.equals(id, peca.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Peca{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", fabricante='" + fabricante + '\'' +
                ", preco=" + preco +
                ", estoque=" + estoque +
                '}';
    }
}