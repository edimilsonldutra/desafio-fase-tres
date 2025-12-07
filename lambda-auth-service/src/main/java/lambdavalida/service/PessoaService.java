package lambdavalida.service;

import com.newrelic.api.agent.NewRelic;
import lambdavalida.model.Pessoa;

import java.util.UUID;

public class PessoaService {
    public PessoaService() {}

    public Pessoa findByDocumento(String documento) {
        try {
            // Simulação: buscar do banco de dados RDS PostgreSQL
            // TODO: Implementar consulta real ao banco de dados
            // Query: SELECT * FROM pessoas WHERE numero_documento = ?
            
            // Exemplo de pessoa física (CPF)
            if ("11144477735".equals(documento)) {
                return new Pessoa(
                    "550e8400-e29b-41d4-a716-446655440000",  // ID da pessoa no banco
                    documento,
                    "FISICA",      // FISICA ou JURIDICA
                    "João Silva",
                    "joao@example.com",
                    null,          // cargo (opcional)
                    "CLIENTE",     // CLIENTE, MECANICO ou ADMIN
                    "ACTIVE"
                );
            }
            
            // Exemplo de mecânico/admin para testes
            if ("12345678900".equals(documento)) {
                return new Pessoa(
                    "660e8400-e29b-41d4-a716-446655440001",
                    documento,
                    "FISICA",
                    "Maria Oliveira",
                    "maria@oficina.com",
                    "Mecânica",    // cargo
                    "MECANICO",    // Mecânico tem acesso total
                    "ACTIVE"
                );
            }
            
            // Exemplo de pessoa jurídica (CNPJ)
            if ("12345678000190".equals(documento)) {
                return new Pessoa(
                    "770e8400-e29b-41d4-a716-446655440002",
                    documento,
                    "JURIDICA",
                    "Empresa XYZ Ltda",
                    "contato@empresaxyz.com",
                    null,
                    "CLIENTE",
                    "ACTIVE"
                );
            }
            
            return null;
        } catch (Exception e) {
            NewRelic.noticeError(e);
            throw new RuntimeException("Pessoa query failed", e);
        }
    }
}
