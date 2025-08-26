package br.com.eventsports.minha_inscricao.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.eventsports.minha_inscricao.dto.evento.EventoCreateDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoResponseDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoUpdateDTO;
import br.com.eventsports.minha_inscricao.dto.evento.StatusChangeDTO;
import br.com.eventsports.minha_inscricao.exception.EventoNotFoundException;
import br.com.eventsports.minha_inscricao.exception.InvalidDateRangeException;
import br.com.eventsports.minha_inscricao.service.Interfaces.IEventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EventoController {

    private final IEventoService eventoService;

    @GetMapping
    public ResponseEntity<List<EventoSummaryDTO>> getAllEventos() {
        List<EventoSummaryDTO> eventos = eventoService.findAll();
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> getEventoById(@PathVariable Long id) {
        EventoResponseDTO evento = eventoService.findById(id);
        return ResponseEntity.ok(evento);
    }

    @PostMapping
    public ResponseEntity<EventoResponseDTO> createEvento(@Valid @RequestBody EventoCreateDTO eventoCreateDTO) {
        EventoResponseDTO createdEvento = eventoService.save(eventoCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoResponseDTO> updateEvento(@PathVariable Long id,
            @Valid @RequestBody EventoUpdateDTO eventoUpdateDTO) {
        EventoResponseDTO updatedEvento = eventoService.update(id, eventoUpdateDTO);
        return ResponseEntity.ok(updatedEvento);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEvento(@PathVariable Long id) {
        eventoService.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Evento deletado com sucesso"));
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
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fim) {
        // Convert LocalDate to LocalDateTime (start of day and end of day)
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime fimDateTime = fim.atTime(23, 59, 59);
        
        List<EventoSummaryDTO> eventos = eventoService.findEventosByDataBetween(inicioDateTime, fimDateTime);
        return ResponseEntity.ok(eventos);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventoResponseDTO> changeEventoStatus(@PathVariable Long id, 
            @Valid @RequestBody StatusChangeDTO statusChangeDTO) {
        EventoResponseDTO updatedEvento = eventoService.changeStatus(id, statusChangeDTO);
        return ResponseEntity.ok(updatedEvento);
    }

    // Exception Handler for this controller
    @ExceptionHandler(EventoNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEventoNotFoundException(EventoNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Evento não encontrado", "message", e.getMessage()));
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDateRangeException(InvalidDateRangeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Dados inválidos", "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor", "message", e.getMessage()));
    }
}
