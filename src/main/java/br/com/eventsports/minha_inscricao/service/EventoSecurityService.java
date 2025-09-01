package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventoSecurityService {

    private final EventoRepository eventoRepository;

    /**
     * Verifica se o usuário é organizador/dono do evento específico
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado
     * @return true se o usuário é o organizador do evento
     */
    public boolean isEventoOwner(Long eventoId, String userEmail) {
        if (eventoId == null || userEmail == null) {
            log.warn("EventoId ou userEmail é null - eventoId: {}, userEmail: {}", eventoId, userEmail);
            return false;
        }

        try {
            boolean isOwner = eventoRepository.findById(eventoId)
                    .map(evento -> {
                        if (evento.getOrganizador() == null) {
                            log.warn("Evento {} não tem organizador definido", eventoId);
                            return false;
                        }
                        boolean matches = userEmail.equals(evento.getOrganizador().getEmail());
                        log.debug("Verificando ownership - Evento: {}, User: {}, Organizador: {}, IsOwner: {}", 
                                 eventoId, userEmail, evento.getOrganizador().getEmail(), matches);
                        return matches;
                    })
                    .orElse(false);

            if (!isOwner) {
                log.debug("Usuário {} NÃO é dono do evento {}", userEmail, eventoId);
            }

            return isOwner;

        } catch (Exception e) {
            log.error("Erro ao verificar ownership do evento {} para usuário {}: {}", eventoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode gerenciar o evento (ADMIN ou organizador/dono)
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado  
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar o evento
     */
    public boolean canManageEvento(Long eventoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (eventoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageEvento - eventoId: {}, userEmail: {}", eventoId, userEmail);
            return false;
        }

        try {
            // Verificar se é ADMIN (pode gerenciar qualquer evento)
            boolean isAdmin = authorities.stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

            if (isAdmin) {
                log.debug("Usuário {} é ADMIN - pode gerenciar evento {}", userEmail, eventoId);
                return true;
            }

            // Se não é admin, verificar se é o organizador do evento
            boolean canManage = isEventoOwner(eventoId, userEmail);
            
            log.debug("Usuário {} {} gerenciar evento {} (não é admin, ownership: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", eventoId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento do evento {} para usuário {}: {}", 
                     eventoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode criar eventos (ADMIN ou ORGANIZADOR)
     * 
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode criar eventos
     */
    public boolean canCreateEvento(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) {
            return false;
        }

        return authorities.stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()) || 
                                "ROLE_ORGANIZADOR".equals(auth.getAuthority()));
    }

    /**
     * Verifica se o evento existe
     * 
     * @param eventoId ID do evento
     * @return true se o evento existe
     */
    public boolean eventoExists(Long eventoId) {
        if (eventoId == null) {
            return false;
        }
        
        try {
            return eventoRepository.existsById(eventoId);
        } catch (Exception e) {
            log.error("Erro ao verificar existência do evento {}: {}", eventoId, e.getMessage());
            return false;
        }
    }

    /**
     * Busca o organizador do evento (para logs/auditoria)
     * 
     * @param eventoId ID do evento
     * @return Email do organizador ou null se não encontrado
     */
    public String getEventoOwnerEmail(Long eventoId) {
        if (eventoId == null) {
            return null;
        }

        try {
            return eventoRepository.findById(eventoId)
                    .map(evento -> evento.getOrganizador() != null ? evento.getOrganizador().getEmail() : null)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar organizador do evento {}: {}", eventoId, e.getMessage());
            return null;
        }
    }
}