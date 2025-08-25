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

import br.com.eventsports.minha_inscricao.dto.workout.WorkoutCreateDTO;
import br.com.eventsports.minha_inscricao.dto.workout.WorkoutResponseDTO;
import br.com.eventsports.minha_inscricao.dto.workout.WorkoutSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.workout.WorkoutUpdateDTO;
import br.com.eventsports.minha_inscricao.service.Interfaces.IWorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class WorkoutController {

    private final IWorkoutService workoutService;

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

    @PostMapping
    public ResponseEntity<WorkoutResponseDTO> createWorkout(@Valid @RequestBody WorkoutCreateDTO workoutCreateDTO) {
        WorkoutResponseDTO createdWorkout = workoutService.save(workoutCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkout);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponseDTO> updateWorkout(@PathVariable Long id,
            @Valid @RequestBody WorkoutUpdateDTO workoutUpdateDTO) {
        WorkoutResponseDTO updatedWorkout = workoutService.update(id, workoutUpdateDTO);
        return ResponseEntity.ok(updatedWorkout);
    }

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

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<WorkoutResponseDTO> ativarWorkout(@PathVariable Long id) {
        WorkoutResponseDTO workout = workoutService.ativar(id);
        return ResponseEntity.ok(workout);
    }

    @PatchMapping("/{id}/desativar")
    public ResponseEntity<WorkoutResponseDTO> desativarWorkout(@PathVariable Long id) {
        WorkoutResponseDTO workout = workoutService.desativar(id);
        return ResponseEntity.ok(workout);
    }

    @PostMapping("/{workoutId}/categorias/{categoriaId}")
    public ResponseEntity<WorkoutResponseDTO> adicionarCategoria(@PathVariable Long workoutId,
            @PathVariable Long categoriaId) {
        WorkoutResponseDTO workout = workoutService.adicionarCategoria(workoutId, categoriaId);
        return ResponseEntity.ok(workout);
    }

    @DeleteMapping("/{workoutId}/categorias/{categoriaId}")
    public ResponseEntity<WorkoutResponseDTO> removerCategoria(@PathVariable Long workoutId,
            @PathVariable Long categoriaId) {
        WorkoutResponseDTO workout = workoutService.removerCategoria(workoutId, categoriaId);
        return ResponseEntity.ok(workout);
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