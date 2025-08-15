package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.evento.*;
import br.com.eventsports.minha_inscricao.exception.EventoNotFoundException;
import br.com.eventsports.minha_inscricao.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    public ResponseEntity<List<EventoSummaryDTO>> getAllEventos() {
        List<EventoSummaryDTO> eventos = eventoService.findAll();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> getEventoById(@PathVariable Long id) {
        try {
            EventoResponseDTO evento = eventoService.findById(id);
            return ResponseEntity.ok(evento);
        } catch (EventoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<EventoResponseDTO> createEvento(@Valid @RequestBody EventoCreateDTO eventoCreateDTO) {
        EventoResponseDTO createdEvento = eventoService.save(eventoCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> updateEvento(@PathVariable Long id, @Valid @RequestBody EventoUpdateDTO eventoUpdateDTO) {
        try {
            EventoResponseDTO updatedEvento = eventoService.update(id, eventoUpdateDTO);
            return ResponseEntity.ok(updatedEvento);
        } catch (EventoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEvento(@PathVariable Long id) {
        try {
            eventoService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Evento deletado com sucesso"));
        } catch (EventoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventoSummaryDTO>> searchEventosByNome(@RequestParam String nome) {
        List<EventoSummaryDTO> eventos = eventoService.findByNome(nome);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventoSummaryDTO>> getUpcomingEventos() {
        List<EventoSummaryDTO> eventos = eventoService.findEventosUpcoming();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/past")
    public ResponseEntity<List<EventoSummaryDTO>> getPastEventos() {
        List<EventoSummaryDTO> eventos = eventoService.findEventosPast();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/between")
    public ResponseEntity<List<EventoSummaryDTO>> getEventosBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        List<EventoSummaryDTO> eventos = eventoService.findEventosByDataBetween(inicio, fim);
        return ResponseEntity.ok(eventos);
    }

    // Exception Handler for this controller
    @ExceptionHandler(EventoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEventoNotFoundException(EventoNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Evento não encontrado", "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor", "message", e.getMessage()));
    }
}
