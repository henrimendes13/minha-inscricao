package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.evento.*;
import br.com.eventsports.minha_inscricao.exception.EventoNotFoundException;
import br.com.eventsports.minha_inscricao.exception.InvalidDateRangeException;
import br.com.eventsports.minha_inscricao.security.OwnershipValidator;
import br.com.eventsports.minha_inscricao.service.Interfaces.IEventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EventoController {

    private final IEventoService eventoService;
    private final OwnershipValidator ownershipValidator;

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
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<EventoResponseDTO> createEvento(@Valid @RequestBody EventoCreateDTO eventoCreateDTO,
                                                         Authentication authentication) {
        try {
            EventoResponseDTO createdEvento = eventoService.save(eventoCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEvento);
        } catch (InvalidDateRangeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<EventoResponseDTO> updateEvento(@PathVariable Long id, 
                                                         @Valid @RequestBody EventoUpdateDTO eventoUpdateDTO,
                                                         Authentication authentication) {
        try {
            // Verificar ownership do evento
            if (!ownershipValidator.isEventoOwner(authentication, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            EventoResponseDTO updatedEvento = eventoService.update(id, eventoUpdateDTO);
            return ResponseEntity.ok(updatedEvento);
        } catch (EventoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InvalidDateRangeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZADOR')")
    public ResponseEntity<Map<String, String>> deleteEvento(@PathVariable Long id,
                                                           Authentication authentication) {
        try {
            // Verificar ownership do evento
            if (!ownershipValidator.isEventoOwner(authentication, id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
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
