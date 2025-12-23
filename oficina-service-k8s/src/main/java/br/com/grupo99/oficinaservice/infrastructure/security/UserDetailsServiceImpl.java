package br.com.grupo99.oficinaservice.infrastructure.security;

import br.com.grupo99.oficinaservice.domain.model.Pessoa;
import br.com.grupo99.oficinaservice.domain.repository.PessoaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PessoaRepository pessoaRepository;

    public UserDetailsServiceImpl(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscamos a pessoa pelo email (que é usado como username)
        Pessoa pessoa = pessoaRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        // Verifica se a conta está ativa
        if (pessoa.getAtivo() == null || !pessoa.getAtivo()) {
            throw new UsernameNotFoundException("Conta desativada: " + username);
        }

        // Adapta a entidade Pessoa para UserDetails
        return new UserDetailsImpl(pessoa);
    }
}
