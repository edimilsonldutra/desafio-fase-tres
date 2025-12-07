package br.com.grupo99.oficinaservice.infrastructure.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do OpenAPI (Swagger) para documentação da API.
 * Define o título, a versão e o esquema de segurança JWT.
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth", // Nome do esquema de segurança
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@OpenAPIDefinition(
        info = @Info(title = "API de Gestão de Oficina", version = "v1", description = "Documentação da API para o sistema de gestão de oficina."),
        security = @SecurityRequirement(name = "bearerAuth") // Aplica o esquema de segurança a todos os endpoints
)
public class OpenApiConfig {
}
