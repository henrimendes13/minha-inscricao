package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoriaSecurityService {

    private final CategoriaRepository categoriaRepository;
    private final EventoSecurityService eventoSecurityService;

    /**
     * Verifica se o usuário pode gerenciar a categoria específica
     * (baseado no evento ao qual a categoria pertence)
     * 
     * @param categoriaId ID da categoria
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode gerenciar a categoria
     */
    public boolean canManageCategoria(Long categoriaId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (categoriaId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canManageCategoria - categoriaId: {}, userEmail: {}", categoriaId, userEmail);
            return false;
        }

        try {
            // Verificar se é ADMIN (pode gerenciar qualquer categoria)
            boolean isAdmin = authorities.stream()
                    .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));

            if (isAdmin) {
                log.debug("Usuário {} é ADMIN - pode gerenciar categoria {}", userEmail, categoriaId);
                return true;
            }

            // Buscar o evento ao qual a categoria pertence
            Long eventoId = categoriaRepository.findById(categoriaId)
                    .map(categoria -> categoria.getEvento().getId())
                    .orElse(null);

            if (eventoId == null) {
                log.warn("Categoria {} não encontrada ou sem evento associado", categoriaId);
                return false;
            }

            // Verificar se o usuário pode gerenciar o evento (e portanto a categoria)
            boolean canManage = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} gerenciar categoria {} (evento: {}, ownership: {})", 
                     userEmail, canManage ? "PODE" : "NÃO PODE", categoriaId, eventoId, canManage);
            
            return canManage;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de gerenciamento da categoria {} para usuário {}: {}", 
                     categoriaId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o usuário pode criar categoria para um evento específico
     * 
     * @param eventoId ID do evento
     * @param userEmail Email do usuário autenticado
     * @param authorities Autoridades/roles do usuário
     * @return true se o usuário pode criar categoria para o evento
     */
    public boolean canCreateCategoriaForEvento(Long eventoId, String userEmail, Collection<? extends GrantedAuthority> authorities) {
        if (eventoId == null || userEmail == null) {
            log.warn("Parâmetros inválidos para canCreateCategoriaForEvento - eventoId: {}, userEmail: {}", eventoId, userEmail);
            return false;
        }

        try {
            // Delegar para EventoSecurityService - se pode gerenciar evento, pode criar categorias
            boolean canCreate = eventoSecurityService.canManageEvento(eventoId, userEmail, authorities);
            
            log.debug("Usuário {} {} criar categoria para evento {} (ownership: {})", 
                     userEmail, canCreate ? "PODE" : "NÃO PODE", eventoId, canCreate);
            
            return canCreate;

        } catch (Exception e) {
            log.error("Erro ao verificar permissão de criação de categoria no evento {} para usuário {}: {}", 
                     eventoId, userEmail, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se a categoria existe
     * 
     * @param categoriaId ID da categoria
     * @return true se a categoria existe
     */
    public boolean categoriaExists(Long categoriaId) {
        if (categoriaId == null) {
            return false;
        }
        
        try {
            return categoriaRepository.existsById(categoriaId);
        } catch (Exception e) {
            log.error("Erro ao verificar existência da categoria {}: {}", categoriaId, e.getMessage());
            return false;
        }
    }

    /**
     * Busca o ID do evento ao qual a categoria pertence
     * 
     * @param categoriaId ID da categoria
     * @return ID do evento ou null se não encontrado
     */
    public Long getEventoIdByCategoria(Long categoriaId) {
        if (categoriaId == null) {
            return null;
        }

        try {
            return categoriaRepository.findById(categoriaId)
                    .map(categoria -> categoria.getEvento().getId())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Erro ao buscar evento da categoria {}: {}", categoriaId, e.getMessage());
            return null;
        }
    }
}