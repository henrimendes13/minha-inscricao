package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.repository.InscricaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class InscricaoSecurityService {

    private final InscricaoRepository inscricaoRepository;
    private final EventoSecurityService eventoSecurityService;

    /**
     * Verifica se o usuário pode gerenciar a inscrição específica
     * REGRA: Organizadores do evento podem gerenciar qualquer inscrição do evento
     * Atletas podem gerenciar apenas suas próprias inscrições
     * 
     * @param inscricaoId ID da inscrição
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar a inscrição
     */
    public boolean canManageInscricao(Long inscricaoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (inscricaoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageInscricao - inscricaoId: {}, userEmail: {}", inscricaoId, userEmail);
            return false;
        }

        try {
            // Verificar se é o admin especial (pode gerenciar qualquer inscrição)
            if ("admin@admin.com".equals(userEmail)) {
                log.debug("Usuário {} é admin especial - pode gerenciar inscrição {}", userEmail, inscricaoId);
                return true;
            }

            // Buscar a inscrição para verificar ownership
            return inscricaoRepository.findById(inscricaoId)
                    .map(inscricao -> {
                        // Verificar se é o próprio atleta da inscrição
                        if (inscricao.getAtleta() != null && 
                            inscricao.getAtleta().getEmail().equals(userEmail)) {
                            log.debug("Usuário {} é o próprio atleta da inscrição {}", userEmail, inscricaoId);
                            return true;
                        }

                        // Verificar se é o organizador do evento
                        Long eventoId = inscricao.getEvento().getId();
                        boolean isOrganizador = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
                        
                        log.debug("Usuário {} {} gerenciar inscrição {} (evento: {}, organizador: {})", 
                                 userEmail, isOrganizador ? "PODE" : "NÃO PODE", inscricaoId, eventoId, isOrganizador);
                        
                        return isOrganizador;
                    })
                    .orElse(false);

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento da inscrição {} para usuário {}: {}", 
                     inscricaoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode criar inscrição para um evento específico
     * REGRA: Qualquer usuário autenticado pode criar inscrição (se inscrever em eventos)
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode criar inscrição para o evento
     */
    public boolean canCreateInscricaoForEvento(Long eventoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (eventoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canCreateInscricaoForEvento - eventoId: {}, userEmail: {}", eventoId, userEmail);
            return false;
        }

        try {
            // Verificar se o evento existe
            if (!eventoSecurityService.eventoExists(eventoId)) {
                log.warn("Evento {} não existe", eventoId);
                return false;
            }

            // Qualquer usuário autenticado pode criar inscrição
            log.debug("Usuário {} pode criar inscrição para evento {}", userEmail, eventoId);
            return true;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de criação de inscrição no evento {} para usuário {}: {}", 
                     eventoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode gerenciar inscrições para um evento específico
     * REGRA: Apenas organizadores do evento podem gerenciar suas inscrições
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar inscrições do evento
     */
    public boolean canManageInscricoesForEvento(Long eventoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (eventoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageInscricoesForEvento - eventoId: {}, userEmail: {}", eventoId, userEmail);
            return false;
        }

        try {
            // Delegar para EventoSecurityService - se pode gerenciar evento, pode gerenciar suas inscrições
            boolean canManage = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} gerenciar inscrições para evento {} (ownership: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", eventoId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento de inscrições no evento {} para usuário {}: {}", 
                     eventoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode visualizar inscrição específica
     * REGRA: Atletas podem ver suas próprias inscrições, organizadores podem ver inscrições do evento
     * 
     * @param inscricaoId ID da inscrição
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode visualizar a inscrição
     */
    public boolean canViewInscricao(Long inscricaoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        // Para visualização, utilizar a mesma lógica de gerenciamento
        return canManageInscricao(inscricaoId, userEmail, authorities);
    }

    /**
     * Verifica se a inscrição existe
     * 
     * @param inscricaoId ID da inscrição
     * @return true se a inscrição existe
     */
    public boolean inscricaoExists(Long inscricaoId) {
        if (inscricaoId == null) {
            return false;
        }
        
        try {
            return inscricaoRepository.existsById(inscricaoId);
        } catch (Exception e) {
            log.error("Erro ao verificar existência da inscrição {}: {}", inscricaoId, e.getMessage());
            return false;
        }
    }

    /**
     * Busca o ID do evento ao qual a inscrição pertence
     * 
     * @param inscricaoId ID da inscrição
     * @return ID do evento ou null se não encontrado
     */
    public Long getEventoIdByInscricao(Long inscricaoId) {
        if (inscricaoId == null) {
            return null;
        }

        try {
            return inscricaoRepository.findById(inscricaoId)
                    .map(inscricao -> inscricao.getEvento().getId())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar evento da inscrição {}: {}", inscricaoId, e.getMessage());
            return null;
        }
    }
}