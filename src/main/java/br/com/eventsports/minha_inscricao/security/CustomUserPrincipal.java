package br.com.eventsports.minha_inscricao.security;

import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementação customizada do UserDetails que encapsula os dados do UsuarioEntity.
 * Fornece acesso direto aos dados específicos do domínio da aplicação.
 */
@RequiredArgsConstructor
@Getter
public class CustomUserPrincipal implements UserDetails {

    private final UsuarioEntity usuario;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Mapear TipoUsuario para roles do Spring Security
        switch (usuario.getTipo()) {
            case ADMIN:
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                break;
            case ORGANIZADOR:
                authorities.add(new SimpleGrantedAuthority("ROLE_ORGANIZADOR"));
                break;
            case ATLETA:
                authorities.add(new SimpleGrantedAuthority("ROLE_ATLETA"));
                break;
        }
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Não implementamos expiração de conta ainda
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Não implementamos bloqueio de conta ainda
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Não implementamos expiração de credenciais ainda
    }

    @Override
    public boolean isEnabled() {
        return usuario.getAtivo();
    }

    // Métodos de conveniência para acessar dados específicos do usuário
    public Long getId() {
        return usuario.getId();
    }

    public String getNome() {
        return usuario.getNome();
    }

    public String getEmail() {
        return usuario.getEmail();
    }

    public TipoUsuario getTipo() {
        return usuario.getTipo();
    }

    public boolean isOrganizador() {
        return TipoUsuario.ORGANIZADOR.equals(usuario.getTipo());
    }

    public boolean isAtleta() {
        return TipoUsuario.ATLETA.equals(usuario.getTipo());
    }
}