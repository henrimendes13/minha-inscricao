package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkoutSecurityService {

    private final WorkoutRepository workoutRepository;
    private final EventoSecurityService eventoSecurityService;

    /**
     * Verifica se o usuário pode gerenciar o workout específico
     * (baseado no evento ao qual o workout pertence)
     * 
     * @param workoutId ID do workout
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar o workout
     */
    public boolean canManageWorkout(Long workoutId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (workoutId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageWorkout - workoutId: {}, userEmail: {}", workoutId, userEmail);
            return false;
        }

        try {
            // Verificar se é o admin especial (pode gerenciar qualquer workout)
            if ("admin@admin.com".equals(userEmail)) {
                log.debug("Usuário {} é admin especial - pode gerenciar workout {}", userEmail, workoutId);
                return true;
            }

            // Buscar o evento ao qual o workout pertence
            Long eventoId = workoutRepository.findById(workoutId)
                    .map(workout -> workout.getEvento().getId())
                    .orElse(null);

            if (eventoId == null) {
                log.warn("Workout {} não encontrado ou sem evento associado", workoutId);
                return false;
            }

            // Verificar se o usuário pode gerenciar o evento (e portanto o workout)
            boolean canManage = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} gerenciar workout {} (evento: {}, ownership: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", workoutId, eventoId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento do workout {} para usuário {}: {}", 
                     workoutId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode criar workout para um evento específico
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode criar workout para o evento
     */
    public boolean canCreateWorkoutForEvento(Long eventoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (eventoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canCreateWorkoutForEvento - eventoId: {}, userEmail: {}", eventoId, userEmail);
            return false;
        }

        try {
            // Delegar para EventoSecurityService - se pode gerenciar evento, pode criar workouts
            boolean canCreate = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} criar workout para evento {} (ownership: {})", 
                     userEmail, canCreate ? "PODE" : "NÃO PODE", eventoId, canCreate);
            
            return canCreate;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de criação de workout no evento {} para usuário {}: {}", 
                     eventoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o workout existe
     * 
     * @param workoutId ID do workout
     * @return true se o workout existe
     */
    public boolean workoutExists(Long workoutId) {
        if (workoutId == null) {
            return false;
        }
        
        try {
            return workoutRepository.existsById(workoutId);
        } catch (Exception e) {
            log.error("Erro ao verificar existência do workout {}: {}", workoutId, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode gerenciar resultados de um workout específico
     * (mesmo critério que gerenciar o workout - apenas criador do evento ou admin)
     * 
     * @param workoutId ID do workout
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar os resultados do workout
     */
    public boolean canManageWorkoutResults(Long workoutId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (workoutId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageWorkoutResults - workoutId: {}, userEmail: {}", workoutId, userEmail);
            return false;
        }

        try {
            // Reutilizar a mesma lógica de canManageWorkout
            // Se pode gerenciar o workout, pode gerenciar seus resultados
            boolean canManage = canManageWorkout(workoutId, userEmail, authorities);
            
            log.debug("Usuário {} {} gerenciar resultados do workout {} (permissão: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", workoutId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento de resultados do workout {} para usuário {}: {}", 
                     workoutId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Busca o ID do evento ao qual o workout pertence
     * 
     * @param workoutId ID do workout
     * @return ID do evento ou null se não encontrado
     */
    public Long getEventoIdByWorkout(Long workoutId) {
        if (workoutId == null) {
            return null;
        }

        try {
            return workoutRepository.findById(workoutId)
                    .map(workout -> workout.getEvento().getId())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar evento do workout {}: {}", workoutId, e.getMessage());
            return null;
        }
    }
}