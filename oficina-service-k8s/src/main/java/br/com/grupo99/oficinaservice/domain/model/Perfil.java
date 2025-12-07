package br.com.grupo99.oficinaservice.domain.model;

/**
 * Enum que representa os diferentes perfis de acesso no sistema.
 * 
 * Seguindo os princípios SOLID:
 * - Single Responsibility: Define apenas os perfis de acesso
 * - Open/Closed: Novos perfis podem ser adicionados sem modificar código existente
 */
public enum Perfil {
    
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
    
    Perfil(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Converte uma string para o enum correspondente.
     * 
     * @param perfil String representando o perfil
     * @return Perfil correspondente
     * @throws IllegalArgumentException se o perfil não for válido
     */
    public static Perfil fromString(String perfil) {
        if (perfil == null || perfil.trim().isEmpty()) {
            throw new IllegalArgumentException("Perfil não pode ser nulo ou vazio");
        }
        
        try {
            return Perfil.valueOf(perfil.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Perfil inválido: " + perfil + ". Valores aceitos: CLIENTE, MECANICO, ADMIN");
        }
    }
    
    /**
     * Verifica se o perfil tem permissão de mecânico ou superior.
     * 
     * @return true se for MECANICO ou ADMIN
     */
    public boolean isMecanicoOrHigher() {
        return this == MECANICO || this == ADMIN;
    }
    
    /**
     * Verifica se o perfil é de cliente.
     * 
     * @return true se for CLIENTE
     */
    public boolean isCliente() {
        return this == CLIENTE;
    }
}
