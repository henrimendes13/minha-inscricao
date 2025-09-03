package br.com.eventsports.minha_inscricao.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.eventsports.minha_inscricao.dto.workout.*;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardResponseDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardSummaryDTO;
import br.com.eventsports.minha_inscricao.service.Interfaces.IWorkoutService;
import br.com.eventsports.minha_inscricao.service.WorkoutResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WorkoutController {

    private final IWorkoutService workoutService;
    private final WorkoutResultService workoutResultService;

    @GetMapping
    public ResponseEntity<List<WorkoutSummaryDTO>> getAllWorkouts() {
        List<WorkoutSummaryDTO> workouts = workoutService.findAll();
        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkoutResponseDTO> getWorkoutById(@PathVariable Long id) {
        WorkoutResponseDTO workout = workoutService.findById(id);
        return ResponseEntity.ok(workout);
    }

    @PreAuthorize("@workoutSecurityService.canCreateWorkoutForEvento(#workoutCreateDTO.eventoId, authentication.name, authentication.authorities)")
    @PostMapping
    public ResponseEntity<WorkoutResponseDTO> createWorkout(@Valid @RequestBody WorkoutCreateDTO workoutCreateDTO) {
        WorkoutResponseDTO createdWorkout = workoutService.save(workoutCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkout);
    }

    @PreAuthorize("@workoutSecurityService.canManageWorkout(#id, authentication.name, authentication.authorities)")
    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponseDTO> updateWorkout(@PathVariable Long id,
            @Valid @RequestBody WorkoutUpdateDTO workoutUpdateDTO) {
        WorkoutResponseDTO updatedWorkout = workoutService.update(id, workoutUpdateDTO);
        return ResponseEntity.ok(updatedWorkout);
    }

    @PreAuthorize("@workoutSecurityService.canManageWorkout(#id, authentication.name, authentication.authorities)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkout(@PathVariable Long id) {
        workoutService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<WorkoutSummaryDTO>> getWorkoutsByEvento(@PathVariable Long eventoId,
            @RequestParam(defaultValue = "false") boolean apenasAtivos) {
        List<WorkoutSummaryDTO> workouts = workoutService.findByEventoIdAndAtivo(eventoId, apenasAtivos);
        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<WorkoutSummaryDTO>> getWorkoutsByCategoria(@PathVariable Long categoriaId) {
        List<WorkoutSummaryDTO> workouts = workoutService.findByCategoriaId(categoriaId);
        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<WorkoutSummaryDTO>> searchWorkoutsByNome(@RequestParam String nome) {
        List<WorkoutSummaryDTO> workouts = workoutService.findByNome(nome);
        return ResponseEntity.ok(workouts);
    }

    @PreAuthorize("@workoutSecurityService.canManageWorkout(#id, authentication.name, authentication.authorities)")
    @PatchMapping("/{id}/ativar")
    public ResponseEntity<WorkoutResponseDTO> ativarWorkout(@PathVariable Long id) {
        WorkoutResponseDTO workout = workoutService.ativar(id);
        return ResponseEntity.ok(workout);
    }

    @PreAuthorize("@workoutSecurityService.canManageWorkout(#id, authentication.name, authentication.authorities)")
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<WorkoutResponseDTO> desativarWorkout(@PathVariable Long id) {
        WorkoutResponseDTO workout = workoutService.desativar(id);
        return ResponseEntity.ok(workout);
    }


    @DeleteMapping("/{workoutId}/categorias/{categoriaId}")
    public ResponseEntity<WorkoutResponseDTO> removerCategoria(@PathVariable Long workoutId,
            @PathVariable Long categoriaId) {
        WorkoutResponseDTO workout = workoutService.removerCategoria(workoutId, categoriaId);
        return ResponseEntity.ok(workout);
    }

    // ===============================================
    // ENDPOINTS PARA GERENCIAMENTO DE RESULTADOS
    // ===============================================


    /**
     * Registra resultado individual para um participante específico
     */
    @PreAuthorize("@workoutSecurityService.canManageWorkoutResults(#workoutId, authentication.name, authentication.authorities)")
    @PostMapping("/{workoutId}/resultados")
    public ResponseEntity<LeaderboardResponseDTO> registrarResultado(
            @PathVariable Long workoutId,
            @Valid @RequestBody WorkoutResultCreateDTO dto) {
        LeaderboardResponseDTO resultado = workoutResultService.registrarResultado(
                dto.getEventoId(), workoutId, dto.getCategoriaId(), dto.getParticipanteId(), 
                dto.getIsEquipe(), dto.getResultadoValor(), dto.getFinalizado());
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /**
     * Atualiza resultado de uma equipe específica
     */
    @PreAuthorize("@workoutSecurityService.canManageWorkoutResults(#workoutId, authentication.name, authentication.authorities)")
    @PutMapping("/{workoutId}/resultados/equipe/{equipeId}")
    public ResponseEntity<LeaderboardResponseDTO> atualizarResultadoEquipe(
            @PathVariable Long workoutId,
            @PathVariable Long equipeId,
            @Valid @RequestBody WorkoutResultUpdateDTO dto) {
        LeaderboardResponseDTO resultado = workoutResultService.atualizarResultadoEquipe(
                workoutId, equipeId, dto.getResultadoValor(), dto.getFinalizado());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Atualiza resultado de um atleta específico
     */
    @PreAuthorize("@workoutSecurityService.canManageWorkoutResults(#workoutId, authentication.name, authentication.authorities)")
    @PutMapping("/{workoutId}/resultados/atleta/{atletaId}")
    public ResponseEntity<LeaderboardResponseDTO> atualizarResultadoAtleta(
            @PathVariable Long workoutId,
            @PathVariable Long atletaId,
            @Valid @RequestBody WorkoutResultUpdateDTO dto) {
        LeaderboardResponseDTO resultado = workoutResultService.atualizarResultadoAtleta(
                workoutId, atletaId, dto.getResultadoValor(), dto.getFinalizado());
        return ResponseEntity.ok(resultado);
    }

    /**
     * Busca todos os resultados de um workout em uma categoria
     */
    @GetMapping("/{workoutId}/resultados")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getResultadosWorkout(
            @PathVariable Long workoutId,
            @RequestParam Long categoriaId) {
        List<LeaderboardSummaryDTO> resultados = workoutResultService
                .getResultadosWorkout(categoriaId, workoutId);
        return ResponseEntity.ok(resultados);
    }


    /**
     * Busca status e estatísticas de um workout
     */
    @GetMapping("/{workoutId}/status")
    public ResponseEntity<WorkoutResultStatusDTO> getStatusWorkout(
            @PathVariable Long workoutId,
            @RequestParam Long categoriaId) {
        
        WorkoutResponseDTO workout = workoutService.findById(workoutId);
        long totalParticipantes = workoutResultService.contarTotalParticipantes(categoriaId, workoutId);
        long participantesFinalizados = workoutResultService.contarParticipantesFinalizados(categoriaId, workoutId);
        List<String> participantesPendentes = workoutResultService.getParticipantesPendentes(categoriaId, workoutId);
        
        double porcentagem = totalParticipantes > 0 ? 
                (double) participantesFinalizados / totalParticipantes * 100 : 0.0;
        
        WorkoutResultStatusDTO status = WorkoutResultStatusDTO.builder()
                .workoutId(workoutId)
                .nomeWorkout(workout.getNome())
                .categoriaId(categoriaId)
                .totalParticipantes(totalParticipantes)
                .participantesFinalizados(participantesFinalizados)
                .porcentagemFinalizados(porcentagem)
                .workoutFinalizado(workoutResultService.isWorkoutFinalizado(categoriaId, workoutId))
                .participantesPendentes(participantesPendentes)
                .build();
        
        return ResponseEntity.ok(status);
    }

    /**
     * Remove resultado de um participante
     */
    @PreAuthorize("@workoutSecurityService.canManageWorkoutResults(#workoutId, authentication.name, authentication.authorities)")
    @DeleteMapping("/{workoutId}/resultados/equipe/{equipeId}")
    public ResponseEntity<Void> removerResultadoEquipe(
            @PathVariable Long workoutId,
            @PathVariable Long equipeId) {
        workoutResultService.removerResultado(workoutId, equipeId, true);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove resultado de um atleta
     */
    @PreAuthorize("@workoutSecurityService.canManageWorkoutResults(#workoutId, authentication.name, authentication.authorities)")
    @DeleteMapping("/{workoutId}/resultados/atleta/{atletaId}")
    public ResponseEntity<Void> removerResultadoAtleta(
            @PathVariable Long workoutId,
            @PathVariable Long atletaId) {
        workoutResultService.removerResultado(workoutId, atletaId, false);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica se um workout já tem resultados inicializados
     */
    @GetMapping("/{workoutId}/resultados/status")
    public ResponseEntity<Map<String, Boolean>> verificarResultadosInicializados(
            @PathVariable Long workoutId,
            @RequestParam Long categoriaId) {
        boolean temResultados = workoutResultService.workoutTemResultados(categoriaId, workoutId);
        return ResponseEntity.ok(Map.of("temResultados", temResultados));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if (e.getMessage().contains("não encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Workout não encontrado", "message", e.getMessage()));
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