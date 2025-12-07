package lambdavalida.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pessoa {

    @JsonProperty("id")
    private String id;

    @JsonProperty("numeroDocumento")
    private String numeroDocumento;

    @JsonProperty("tipoPessoa")
    private String tipoPessoa;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("cargo")
    private String cargo;

    @JsonProperty("perfil")
    private String perfil;

    @JsonProperty("status")
    private String status;

    public Pessoa() {
    }

    public Pessoa(String id, String numeroDocumento, String tipoPessoa, String name, 
                  String email, String cargo, String perfil, String status) {
        this.id = id;
        this.numeroDocumento = numeroDocumento;
        this.tipoPessoa = tipoPessoa;
        this.name = name;
        this.email = email;
        this.cargo = cargo;
        this.perfil = perfil;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTipoPessoa() {
        return tipoPessoa;
    }

    public void setTipoPessoa(String tipoPessoa) {
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

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

