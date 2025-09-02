package br.com.eventsports.minha_inscricao.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.eventsports.minha_inscricao.dto.timeline.TimelineCreateDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineResponseDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineUpdateDTO;
import br.com.eventsports.minha_inscricao.exception.EventoNotFoundException;
import br.com.eventsports.minha_inscricao.service.Interfaces.ITimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/eventos/{eventoId}/timeline")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TimelineController {

    private final ITimelineService timelineService;

    @GetMapping
    public ResponseEntity<TimelineResponseDTO> getTimelineByEventoId(@PathVariable Long eventoId) {
        TimelineResponseDTO timeline = timelineService.findByEventoId(eventoId);
        return ResponseEntity.ok(timeline);
    }

    @PreAuthorize("@timelineSecurityService.canCreateTimelineForEvento(#eventoId, authentication.name, authentication.authorities)")
    @PostMapping
    public ResponseEntity<TimelineResponseDTO> createTimeline(@PathVariable Long eventoId,
            @Valid @RequestBody TimelineCreateDTO timelineCreateDTO) {
        TimelineResponseDTO createdTimeline = timelineService.createForEvento(eventoId, timelineCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTimeline);
    }

    @PreAuthorize("@timelineSecurityService.canManageTimelineForEvento(#eventoId, authentication.name, authentication.authorities)")
    @PutMapping
    public ResponseEntity<TimelineResponseDTO> updateTimeline(@PathVariable Long eventoId,
            @Valid @RequestBody TimelineUpdateDTO timelineUpdateDTO) {
        TimelineResponseDTO updatedTimeline = timelineService.updateByEventoId(eventoId, timelineUpdateDTO);
        return ResponseEntity.ok(updatedTimeline);
    }

    @PreAuthorize("@timelineSecurityService.canManageTimelineForEvento(#eventoId, authentication.name, authentication.authorities)")
    @DeleteMapping
    public ResponseEntity<Void> deleteTimeline(@PathVariable Long eventoId) {
        timelineService.deleteByEventoId(eventoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> checkTimelineExists(@PathVariable Long eventoId) {
        boolean exists = timelineService.existsByEventoId(eventoId);
        return ResponseEntity.ok(exists);
    }

    @ExceptionHandler(EventoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEventoNotFoundException(EventoNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Evento não encontrado", "message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if (e.getMessage().contains("não encontrada")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Timeline não encontrada", "message", e.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Erro na operação", "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor", "message", e.getMessage()));
    }
}