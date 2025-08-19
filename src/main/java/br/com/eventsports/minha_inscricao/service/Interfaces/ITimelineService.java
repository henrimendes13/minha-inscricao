package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.timeline.TimelineCreateDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineResponseDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineUpdateDTO;

public interface ITimelineService {

    TimelineResponseDTO findByEventoId(Long eventoId);
    
    TimelineSummaryDTO findSummaryByEventoId(Long eventoId);
    
    TimelineResponseDTO createForEvento(Long eventoId, TimelineCreateDTO timelineCreateDTO);
    
    TimelineResponseDTO updateByEventoId(Long eventoId, TimelineUpdateDTO timelineUpdateDTO);
    
    void deleteByEventoId(Long eventoId);
    
    boolean existsByEventoId(Long eventoId);
}