package br.com.grupo99.oficinaservice.infrastructure.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro de autorização que valida o acesso baseado em roles e clienteId.
 * - CLIENTE: só pode acessar suas próprias ordens de serviço (GET /api/v1/ordens-servico/{id} onde o clienteId do token == clienteId da OS)
 * - MECANICO/ADMIN: acesso total a todas as operações
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Obtém o token do header
        final String authHeader = request.getHeader("Authorization");
        
        // Se não houver token, deixa passar (o JwtRequestFilter já tratou a autenticação)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai o token
        String jwt = authHeader.substring(7);
        
        // Verifica se o usuário está autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrai perfil e pessoaId do token
        String perfil = jwtUtil.extractPerfil(jwt);
        String pessoaId = jwtUtil.extractPessoaId(jwt);

        // Se for MECANICO ou ADMIN, permite acesso total
        if ("MECANICO".equals(perfil) || "ADMIN".equals(perfil)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Se for CLIENTE, valida o acesso
        if ("CLIENTE".equals(perfil)) {
            String requestURI = request.getRequestURI();
            String method = request.getMethod();

            // CLIENTE só pode fazer GET em suas próprias ordens de serviço
            if (requestURI.matches("/api/v1/ordens-servico/[a-f0-9\\-]+")) {
                if ("GET".equals(method)) {
                    // Armazena o pessoaId no request para validação posterior no controller
                    request.setAttribute("pessoaId", pessoaId);
                    request.setAttribute("perfil", perfil);
                    filterChain.doFilter(request, response);
                    return;
                } else {
                    // CLIENTE não pode criar, atualizar ou deletar ordens de serviço
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Acesso negado. Clientes só podem consultar suas ordens de serviço.\"}");
                    return;
                }
            }

            // CLIENTE não pode acessar listagem completa de ordens de serviço
            if ("/api/v1/ordens-servico".equals(requestURI)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Acesso negado. Clientes não podem listar todas as ordens de serviço.\"}");
                return;
            }

            // CLIENTE não pode acessar outros endpoints (clientes, veículos, peças, serviços)
            if (requestURI.startsWith("/api/v1/clientes") || 
                requestURI.startsWith("/api/v1/veiculos") ||
                requestURI.startsWith("/api/v1/pecas") ||
                requestURI.startsWith("/api/v1/servicos")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Acesso negado. Clientes só podem consultar suas ordens de serviço.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
