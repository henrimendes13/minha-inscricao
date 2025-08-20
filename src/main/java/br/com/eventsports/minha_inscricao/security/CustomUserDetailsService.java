package br.com.eventsports.minha_inscricao.security;

import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Tentando carregar usuário por email: {}", email);
        
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: {}", email);
                    return new UsernameNotFoundException("Usuário não encontrado: " + email);
                });

        if (!usuario.getAtivo()) {
            log.warn("Usuário inativo tentando fazer login: {}", email);
            throw new UsernameNotFoundException("Usuário inativo: " + email);
        }

        log.debug("Usuário carregado com sucesso: {} - Tipo: {}", email, usuario.getTipo());

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getSenha())
                .authorities(getAuthorities(usuario.getTipo()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.getAtivo())
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(TipoUsuario tipoUsuario) {
        String role = mapTipoUsuarioToRole(tipoUsuario);
        log.debug("Mapeando tipo {} para role {}", tipoUsuario, role);
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    private String mapTipoUsuarioToRole(TipoUsuario tipoUsuario) {
        return switch (tipoUsuario) {
            case ORGANIZADOR -> "ROLE_ORGANIZADOR";
            case ATLETA -> "ROLE_ATLETA";
        };
    }
}