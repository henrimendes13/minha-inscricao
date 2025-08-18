package br.com.eventsports.minha_inscricao.controller;

import br.com.eventsports.minha_inscricao.dto.leaderboard.*;
import br.com.eventsports.minha_inscricao.service.Interfaces.ILeaderboardService;
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
@RequestMapping("/api/leaderboards")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "Leaderboards", description = "APIs para consulta de classificações e rankings")
public class LeaderboardController {

    private final ILeaderboardService leaderboardService;

    // ========== ENDPOINTS PARA ORGANIZADOR REGISTRAR RESULTADOS ==========

    @PostMapping("/leaderboard-resultado")
    @Operation(summary = "Registrar resultado de um participante", 
               description = "Registra o resultado de uma equipe ou atleta em um workout específico")
    public ResponseEntity<LeaderboardResponseDTO> registrarLeaderboardResultado(
            @Valid @RequestBody LeaderboardResultadoCreateDTO dto) {
        try {
            LeaderboardResponseDTO resultado = leaderboardService.registrarLeaderboardResultado(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/leaderboard-resultado/lote")
    @Operation(summary = "Registrar múltiplos resultados em lote", 
               description = "Registra resultados de múltiplos participantes de uma vez")
    public ResponseEntity<List<LeaderboardResponseDTO>> registrarLeaderboardResultadosLote(
            @Valid @RequestBody LeaderboardResultadoLoteDTO dto) {
        try {
            List<LeaderboardResponseDTO> resultados = leaderboardService.registrarLeaderboardResultadosLote(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultados);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/leaderboard-resultado/{id}")
    @Operation(summary = "Atualizar resultado existente", 
               description = "Atualiza um resultado já registrado")
    public ResponseEntity<LeaderboardResponseDTO> atualizarLeaderboardResultado(
            @Parameter(description = "ID do resultado") @PathVariable Long id,
            @Valid @RequestBody LeaderboardResultadoUpdateDTO dto) {
        try {
            LeaderboardResponseDTO resultado = leaderboardService.atualizarLeaderboardResultado(id, dto);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/categoria/{categoriaId}/workout/{workoutId}/calcular-ranking")
    @Operation(summary = "Calcular ranking do workout", 
               description = "Calcula e atribui posições baseadas nos resultados registrados")
    public ResponseEntity<List<LeaderboardSummaryDTO>> calcularRankingWorkout(
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId,
            @Parameter(description = "ID do workout") @PathVariable Long workoutId) {
        try {
            List<LeaderboardSummaryDTO> ranking = leaderboardService.calcularRankingWorkout(categoriaId, workoutId);
            return ResponseEntity.ok(ranking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/leaderboard-resultado/{id}")
    @Operation(summary = "Deletar resultado", 
               description = "Remove um resultado do sistema")
    public ResponseEntity<Void> deletarLeaderboardResultado(
            @Parameter(description = "ID do resultado") @PathVariable Long id) {
        try {
            leaderboardService.deletarLeaderboardResultado(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== ENDPOINTS PARA CONSULTA DE LEADERBOARDS ==========

    @GetMapping("/categoria/{categoriaId}/final")
    @Operation(summary = "Obter leaderboard final da categoria", 
               description = "Retorna o ranking final de uma categoria, ordenado por pontuação total (menor pontuação ganha)")
    public ResponseEntity<List<LeaderboardFinalDTO>> getLeaderboardFinalCategoria(
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId) {
        List<LeaderboardFinalDTO> leaderboard = leaderboardService.getLeaderboardFinalCategoria(categoriaId);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/categoria/{categoriaId}/workout/{workoutId}")
    @Operation(summary = "Obter resultados de um workout específico", 
               description = "Retorna os resultados de um workout específico em uma categoria")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getLeaderboardWorkout(
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId,
            @Parameter(description = "ID do workout") @PathVariable Long workoutId) {
        List<LeaderboardSummaryDTO> resultados = leaderboardService.getLeaderboardWorkout(categoriaId, workoutId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Obter todos os resultados de uma categoria", 
               description = "Retorna todos os resultados de workouts de uma categoria")
    public ResponseEntity<List<LeaderboardResponseDTO>> getLeaderboardCategoria(
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId) {
        List<LeaderboardResponseDTO> resultados = leaderboardService.getLeaderboardCategoria(categoriaId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/equipe/{equipeId}")
    @Operation(summary = "Obter resultados de uma equipe", 
               description = "Retorna todos os resultados de uma equipe específica")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getLeaderboardEquipe(
            @Parameter(description = "ID da equipe") @PathVariable Long equipeId) {
        List<LeaderboardSummaryDTO> resultados = leaderboardService.getLeaderboardEquipe(equipeId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/atleta/{atletaId}")
    @Operation(summary = "Obter resultados de um atleta", 
               description = "Retorna todos os resultados de um atleta específico")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getLeaderboardAtleta(
            @Parameter(description = "ID do atleta") @PathVariable Long atletaId) {
        List<LeaderboardSummaryDTO> resultados = leaderboardService.getLeaderboardAtleta(atletaId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/evento/{eventoId}")
    @Operation(summary = "Obter todos os leaderboards de um evento", 
               description = "Retorna todos os resultados de todas as categorias de um evento")
    public ResponseEntity<List<LeaderboardResponseDTO>> getLeaderboardEvento(
            @Parameter(description = "ID do evento") @PathVariable Long eventoId) {
        List<LeaderboardResponseDTO> resultados = leaderboardService.getLeaderboardEvento(eventoId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/categoria/{categoriaId}/estatisticas")
    @Operation(summary = "Obter estatísticas de uma categoria", 
               description = "Retorna estatísticas gerais de uma categoria (participantes, workouts, etc.)")
    public ResponseEntity<EstatisticasResponse> getEstatisticasCategoria(
            @Parameter(description = "ID da categoria") @PathVariable Long categoriaId) {
        Object[] estatisticas = leaderboardService.getEstatisticasCategoria(categoriaId);
        
        if (estatisticas != null && estatisticas.length >= 4) {
            Long totalParticipantes = (Long) estatisticas[0];
            Long totalWorkouts = (Long) estatisticas[1];
            Long totalFinalizados = (Long) estatisticas[2];
            Long totalResultados = (Long) estatisticas[3];
            Double percentualFinalizacao = totalResultados > 0 ? 
                (totalFinalizados.doubleValue() / totalResultados.doubleValue()) * 100 : 0.0;
            
            EstatisticasResponse response = new EstatisticasResponse(
                totalParticipantes, totalWorkouts, totalFinalizados, 
                totalResultados, percentualFinalizacao
            );
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.notFound().build();
    }

    // Classe interna para resposta das estatísticas
    public static class EstatisticasResponse {
        public final Long totalParticipantes;
        public final Long totalWorkouts;
        public final Long totalFinalizados;
        public final Long totalResultados;
        public final Double percentualFinalizacao;

        public EstatisticasResponse(Long totalParticipantes, Long totalWorkouts, 
                                   Long totalFinalizados, Long totalResultados, 
                                   Double percentualFinalizacao) {
            this.totalParticipantes = totalParticipantes;
            this.totalWorkouts = totalWorkouts;
            this.totalFinalizados = totalFinalizados;
            this.totalResultados = totalResultados;
            this.percentualFinalizacao = percentualFinalizacao;
        }
    }
}
