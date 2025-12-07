package lambdavalida.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {

    @JsonProperty("token")
    private String token;

    @JsonProperty("customer")
    private Customer customer;

    public AuthResponse() {
    }

    public AuthResponse(String token, Customer customer) {
        this.token = token;
        this.customer = customer;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}

