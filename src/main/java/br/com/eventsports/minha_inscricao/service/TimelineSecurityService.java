package br.com.eventsports.minha_inscricao.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineSecurityService {

    private final EventoSecurityService eventoSecurityService;

    /**
     * Verifica se o usuário pode gerenciar timeline para um evento específico
     * (baseado no evento ao qual a timeline pertence)
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar a timeline
     */
    public boolean canManageTimelineForEvento(Long eventoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (eventoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageTimelineForEvento - eventoId: {}, userEmail: {}", eventoId, userEmail);
            return false;
        }

        try {
            // Verificar se é o admin especial (pode gerenciar qualquer timeline)
            if ("admin@admin.com".equals(userEmail)) {
                log.debug("Usuário {} é admin especial - pode gerenciar timeline do evento {}", userEmail, eventoId);
                return true;
            }

            // Delegar para EventoSecurityService - se pode gerenciar evento, pode gerenciar timeline
            boolean canManage = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} gerenciar timeline do evento {} (ownership: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", eventoId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento de timeline no evento {} para usuário {}: {}", 
                     eventoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode criar timeline para um evento específico
     * (mesma lógica do gerenciamento)
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode criar timeline para o evento
     */
    public boolean canCreateTimelineForEvento(Long eventoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        // Para timeline, criar e gerenciar têm as mesmas regras
        return canManageTimelineForEvento(eventoId, userEmail, authorities);
    }
}