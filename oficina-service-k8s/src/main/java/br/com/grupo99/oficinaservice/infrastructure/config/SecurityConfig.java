package br.com.grupo99.oficinaservice.infrastructure.config;

import br.com.grupo99.oficinaservice.infrastructure.security.jwt.JwtAuthorizationFilter;
import br.com.grupo99.oficinaservice.infrastructure.security.jwt.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração principal do Spring Security.
 * Define as regras de autorização para os endpoints da API.
 * 
 * Habilita:
 * - Method Security: Para uso de @PreAuthorize, @Secured, etc.
 * - AspectJ Proxy: Para interceptação via AOP (@RequiresRole)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableAspectJAutoProxy
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;

    @Value("${security.disabled:false}")
    private boolean securityDisabled;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter, JwtAuthorizationFilter jwtAuthorizationFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    /**
     * Configuração que instrui o Spring Security a IGNORAR completamente
     * certos caminhos. É mais eficaz para recursos estáticos e endpoints públicos.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/api/v1/auth/**",
                "/actuator/health/**",
                "/actuator/health",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/api-docs/**",
                "/v3/api-docs/**",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/webjars/**"
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        if (securityDisabled) {
            http.csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            return http.build();
        }
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Endpoints de ordens de serviço são protegidos por @RequiresRole e validação customizada
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordens-servico/**").authenticated()
                        // Todos os outros endpoints da API exigem autenticação
                        // A autorização baseada em roles é feita via @RequiresRole nos controllers
                        .requestMatchers("/api/v1/**").authenticated()
                        // Permite requisições não autenticadas para outros paths
                        .anyRequest().permitAll()
                )
                // Configura a gestão de sessão para ser stateless, pois usaremos JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Adiciona o filtro JWT antes do filtro padrão de autenticação de utilizador/senha
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}