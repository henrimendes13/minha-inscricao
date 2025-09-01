package br.com.eventsports.minha_inscricao.util;

import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;

/**
 * Classe utilitária para determinar o tipo de usuário baseado em seus relacionamentos
 */
public class TipoUsuarioUtil {

    private TipoUsuarioUtil() {
        // Classe utilitária - construtor privado
    }

    /**
     * Determina o tipo de usuário baseado em seus relacionamentos
     * NOTA: ADMIN é reservado exclusivamente para desenvolvedores/sistema
     * 
     * @param usuario o usuário a ser analisado
     * @return o tipo de usuário determinado
     */
    public static TipoUsuario determinarTipo(UsuarioEntity usuario) {
        if (usuario == null) {
            return TipoUsuario.ATLETA; // Padrão
        }

        // ADMIN é determinado apenas por configuração manual/sistema, não por relacionamentos
        // Usuários da aplicação nunca recebem tipo ADMIN automaticamente
        
        boolean temEventos = usuario.getEventosOrganizados() != null && 
                            !usuario.getEventosOrganizados().isEmpty();
        
        if (temEventos) {
            return TipoUsuario.ORGANIZADOR; // Usuário que organiza eventos
        } else {
            return TipoUsuario.ATLETA; // Padrão (inclui quem só se inscreve)
        }
    }

    /**
     * Verifica se o usuário pode organizar eventos
     * 
     * @param usuario o usuário a ser verificado
     * @return true se pode organizar eventos
     */
    public static boolean podeOrganizarEventos(UsuarioEntity usuario) {
        if (usuario == null || !usuario.getAtivo()) {
            return false;
        }
        
        // Qualquer usuário ativo e verificado pode organizar eventos
        return usuario.getVerificado();
    }

    /**
     * Verifica se o usuário pode se inscrever em eventos
     * 
     * @param usuario o usuário a ser verificado
     * @return true se pode se inscrever
     */
    public static boolean podeSeInscreverEmEventos(UsuarioEntity usuario) {
        if (usuario == null || !usuario.getAtivo()) {
            return false;
        }
        
        return usuario.getAceitaTermos();
    }

    /**
     * Retorna a descrição do tipo do usuário
     * 
     * @param usuario o usuário
     * @return descrição do tipo
     */
    public static String getDescricaoTipo(UsuarioEntity usuario) {
        return determinarTipo(usuario).getDescricao();
    }

    /**
     * Verifica se o usuário tem privilégios administrativos
     * NOTA: Usuários ADMIN devem ser criados manualmente pelos desenvolvedores
     * 
     * @param usuario o usuário
     * @return true se é admin
     */
    public static boolean isAdmin(UsuarioEntity usuario) {
        // Como ADMIN não é mais determinado automaticamente,
        // precisamos verificar se o usuário foi marcado explicitamente como ADMIN
        // Isso pode ser feito através de um campo específico ou verificação no email
        if (usuario == null) {
            return false;
        }
        
        // Apenas admin@admin.com é considerado admin especial
        String email = usuario.getEmail();
        return "admin@admin.com".equals(email);
    }

    /**
     * Verifica se o usuário pode executar ações administrativas globais
     * (diferente de organizadores que só podem gerenciar seus eventos)
     * 
     * @param usuario o usuário
     * @return true se tem privilégios de admin global
     */
    public static boolean podeExecutarAcoesAdmin(UsuarioEntity usuario) {
        return isAdmin(usuario) && usuario.getAtivo();
    }
}