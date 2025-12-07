package lambdavalida.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("pessoa")
    private Pessoa pessoa;

    public AuthResponse() {
    }

    public AuthResponse(String token, Pessoa pessoa) {
        this.token = token;
        this.pessoa = pessoa;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }
}

