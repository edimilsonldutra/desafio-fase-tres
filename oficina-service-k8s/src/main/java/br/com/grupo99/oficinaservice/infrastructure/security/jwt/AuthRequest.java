package br.com.grupo99.oficinaservice.infrastructure.security.jwt;

public record AuthRequest(String username, String password) {
}
