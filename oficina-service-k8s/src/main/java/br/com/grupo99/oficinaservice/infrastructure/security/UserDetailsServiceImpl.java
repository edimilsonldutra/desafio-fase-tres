package br.com.grupo99.oficinaservice.infrastructure.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: Implementar a busca do utilizador no banco de dados (ex: ClienteRepository)
        // Por enquanto, usaremos um utilizador em memória para fins de demonstração.
        if ("admin".equals(username)) {
            return new User("admin", "$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6", new ArrayList<>()); // senha = "password"
        } else {
            throw new UsernameNotFoundException("Utilizador não encontrado: " + username);
        }
    }
}
