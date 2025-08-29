package br.com.eventsports.minha_inscricao.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardRankingDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardSummaryDTO;
import br.com.eventsports.minha_inscricao.service.Interfaces.ILeaderboardService;
import br.com.eventsports.minha_inscricao.service.PontuacaoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/leaderboards")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LeaderboardController {

    private final ILeaderboardService leaderboardService;
    private final PontuacaoService pontuacaoService;


    @GetMapping("/categoria/{categoriaId}/workout/{workoutId}/resultados")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getResultadosWorkoutComNomes(@PathVariable Long categoriaId,
            @PathVariable Long workoutId) {
        List<LeaderboardSummaryDTO> resultados = leaderboardService.getLeaderboardWorkout(categoriaId, workoutId);
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

    /**
     * Busca ranking completo de uma categoria em um evento
     */
    @GetMapping("/evento/{eventoId}/categoria/{categoriaId}/ranking")
    public ResponseEntity<List<LeaderboardRankingDTO>> getRankingCategoria(
            @PathVariable Long eventoId,
            @PathVariable Long categoriaId) {
        List<LeaderboardRankingDTO> ranking = leaderboardService.getRankingCategoria(eventoId, categoriaId);
        return ResponseEntity.ok(ranking);
    }

    @PostMapping("/categoria/{categoriaId}/recalcular-pontuacoes")
    public ResponseEntity<Map<String, String>> recalcularPontuacoesCategoria(@PathVariable Long categoriaId) {
        try {
            pontuacaoService.recalcularTodasPontuacoesPorCategoria(categoriaId);
            return ResponseEntity.ok(Map.of("status", "sucesso", "message", "Pontuações recalculadas com sucesso para categoria " + categoriaId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "erro", "message", "Erro ao recalcular pontuações: " + e.getMessage()));
        }
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

}