package br.com.grupo99.oficinaservice.domain.model;

/**
 * Enum que representa os diferentes perfis de usuário no sistema.
 * 
 * Seguindo os princípios SOLID:
 * - Single Responsibility: Define apenas os papéis do sistema
 * - Open/Closed: Novos papéis podem ser adicionados sem modificar código existente
 */
public enum UserRole {
    
    /**
     * Cliente da oficina.
     * Permissões:
     * - Consultar apenas suas próprias ordens de serviço (GET /api/v1/ordens-servico)
     */
    CLIENTE("Cliente"),
    
    /**
     * Mecânico/Atendente da oficina.
     * Permissões:
     * - Acesso total a todas as APIs
     * - Criar, editar e consultar ordens de serviço
     * - Gerenciar clientes, veículos, peças e serviços
     */
    MECANICO("Mecânico"),
    
    /**
     * Administrador do sistema.
     * Permissões:
     * - Acesso completo a todas as funcionalidades
     * - Gerenciar usuários e configurações do sistema
     */
    ADMIN("Administrador");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Converte uma string para o enum correspondente.
     * 
     * @param role String representando o role
     * @return UserRole correspondente
     * @throws IllegalArgumentException se o role não for válido
     */
    public static UserRole fromString(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role não pode ser nulo ou vazio");
        }
        
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role inválido: " + role + ". Valores aceitos: CLIENTE, MECANICO, ADMIN");
        }
    }
    
    /**
     * Verifica se o role tem permissão de mecânico ou superior.
     * 
     * @return true se for MECANICO ou ADMIN
     */
    public boolean isMecanicoOrHigher() {
        return this == MECANICO || this == ADMIN;
    }
    
    /**
     * Verifica se o role é de cliente.
     * 
     * @return true se for CLIENTE
     */
    public boolean isCliente() {
        return this == CLIENTE;
    }
}
