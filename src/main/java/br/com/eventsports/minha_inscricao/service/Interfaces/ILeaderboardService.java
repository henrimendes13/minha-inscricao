package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardRankingDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardResultadoCreateDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardResultadoLoteDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardResultadoUpdateDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardResponseDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.LeaderboardSummaryDTO;

import java.util.List;

public interface ILeaderboardService {
    
    /**
     * Busca o leaderboard de um workout específico
     */
    List<LeaderboardSummaryDTO> getLeaderboardWorkout(Long categoriaId, Long workoutId);
    
    /**
     * Busca resultados de um workout com recálculo automático de posições
     */
    List<LeaderboardSummaryDTO> getLeaderboardWorkoutComRecalculo(Long categoriaId, Long workoutId);
    
    /**
     * Busca todos os resultados de uma equipe
     */
    List<LeaderboardSummaryDTO> getLeaderboardEquipe(Long equipeId);
    
    /**
     * Busca todos os resultados de um atleta
     */
    List<LeaderboardSummaryDTO> getLeaderboardAtleta(Long atletaId);
    
    /**
     * Registra um resultado no leaderboard
     */
    LeaderboardResponseDTO registrarLeaderboardResultado(LeaderboardResultadoCreateDTO dto);
    
    /**
     * Registra resultados em lote no leaderboard
     */
    List<LeaderboardResponseDTO> registrarLeaderboardResultadosLote(LeaderboardResultadoLoteDTO dto);
    
    /**
     * Atualiza um resultado no leaderboard
     */
    LeaderboardResponseDTO atualizarLeaderboardResultado(Long id, LeaderboardResultadoUpdateDTO dto);
    
    /**
     * Calcula ranking de um workout
     */
    List<LeaderboardSummaryDTO> calcularRankingWorkout(Long categoriaId, Long workoutId);
    
    /**
     * Deleta um resultado do leaderboard
     */
    void deletarLeaderboardResultado(Long id);
    
    /**
     * Busca ranking completo de uma categoria em um evento
     */
    List<LeaderboardRankingDTO> getRankingCategoria(Long eventoId, Long categoriaId);
}
