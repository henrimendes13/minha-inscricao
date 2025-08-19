package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.timeline.TimelineCreateDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineResponseDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineUpdateDTO;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.TimelineEntity;
import br.com.eventsports.minha_inscricao.exception.EventoNotFoundException;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.repository.TimelineRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.ITimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TimelineService implements ITimelineService {

    private final TimelineRepository timelineRepository;
    private final EventoRepository eventoRepository;

    @Override
    @Cacheable(value = "timelines", key = "'evento-' + #eventoId")
    @Transactional(readOnly = true)
    public TimelineResponseDTO findByEventoId(Long eventoId) {
        TimelineEntity timeline = timelineRepository.findByEventoId(eventoId)
                .orElse(null);
        
        if (timeline == null) {
            // Se não existe timeline, retorna uma timeline vazia com os dados do evento
            EventoEntity evento = eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new EventoNotFoundException("Evento não encontrado com ID: " + eventoId));
            return createEmptyTimelineResponse(evento);
        }
        
        return convertToResponseDTO(timeline);
    }

    @Override
    @Cacheable(value = "timelines", key = "'summary-evento-' + #eventoId")
    @Transactional(readOnly = true)
    public TimelineSummaryDTO findSummaryByEventoId(Long eventoId) {
        TimelineEntity timeline = timelineRepository.findByEventoId(eventoId)
                .orElse(null);
        
        if (timeline == null) {
            EventoEntity evento = eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new EventoNotFoundException("Evento não encontrado com ID: " + eventoId));
            return createEmptyTimelineSummary(evento);
        }
        
        return convertToSummaryDTO(timeline);
    }

    @Override
    @CachePut(value = "timelines", key = "'evento-' + #eventoId")
    @CacheEvict(value = "timelines", key = "'summary-evento-' + #eventoId")
    public TimelineResponseDTO createForEvento(Long eventoId, TimelineCreateDTO timelineCreateDTO) {
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new EventoNotFoundException("Evento não encontrado com ID: " + eventoId));
        
        // Verifica se já existe timeline para este evento
        if (timelineRepository.existsByEventoId(eventoId)) {
            throw new RuntimeException("Já existe uma timeline para este evento");
        }
        
        TimelineEntity timeline = convertCreateDTOToEntity(timelineCreateDTO, evento);
        TimelineEntity savedTimeline = timelineRepository.save(timeline);
        return convertToResponseDTO(savedTimeline);
    }

    @Override
    @CachePut(value = "timelines", key = "'evento-' + #eventoId")
    @CacheEvict(value = "timelines", key = "'summary-evento-' + #eventoId")
    public TimelineResponseDTO updateByEventoId(Long eventoId, TimelineUpdateDTO timelineUpdateDTO) {
        TimelineEntity timeline = timelineRepository.findByEventoId(eventoId)
                .orElseThrow(() -> new RuntimeException("Timeline não encontrada para o evento ID: " + eventoId));
        
        updateTimelineFromDTO(timeline, timelineUpdateDTO);
        TimelineEntity updatedTimeline = timelineRepository.save(timeline);
        return convertToResponseDTO(updatedTimeline);
    }

    @Override
    @CacheEvict(value = "timelines", allEntries = true)
    public void deleteByEventoId(Long eventoId) {
        if (!timelineRepository.existsByEventoId(eventoId)) {
            throw new RuntimeException("Timeline não encontrada para o evento ID: " + eventoId);
        }
        timelineRepository.deleteByEventoId(eventoId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEventoId(Long eventoId) {
        return timelineRepository.existsByEventoId(eventoId);
    }

    // Métodos privados de conversão
    private TimelineResponseDTO convertToResponseDTO(TimelineEntity timeline) {
        return TimelineResponseDTO.builder()
                .id(timeline.getId())
                .eventoId(timeline.getEvento().getId())
                .nomeEvento(timeline.getNomeEvento())
                .descricaoDiaUm(timeline.getDescricaoDiaUm())
                .descricaoDiaDois(timeline.getDescricaoDiaDois())
                .descricaoDiaTres(timeline.getDescricaoDiaTres())
                .descricaoDiaQuatro(timeline.getDescricaoDiaQuatro())
                .descricaoCompleta(timeline.getDescricaoCompleta())
                .totalDiasComDescricao(timeline.getTotalDiasComDescricao())
                .vazia(timeline.isVazia())
                .temDescricaoDiaUm(timeline.temDescricaoDiaUm())
                .temDescricaoDiaDois(timeline.temDescricaoDiaDois())
                .temDescricaoDiaTres(timeline.temDescricaoDiaTres())
                .temDescricaoDiaQuatro(timeline.temDescricaoDiaQuatro())
                .createdAt(timeline.getCreatedAt())
                .updatedAt(timeline.getUpdatedAt())
                .build();
    }

    private TimelineSummaryDTO convertToSummaryDTO(TimelineEntity timeline) {
        return TimelineSummaryDTO.builder()
                .id(timeline.getId())
                .eventoId(timeline.getEvento().getId())
                .nomeEvento(timeline.getNomeEvento())
                .descricaoCompleta(timeline.getDescricaoCompleta())
                .totalDiasComDescricao(timeline.getTotalDiasComDescricao())
                .vazia(timeline.isVazia())
                .temDescricaoDiaUm(timeline.temDescricaoDiaUm())
                .temDescricaoDiaDois(timeline.temDescricaoDiaDois())
                .temDescricaoDiaTres(timeline.temDescricaoDiaTres())
                .temDescricaoDiaQuatro(timeline.temDescricaoDiaQuatro())
                .build();
    }

    private TimelineEntity convertCreateDTOToEntity(TimelineCreateDTO createDTO, EventoEntity evento) {
        return TimelineEntity.builder()
                .evento(evento)
                .descricaoDiaUm(createDTO.getDescricaoDiaUm())
                .descricaoDiaDois(createDTO.getDescricaoDiaDois())
                .descricaoDiaTres(createDTO.getDescricaoDiaTres())
                .descricaoDiaQuatro(createDTO.getDescricaoDiaQuatro())
                .build();
    }

    private void updateTimelineFromDTO(TimelineEntity timeline, TimelineUpdateDTO updateDTO) {
        timeline.setDescricaoDiaUm(updateDTO.getDescricaoDiaUm());
        timeline.setDescricaoDiaDois(updateDTO.getDescricaoDiaDois());
        timeline.setDescricaoDiaTres(updateDTO.getDescricaoDiaTres());
        timeline.setDescricaoDiaQuatro(updateDTO.getDescricaoDiaQuatro());
    }

    private TimelineResponseDTO createEmptyTimelineResponse(EventoEntity evento) {
        return TimelineResponseDTO.builder()
                .id(null)
                .eventoId(evento.getId())
                .nomeEvento(evento.getNome())
                .descricaoDiaUm(null)
                .descricaoDiaDois(null)
                .descricaoDiaTres(null)
                .descricaoDiaQuatro(null)
                .descricaoCompleta("")
                .totalDiasComDescricao(0)
                .vazia(true)
                .temDescricaoDiaUm(false)
                .temDescricaoDiaDois(false)
                .temDescricaoDiaTres(false)
                .temDescricaoDiaQuatro(false)
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    private TimelineSummaryDTO createEmptyTimelineSummary(EventoEntity evento) {
        return TimelineSummaryDTO.builder()
                .id(null)
                .eventoId(evento.getId())
                .nomeEvento(evento.getNome())
                .descricaoCompleta("")
                .totalDiasComDescricao(0)
                .vazia(true)
                .temDescricaoDiaUm(false)
                .temDescricaoDiaDois(false)
                .temDescricaoDiaTres(false)
                .temDescricaoDiaQuatro(false)
                .build();
    }
}