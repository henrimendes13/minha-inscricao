package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.repository.LeaderboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardSecurityService {

    private final LeaderboardRepository leaderboardRepository;
    private final EventoSecurityService eventoSecurityService;

    /**
     * Verifica se o usuário pode gerenciar o leaderboard específico
     * (baseado no evento ao qual o leaderboard pertence)
     * 
     * @param leaderboardId ID do leaderboard
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar o leaderboard
     */
    public boolean canManageLeaderboard(Long leaderboardId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (leaderboardId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageLeaderboard - leaderboardId: {}, userEmail: {}", leaderboardId, userEmail);
            return false;
        }

        try {
            // Verificar se é o admin especial (pode gerenciar qualquer leaderboard)
            if ("admin@admin.com".equals(userEmail)) {
                log.debug("Usuário {} é admin especial - pode gerenciar leaderboard {}", userEmail, leaderboardId);
                return true;
            }

            // Buscar o evento ao qual o leaderboard pertence
            Long eventoId = leaderboardRepository.findById(leaderboardId)
                    .map(leaderboard -> leaderboard.getEvento().getId())
                    .orElse(null);

            if (eventoId == null) {
                log.warn("Leaderboard {} não encontrado ou sem evento associado", leaderboardId);
                return false;
            }

            // Verificar se o usuário pode gerenciar o evento (e portanto o leaderboard)
            boolean canManage = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} gerenciar leaderboard {} (evento: {}, ownership: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", leaderboardId, eventoId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento do leaderboard {} para usuário {}: {}", 
                     leaderboardId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode gerenciar leaderboards para um evento específico
     * (utilizado para recálculo de pontuações por categoria)
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar leaderboards do evento
     */
    public boolean canManageLeaderboardsForEvento(Long eventoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (eventoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageLeaderboardsForEvento - eventoId: {}, userEmail: {}", eventoId, userEmail);
            return false;
        }

        try {
            // Delegar para EventoSecurityService - se pode gerenciar evento, pode gerenciar leaderboards
            boolean canManage = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} gerenciar leaderboards para evento {} (ownership: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", eventoId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento de leaderboards no evento {} para usuário {}: {}", 
                     eventoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode gerenciar leaderboards para uma categoria específica
     * (validação baseada no evento da categoria)
     * 
     * @param categoriaId ID da categoria
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar leaderboards da categoria
     */
    public boolean canManageLeaderboardsForCategoria(Long categoriaId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (categoriaId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageLeaderboardsForCategoria - categoriaId: {}, userEmail: {}", categoriaId, userEmail);
            return false;
        }

        try {
            // Verificar se é o admin especial (pode gerenciar qualquer leaderboard)
            if ("admin@admin.com".equals(userEmail)) {
                log.debug("Usuário {} é admin especial - pode gerenciar leaderboards da categoria {}", userEmail, categoriaId);
                return true;
            }

            // Buscar leaderboards da categoria para obter o evento
            Long eventoId = leaderboardRepository.findByCategoriaIdOrderByPosicaoWorkoutAsc(categoriaId)
                    .stream()
                    .findFirst()
                    .map(leaderboard -> leaderboard.getEvento().getId())
                    .orElse(null);

            if (eventoId == null) {
                log.warn("Nenhum leaderboard encontrado para categoria {} ou sem evento associado", categoriaId);
                return false;
            }

            // Verificar se o usuário pode gerenciar o evento (e portanto os leaderboards)
            boolean canManage = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} gerenciar leaderboards da categoria {} (evento: {}, ownership: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", categoriaId, eventoId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento de leaderboards da categoria {} para usuário {}: {}", 
                     categoriaId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o leaderboard existe
     * 
     * @param leaderboardId ID do leaderboard
     * @return true se o leaderboard existe
     */
    public boolean leaderboardExists(Long leaderboardId) {
        if (leaderboardId == null) {
            return false;
        }
        
        try {
            return leaderboardRepository.existsById(leaderboardId);
        } catch (Exception e) {
            log.error("Erro ao verificar existência do leaderboard {}: {}", leaderboardId, e.getMessage());
            return false;
        }
    }

    /**
     * Busca o ID do evento ao qual o leaderboard pertence
     * 
     * @param leaderboardId ID do leaderboard
     * @return ID do evento ou null se não encontrado
     */
    public Long getEventoIdByLeaderboard(Long leaderboardId) {
        if (leaderboardId == null) {
            return null;
        }

        try {
            return leaderboardRepository.findById(leaderboardId)
                    .map(leaderboard -> leaderboard.getEvento().getId())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar evento do leaderboard {}: {}", leaderboardId, e.getMessage());
            return null;
        }
    }
}