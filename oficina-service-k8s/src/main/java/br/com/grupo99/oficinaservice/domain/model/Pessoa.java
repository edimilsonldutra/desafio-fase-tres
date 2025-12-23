package br.com.grupo99.oficinaservice.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pessoas")
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 14)
    private String numeroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false, length = 10)
    private TipoPessoa tipoPessoa;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String cargo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Perfil perfil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relacionamento opcional com Cliente (apenas se perfil = CLIENTE)
    @OneToOne(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cliente cliente;

    // Relacionamento opcional com Funcionario (apenas se perfil = MECANICO ou
    // ADMIN)
    @OneToOne(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Funcionario funcionario;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Construtores
    public Pessoa() {
    }

    /**
     * Construtor completo com senha
     */
    public Pessoa(String numeroDocumento, TipoPessoa tipoPessoa, String name, String email, String senha,
            Perfil perfil) {
        validateNumeroDocumento(numeroDocumento, tipoPessoa);
        validateName(name);
        validateEmail(email);
        validateSenha(senha);

        this.numeroDocumento = numeroDocumento;
        this.tipoPessoa = tipoPessoa;
        this.name = name;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = true;
    }

    /**
     * Construtor sem senha - utilizado apenas para testes legados
     * 
     * @deprecated Use o construtor com senha. Este existe apenas para
     *             compatibilidade com testes
     */
    @Deprecated
    public Pessoa(String numeroDocumento, TipoPessoa tipoPessoa, String name, String email, Perfil perfil) {
        this(numeroDocumento, tipoPessoa, name, email, "$2a$10$defaultTestPassword", perfil);
    }

    // Validações
    private void validateNumeroDocumento(String documento, TipoPessoa tipo) {
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("Número do documento não pode ser nulo ou vazio");
        }
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo de pessoa não pode ser nulo");
        }
        if (!tipo.validarTamanhoDocumento(documento)) {
            throw new IllegalArgumentException(
                    "Documento inválido para " + tipo.getDisplayName() +
                            ". Esperado: " + tipo.getTamanhoDocumento() + " dígitos");
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email não pode ser nulo ou vazio");
        }
        // Validação básica de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Email inválido");
        }
    }

    private void validateSenha(String senha) {
        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia");
        }
        if (senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres");
        }
    }

    // Getters e Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Pessoa pessoa = (Pessoa) o;
        return Objects.equals(id, pessoa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pessoa{" +
                "id=" + id +
                ", numeroDocumento='" + numeroDocumento + '\'' +
                ", tipoPessoa=" + tipoPessoa +
                ", name='" + name + '\'' +
                ", perfil=" + perfil +
                '}';
    }
}
