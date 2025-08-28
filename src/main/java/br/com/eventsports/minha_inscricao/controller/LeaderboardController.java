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

import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardSummaryDTO;
import br.com.eventsports.minha_inscricao.service.Interfaces.ILeaderboardService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/leaderboards")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LeaderboardController {

    private final ILeaderboardService leaderboardService;


    @GetMapping("/categoria/{categoriaId}/workout/{workoutId}/resultados")
    public ResponseEntity<List<LeaderboardSummaryDTO>> getResultadosWorkoutComNomes(@PathVariable Long categoriaId,
            @PathVariable Long workoutId) {
        List<LeaderboardSummaryDTO> resultados = leaderboardService.getLeaderboardWorkoutComRecalculo(categoriaId, workoutId);
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