package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.timeline.TimelineCreateDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineResponseDTO;
import br.com.eventsports.minha_inscricao.dto.timeline.TimelineUpdateDTO;
import br.com.eventsports.minha_inscricao.exception.EventoNotFoundException;
import br.com.eventsports.minha_inscricao.service.Interfaces.ITimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eventos/{eventoId}/timeline")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Timeline", description = "Gerenciamento de cronogramas de eventos")
public class TimelineController {

    private final ITimelineService timelineService;

    @GetMapping
    @Operation(summary = "Buscar timeline do evento", 
               description = "Retorna a timeline de um evento específico. Se não existir, retorna uma timeline vazia.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Timeline encontrada ou timeline vazia retornada"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public ResponseEntity<TimelineResponseDTO> getTimelineByEventoId(
            @Parameter(description = "ID do evento", required = true, example = "1")
            @PathVariable Long eventoId) {
        try {
            TimelineResponseDTO timeline = timelineService.findByEventoId(eventoId);
            return ResponseEntity.ok(timeline);
        } catch (EventoNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar timeline para evento", 
               description = "Cria uma nova timeline para um evento específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Timeline criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou timeline já existe para o evento"),
        @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public ResponseEntity<TimelineResponseDTO> createTimeline(
            @Parameter(description = "ID do evento", required = true, example = "1")
            @PathVariable Long eventoId,
            @Valid @RequestBody TimelineCreateDTO timelineCreateDTO) {
        try {
            TimelineResponseDTO createdTimeline = timelineService.createForEvento(eventoId, timelineCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTimeline);
        } catch (EventoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    @Operation(summary = "Atualizar timeline do evento", 
               description = "Atualiza a timeline existente de um evento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Timeline atualizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Timeline ou evento não encontrado")
    })
    public ResponseEntity<TimelineResponseDTO> updateTimeline(
            @Parameter(description = "ID do evento", required = true, example = "1")
            @PathVariable Long eventoId,
            @Valid @RequestBody TimelineUpdateDTO timelineUpdateDTO) {
        try {
            TimelineResponseDTO updatedTimeline = timelineService.updateByEventoId(eventoId, timelineUpdateDTO);
            return ResponseEntity.ok(updatedTimeline);
        } catch (EventoNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping
    @Operation(summary = "Excluir timeline do evento", 
               description = "Remove a timeline de um evento específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Timeline excluída com sucesso"),
        @ApiResponse(responseCode = "404", description = "Timeline não encontrada")
    })
    public ResponseEntity<Void> deleteTimeline(
            @Parameter(description = "ID do evento", required = true, example = "1")
            @PathVariable Long eventoId) {
        try {
            timelineService.deleteByEventoId(eventoId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/exists")
    @Operation(summary = "Verificar se timeline existe", 
               description = "Verifica se existe uma timeline para o evento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verificação realizada com sucesso")
    })
    public ResponseEntity<Boolean> checkTimelineExists(
            @Parameter(description = "ID do evento", required = true, example = "1")
            @PathVariable Long eventoId) {
        boolean exists = timelineService.existsByEventoId(eventoId);
        return ResponseEntity.ok(exists);
    }
}