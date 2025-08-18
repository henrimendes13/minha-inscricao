package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.workout.*;
import br.com.eventsports.minha_inscricao.service.Interfaces.IWorkoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Workouts", description = "APIs para gerenciamento de workouts de eventos")
public class WorkoutController {

    private final IWorkoutService workoutService;

    @GetMapping
    @Operation(summary = "Listar todos os workouts", description = "Retorna uma lista com todos os workouts cadastrados")
    public ResponseEntity<List<WorkoutSummaryDTO>> getAllWorkouts() {
        List<WorkoutSummaryDTO> workouts = workoutService.findAll();
        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar workout por ID", description = "Retorna os detalhes completos de um workout específico")
    public ResponseEntity<WorkoutResponseDTO> getWorkoutById(
            @Parameter(description = "ID do workout") @PathVariable Long id) {
        try {
            WorkoutResponseDTO workout = workoutService.findById(id);
            return ResponseEntity.ok(workout);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Criar novo workout", description = "Cria um novo workout")
    public ResponseEntity<WorkoutResponseDTO> createWorkout(
            @Valid @RequestBody WorkoutCreateDTO workoutCreateDTO) {
        try {
            WorkoutResponseDTO createdWorkout = workoutService.save(workoutCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkout);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar workout", description = "Atualiza os dados de um workout existente")
    public ResponseEntity<WorkoutResponseDTO> updateWorkout(
            @Parameter(description = "ID do workout") @PathVariable Long id,
            @Valid @RequestBody WorkoutUpdateDTO workoutUpdateDTO) {
        try {
            WorkoutResponseDTO updatedWorkout = workoutService.update(id, workoutUpdateDTO);
            return ResponseEntity.ok(updatedWorkout);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar workout", description = "Remove um workout do sistema")
    public ResponseEntity<Void> deleteWorkout(
            @Parameter(description = "ID do workout") @PathVariable Long id) {
        try {
            workoutService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/evento/{eventoId}")
    @Operation(summary = "Listar workouts por evento", description = "Retorna todos os workouts de um evento específico")
    public ResponseEntity<List<WorkoutSummaryDTO>> getWorkoutsByEvento(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId,
            @Parameter(description = "Filtrar apenas workouts ativos") @RequestParam(defaultValue = "false") boolean apenasAtivos) {
        List<WorkoutSummaryDTO> workouts = workoutService.findByEventoIdAndAtivo(eventoId, apenasAtivos);
        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar workouts por categoria", description = "Retorna todos os workouts de uma categoria específica")
    public ResponseEntity<List<WorkoutSummaryDTO>> getWorkoutsByCategoria(
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId) {
        List<WorkoutSummaryDTO> workouts = workoutService.findByCategoriaId(categoriaId);
        return ResponseEntity.ok(workouts);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar workouts por nome", description = "Busca workouts que contenham o termo no nome")
    public ResponseEntity<List<WorkoutSummaryDTO>> searchWorkoutsByNome(
            @Parameter(description = "Termo de busca") @RequestParam String nome) {
        List<WorkoutSummaryDTO> workouts = workoutService.findByNome(nome);
        return ResponseEntity.ok(workouts);
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativar workout", description = "Ativa um workout específico")
    public ResponseEntity<WorkoutResponseDTO> ativarWorkout(
            @Parameter(description = "ID do workout") @PathVariable Long id) {
        try {
            WorkoutResponseDTO workout = workoutService.ativar(id);
            return ResponseEntity.ok(workout);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar workout", description = "Desativa um workout específico")
    public ResponseEntity<WorkoutResponseDTO> desativarWorkout(
            @Parameter(description = "ID do workout") @PathVariable Long id) {
        try {
            WorkoutResponseDTO workout = workoutService.desativar(id);
            return ResponseEntity.ok(workout);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{workoutId}/categorias/{categoriaId}")
    @Operation(summary = "Adicionar categoria ao workout", description = "Associa uma categoria a um workout")
    public ResponseEntity<WorkoutResponseDTO> adicionarCategoria(
            @Parameter(description = "ID do workout") @PathVariable Long workoutId,
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId) {
        try {
            WorkoutResponseDTO workout = workoutService.adicionarCategoria(workoutId, categoriaId);
            return ResponseEntity.ok(workout);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{workoutId}/categorias/{categoriaId}")
    @Operation(summary = "Remover categoria do workout", description = "Remove a associação de uma categoria com um workout")
    public ResponseEntity<WorkoutResponseDTO> removerCategoria(
            @Parameter(description = "ID do workout") @PathVariable Long workoutId,
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId) {
        try {
            WorkoutResponseDTO workout = workoutService.removerCategoria(workoutId, categoriaId);
            return ResponseEntity.ok(workout);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
