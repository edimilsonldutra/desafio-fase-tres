package br.com.grupo99.oficinaservice.domain.model;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String placa;

    @Column(unique = true)
    private String renavam;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Construtores
    public Veiculo() {
    }

    public Veiculo(String placa, String marca, String modelo, Integer ano) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("Placa do veículo não pode ser nula ou vazia.");
        }
        if (marca == null || marca.trim().isEmpty()) {
            throw new IllegalArgumentException("Marca do veículo não pode ser nula ou vazia.");
        }
        if (modelo == null || modelo.trim().isEmpty()) {
            throw new IllegalArgumentException("Modelo do veículo não pode ser nulo ou vazio.");
        }
        if (ano == null) {
            throw new IllegalArgumentException("Ano do veículo não pode ser nulo.");
        }
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
    }

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getRenavam() { return renavam; }
    public void setRenavam(String renavam) { this.renavam = renavam; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Veiculo veiculo = (Veiculo) o;
        return Objects.equals(id, veiculo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Veiculo{" +
                "id=" + id +
                ", placa='" + placa + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", ano=" + ano +
                '}';
    }
}