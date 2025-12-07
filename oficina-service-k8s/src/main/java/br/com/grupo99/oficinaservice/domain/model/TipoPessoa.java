package br.com.grupo99.oficinaservice.domain.model;

/**
 * Enum que representa o tipo de pessoa no sistema.
 * 
 * Seguindo os princípios SOLID:
 * - Single Responsibility: Define apenas os tipos de pessoa
 * - Open/Closed: Novos tipos podem ser adicionados sem modificar código existente
 */
public enum TipoPessoa {
    
    /**
     * Pessoa Física (CPF).
     * Documento: CPF com 11 dígitos
     */
    FISICA("Pessoa Física", "CPF", 11),
    
    /**
     * Pessoa Jurídica (CNPJ).
     * Documento: CNPJ com 14 dígitos
     */
    JURIDICA("Pessoa Jurídica", "CNPJ", 14);
    
    private final String displayName;
    private final String tipoDocumento;
    private final int tamanhoDocumento;
    
    TipoPessoa(String displayName, String tipoDocumento, int tamanhoDocumento) {
        this.displayName = displayName;
        this.tipoDocumento = tipoDocumento;
        this.tamanhoDocumento = tamanhoDocumento;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    
    public int getTamanhoDocumento() {
        return tamanhoDocumento;
    }
    
    /**
     * Converte uma string para o enum correspondente.
     * 
     * @param tipo String representando o tipo de pessoa
     * @return TipoPessoa correspondente
     * @throws IllegalArgumentException se o tipo não for válido
     */
    public static TipoPessoa fromString(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de pessoa não pode ser nulo ou vazio");
        }
        
        try {
            return TipoPessoa.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de pessoa inválido: " + tipo + ". Valores aceitos: FISICA, JURIDICA");
        }
    }
    
    /**
     * Valida se o documento possui o tamanho correto para o tipo de pessoa.
     * 
     * @param documento Número do documento sem formatação
     * @return true se o documento é válido para o tipo
     */
    public boolean validarTamanhoDocumento(String documento) {
        if (documento == null) {
            return false;
        }
        // Remove qualquer formatação (pontos, traços, barras)
        String documentoLimpo = documento.replaceAll("[^0-9]", "");
        return documentoLimpo.length() == this.tamanhoDocumento;
    }
    
    /**
     * Verifica se é pessoa física.
     * 
     * @return true se for FISICA
     */
    public boolean isFisica() {
        return this == FISICA;
    }
    
    /**
     * Verifica se é pessoa jurídica.
     * 
     * @return true se for JURIDICA
     */
    public boolean isJuridica() {
        return this == JURIDICA;
    }
}
