package br.com.eventsports.minha_inscricao.security;

import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.InscricaoEntity;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.repository.InscricaoRepository;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnershipValidator {

    private final EventoRepository eventoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;

    public boolean isEventoOwner(Authentication authentication, Long eventoId) {
        if (authentication == null || eventoId == null) {
            log.warn("Authentication ou eventoId null na validação de ownership");
            return false;
        }

        String userEmail = authentication.getName();
        log.debug("Validando ownership do evento {} para usuário {}", eventoId, userEmail);

        try {
            // Buscar o usuário logado
            var usuario = usuarioRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (usuario == null) {
                log.warn("Usuário não encontrado para validação de ownership: {}", userEmail);
                return false;
            }

            // Buscar o evento
            EventoEntity evento = eventoRepository.findById(eventoId)
                    .orElse(null);
            
            if (evento == null) {
                log.warn("Evento não encontrado para validação de ownership: {}", eventoId);
                return false;
            }

            // Verificar se o evento tem organizador e se pertence ao usuário logado
            if (evento.getOrganizador() == null) {
                log.warn("Evento {} sem organizador definido", eventoId);
                return false;
            }

            boolean isOwner = evento.getOrganizador().getId().equals(usuario.getId());
            log.debug("Resultado da validação de ownership: usuário {} {} dono do evento {}", 
                    userEmail, isOwner ? "é" : "não é", eventoId);
            
            return isOwner;

        } catch (Exception e) {
            log.error("Erro ao validar ownership do evento {} para usuário {}", eventoId, userEmail, e);
            return false;
        }
    }

    public boolean isInscricaoOwner(Authentication authentication, Long inscricaoId) {
        if (authentication == null || inscricaoId == null) {
            log.warn("Authentication ou inscricaoId null na validação de ownership");
            return false;
        }

        String userEmail = authentication.getName();
        log.debug("Validando ownership da inscrição {} para usuário {}", inscricaoId, userEmail);

        try {
            // Buscar o usuário logado
            var usuario = usuarioRepository.findByEmail(userEmail)
                    .orElse(null);
            
            if (usuario == null) {
                log.warn("Usuário não encontrado para validação de ownership: {}", userEmail);
                return false;
            }

            // Buscar a inscrição
            InscricaoEntity inscricao = inscricaoRepository.findById(inscricaoId)
                    .orElse(null);
            
            if (inscricao == null) {
                log.warn("Inscrição não encontrada para validação de ownership: {}", inscricaoId);
                return false;
            }

            // Para inscrições, consideramos que o usuário é o dono se:
            // 1. É um atleta e o email dele está entre os atletas da inscrição
            // 2. É um organizador do evento da inscrição
            
            // Verificar se é organizador do evento
            if (inscricao.getEvento() != null && 
                inscricao.getEvento().getOrganizador() != null &&
                inscricao.getEvento().getOrganizador().getId().equals(usuario.getId())) {
                log.debug("Usuário {} é organizador do evento da inscrição {}", userEmail, inscricaoId);
                return true;
            }
            
            // Verificar se é o atleta da inscrição
            if (inscricao.getAtleta() != null && inscricao.getAtleta().getId().equals(usuario.getId())) {
                log.debug("Usuário {} é o atleta da inscrição {}", userEmail, inscricaoId);
                return true;
            }

            // Para atletas, por enquanto permitir apenas visualização pública
            // ou poderia verificar se o email do usuário está presente nos dados dos atletas
            // Como AtletaEntity não tem email, vamos considerar que atletas podem ver todas as inscrições
            // mas só podem modificar as próprias (implementação futura)
            
            log.debug("Usuário {} não tem ownership da inscrição {}", userEmail, inscricaoId);
            return false;

        } catch (Exception e) {
            log.error("Erro ao validar ownership da inscrição {} para usuário {}", inscricaoId, userEmail, e);
            return false;
        }
    }

    public String getCurrentUserEmail(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}