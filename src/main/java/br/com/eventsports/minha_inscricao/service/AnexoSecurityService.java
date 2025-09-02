package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.repository.AnexoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnexoSecurityService {

    private final AnexoRepository anexoRepository;
    private final EventoSecurityService eventoSecurityService;

    /**
     * Verifica se o usuário pode gerenciar o anexo específico
     * (baseado no evento ao qual o anexo pertence)
     * 
     * @param anexoId ID do anexo
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar o anexo
     */
    public boolean canManageAnexo(Long anexoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (anexoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageAnexo - anexoId: {}, userEmail: {}", anexoId, userEmail);
            return false;
        }

        try {
            // Verificar se é o admin especial (pode gerenciar qualquer anexo)
            if ("admin@admin.com".equals(userEmail)) {
                log.debug("Usuário {} é admin especial - pode gerenciar anexo {}", userEmail, anexoId);
                return true;
            }

            // Buscar o evento ao qual o anexo pertence
            Long eventoId = anexoRepository.findById(anexoId)
                    .map(anexo -> anexo.getEvento().getId())
                    .orElse(null);

            if (eventoId == null) {
                log.warn("Anexo {} não encontrado ou sem evento associado", anexoId);
                return false;
            }

            // Verificar se o usuário pode gerenciar o evento (e portanto o anexo)
            boolean canManage = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} gerenciar anexo {} (evento: {}, ownership: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", anexoId, eventoId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento do anexo {} para usuário {}: {}", 
                     anexoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode criar anexo para um evento específico
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode criar anexo para o evento
     */
    public boolean canCreateAnexoForEvento(Long eventoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (eventoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canCreateAnexoForEvento - eventoId: {}, userEmail: {}", eventoId, userEmail);
            return false;
        }

        try {
            // Delegar para EventoSecurityService - se pode gerenciar evento, pode criar anexos
            boolean canCreate = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} criar anexo para evento {} (ownership: {})", 
                     userEmail, canCreate ? "PODE" : "NÃO PODE", eventoId, canCreate);
            
            return canCreate;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de criação de anexo no evento {} para usuário {}: {}", 
                     eventoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o anexo existe
     * 
     * @param anexoId ID do anexo
     * @return true se o anexo existe
     */
    public boolean anexoExists(Long anexoId) {
        if (anexoId == null) {
            return false;
        }
        
        try {
            return anexoRepository.existsById(anexoId);
        } catch (Exception e) {
            log.error("Erro ao verificar existência do anexo {}: {}", anexoId, e.getMessage());
            return false;
        }
    }

    /**
     * Busca o ID do evento ao qual o anexo pertence
     * 
     * @param anexoId ID do anexo
     * @return ID do evento ou null se não encontrado
     */
    public Long getEventoIdByAnexo(Long anexoId) {
        if (anexoId == null) {
            return null;
        }

        try {
            return anexoRepository.findById(anexoId)
                    .map(anexo -> anexo.getEvento().getId())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar evento do anexo {}: {}", anexoId, e.getMessage());
            return null;
        }
    }
}