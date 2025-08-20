package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.evento.*;

import java.time.LocalDateTime;
import java.util.List;

public interface IEventoService {
    
    EventoResponseDTO findById(Long id);
    
    List<EventoSummaryDTO> findAll();
    
    EventoResponseDTO save(EventoCreateDTO eventoCreateDTO);
    
    EventoResponseDTO update(Long id, EventoUpdateDTO eventoUpdateDTO);
    
    void deleteById(Long id);
    
    List<EventoSummaryDTO> findByNome(String nome);
    
    List<EventoSummaryDTO> findEventosUpcoming();
    
    List<EventoSummaryDTO> findEventosPast();
    
    List<EventoSummaryDTO> findEventosByDataBetween(LocalDateTime inicio, LocalDateTime fim);
    
    /**
     * Valida se o usuário logado é o organizador do evento.
     * @param eventoId ID do evento
     * @param usuarioId ID do usuário logado
     * @throws UnauthorizedException se o usuário não for o organizador do evento
     */
    void validateEventoOwnership(Long eventoId, Long usuarioId);
    
    /**
     * Verifica se o usuário logado é o organizador do evento.
     * @param eventoId ID do evento
     * @param usuarioId ID do usuário logado
     * @return true se o usuário for o organizador, false caso contrário
     */
    boolean isEventoOwner(Long eventoId, Long usuarioId);
}
