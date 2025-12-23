package br.com.grupo99.oficinaservice.domain.model;


public enum Perfil {
    

    CLIENTE("Cliente"),
    

    MECANICO("Mecânico"),
    
    ADMIN("Administrador");
    
    private final String displayName;
    
    Perfil(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
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

    public boolean isMecanicoOrHigher() {
        return this == MECANICO || this == ADMIN;
    }
    
    public boolean isCliente() {
        return this == CLIENTE;
    }
}
