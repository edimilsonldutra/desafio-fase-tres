package br.com.grupo99.oficinaservice.infrastructure.security.aspect;

import br.com.grupo99.oficinaservice.domain.model.Perfil;
import br.com.grupo99.oficinaservice.infrastructure.security.annotation.RequiresRole;
import br.com.grupo99.oficinaservice.infrastructure.security.jwt.JwtUserDetails;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect para validar permissões baseadas em perfis usando AOP (Aspect-Oriented Programming).
 * 
 * Intercepta métodos anotados com @RequiresRole e valida se o usuário tem permissão.
 * 
 * Seguindo os princípios:
 * - Separation of Concerns: Separa lógica de autorização da lógica de negócio
 * - DRY (Don't Repeat Yourself): Evita repetição de código de validação
 * - Open/Closed: Fechado para modificação, aberto para extensão
 * - Single Responsibility: Apenas valida autorizações
 */
@Aspect
@Component
public class AuthorizationAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationAspect.class);
    
    /**
     * Advice que executa ANTES de qualquer método anotado com @RequiresRole.
     * 
     * @param joinPoint Ponto de execução do método interceptado
     * @param requiresRole Anotação com os perfis necessários
     * @throws AccessDeniedException se o usuário não tiver permissão
     */
    @Before("@annotation(requiresRole)")
    public void checkRolePermission(JoinPoint joinPoint, RequiresRole requiresRole) {
        // Obtém informações do método para log
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        
        // Obtém o usuário autenticado do SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Tentativa de acesso não autenticado ao método: {}", methodName);
            throw new AccessDeniedException("Usuário não autenticado");
        }
        
        // Extrai JwtUserDetails do principal
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof JwtUserDetails userDetails)) {
            logger.error("Principal não é JwtUserDetails no método: {}", methodName);
            throw new AccessDeniedException("Token de autenticação inválido");
        }
        
        // Obtém os perfis necessários da anotação
        Perfil[] requiredPerfis = requiresRole.value();
        Perfil userPerfil = userDetails.getPerfil();
        
        // Verifica se o usuário tem pelo menos um dos perfis necessários
        boolean hasPermission = Arrays.stream(requiredPerfis)
                .anyMatch(perfil -> perfil == userPerfil);
        
        if (!hasPermission) {
            String message = requiresRole.message().isEmpty() 
                ? String.format("Acesso negado. Perfis necessários: %s. Perfil do usuário: %s", 
                    Arrays.toString(requiredPerfis), userPerfil)
                : requiresRole.message();
                
            logger.warn("Acesso negado ao método: {}. Usuário: {}, Perfil: {}, Perfis necessários: {}", 
                methodName, 
                userDetails.getUsername(), 
                userPerfil, 
                Arrays.toString(requiredPerfis)
            );
            
            throw new AccessDeniedException(message);
        }
        
        logger.debug("Acesso permitido ao método: {}. Usuário: {}, Perfil: {}", 
            methodName, 
            userDetails.getUsername(), 
            userPerfil
        );
    }
    
    /**
     * Advice que também verifica @RequiresRole aplicada na CLASSE (class-level).
     * Se a classe tiver a anotação, aplica a todos os métodos.
     */
    @Before("@within(requiresRole) && !@annotation(br.com.grupo99.oficinaservice.infrastructure.security.annotation.RequiresRole)")
    public void checkClassLevelRolePermission(JoinPoint joinPoint, RequiresRole requiresRole) {
        // Reutiliza a mesma lógica do método acima
        checkRolePermission(joinPoint, requiresRole);
    }
}
