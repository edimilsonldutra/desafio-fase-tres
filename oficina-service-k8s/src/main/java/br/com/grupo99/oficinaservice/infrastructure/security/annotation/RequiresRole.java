package br.com.grupo99.oficinaservice.infrastructure.security.annotation;

import br.com.grupo99.oficinaservice.domain.model.Perfil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para controle de acesso baseado em perfis.
 * 
 * Pode ser aplicada em:
 * - Métodos de controllers
 * - Classes de controllers (aplica a todos os métodos)
 * 
 * Exemplo de uso:
 * <pre>
 * {@code
 * @RequiresRole(Perfil.MECANICO)
 * public ResponseEntity<?> criarOrdemServico() {
 *     // Apenas mecânicos podem criar ordens de serviço
 * }
 * 
 * @RequiresRole({Perfil.MECANICO, Perfil.ADMIN})
 * public ResponseEntity<?> deletarCliente() {
 *     // Mecânicos e Admins podem deletar clientes
 * }
 * }
 * </pre>
 * 
 * Seguindo os princípios:
 * - Declarative Programming: Controle de acesso declarativo
 * - Open/Closed: Fechado para modificação, aberto para extensão
 * - Single Responsibility: Apenas declara permissões necessárias
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    
    /**
     * Perfis necessários para acessar o recurso.
     * Se múltiplos perfis forem especificados, o usuário precisa ter PELO MENOS UM deles (OR logic).
     * 
     * @return Array de perfis permitidos
     */
    Perfil[] value();
    
    /**
     * Mensagem personalizada de erro quando o acesso é negado.
     * Se não especificada, usa mensagem padrão.
     * 
     * @return Mensagem de erro customizada
     */
    String message() default "";
}
