package br.com.eventsports.minha_inscricao.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardFinalDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardResponseDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardSummaryDTO;
import br.com.eventsports.minha_inscricao.service.Interfaces.ILeaderboardService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/leaderboards")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LeaderboardController {

    private final ILeaderboardService leaderboardService;


    // ========== ENDPOINTS PARA CONSULTA DE LEADERBOARDS ==========

    @GetMapping("/categoria/{categoriaId}/final")
    public ResponseEntity<List<LeaderboardFinalDTO>> getLeaderboardFinalCategoria(@PathVariable Long categoriaId) {
        List<LeaderboardFinalDTO> leaderboard = leaderboardService.getLeaderboardFinalCategoria(categoriaId);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/categoria/{categoriaId}/workout/{workoutId}")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getLeaderboardWorkout(@PathVariable Long categoriaId,
            @PathVariable Long workoutId) {
        List<LeaderboardSummaryDTO> resultados = leaderboardService.getLeaderboardWorkout(categoriaId, workoutId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/categoria/{categoriaId}/workout/{workoutId}/resultados")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getResultadosWorkoutComNomes(@PathVariable Long categoriaId,
            @PathVariable Long workoutId) {
        List<LeaderboardSummaryDTO> resultados = leaderboardService.getLeaderboardWorkoutComRecalculo(categoriaId, workoutId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<LeaderboardResponseDTO>> getLeaderboardCategoria(@PathVariable Long categoriaId) {
        List<LeaderboardResponseDTO> resultados = leaderboardService.getLeaderboardCategoria(categoriaId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/equipe/{equipeId}")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getLeaderboardEquipe(@PathVariable Long equipeId) {
        List<LeaderboardSummaryDTO> resultados = leaderboardService.getLeaderboardEquipe(equipeId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/atleta/{atletaId}")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getLeaderboardAtleta(@PathVariable Long atletaId) {
        List<LeaderboardSummaryDTO> resultados = leaderboardService.getLeaderboardAtleta(atletaId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<LeaderboardResponseDTO>> getLeaderboardEvento(@PathVariable Long eventoId) {
        List<LeaderboardResponseDTO> resultados = leaderboardService.getLeaderboardEvento(eventoId);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/categoria/{categoriaId}/estatisticas")
    public ResponseEntity<EstatisticasResponse> getEstatisticasCategoria(@PathVariable Long categoriaId) {
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Erro na operação", "message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor", "message", e.getMessage()));
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