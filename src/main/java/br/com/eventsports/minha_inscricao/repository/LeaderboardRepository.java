package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.LeaderboardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntity, Long> {

    /**
     * Busca todos os resultados de uma categoria específica
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId ORDER BY l.posicaoWorkout ASC")
    List<LeaderboardEntity> findByCategoriaIdOrderByPosicaoWorkoutAsc(@Param("categoriaId") Long categoriaId);

    /**
     * Busca todos os resultados de um evento específico
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.evento.id = :eventoId ORDER BY l.categoria.nome ASC, l.posicaoWorkout ASC")
    List<LeaderboardEntity> findByEventoIdOrderByCategoriaNomeAscPosicaoWorkoutAsc(@Param("eventoId") Long eventoId);

    /**
     * Busca resultados de uma categoria e workout específicos
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.workout.id = :workoutId ORDER BY l.posicaoWorkout ASC")
    List<LeaderboardEntity> findByCategoriaIdAndWorkoutIdOrderByPosicaoWorkoutAsc(@Param("categoriaId") Long categoriaId, @Param("workoutId") Long workoutId);

    /**
     * Busca resultados de uma equipe específica
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.equipe.id = :equipeId ORDER BY l.workout.nome ASC")
    List<LeaderboardEntity> findByEquipeIdOrderByWorkoutNomeAsc(@Param("equipeId") Long equipeId);

    /**
     * Busca resultados de um atleta específico
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.atleta.id = :atletaId ORDER BY l.workout.nome ASC")
    List<LeaderboardEntity> findByAtletaIdOrderByWorkoutNomeAsc(@Param("atletaId") Long atletaId);

    /**
     * Busca resultado específico de uma equipe em um workout
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.workout.id = :workoutId AND l.equipe.id = :equipeId")
    Optional<LeaderboardEntity> findByCategoriaIdAndWorkoutIdAndEquipeId(@Param("categoriaId") Long categoriaId, @Param("workoutId") Long workoutId, @Param("equipeId") Long equipeId);

    /**
     * Busca resultado específico de um atleta em um workout
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.workout.id = :workoutId AND l.atleta.id = :atletaId")
    Optional<LeaderboardEntity> findByCategoriaIdAndWorkoutIdAndAtletaId(@Param("categoriaId") Long categoriaId, @Param("workoutId") Long workoutId, @Param("atletaId") Long atletaId);

    /**
     * Busca todas as equipes que participaram de uma categoria
     */
    @Query("SELECT DISTINCT l.equipe FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.equipe IS NOT NULL")
    List<Object> findEquipesByCategoriaId(@Param("categoriaId") Long categoriaId);

    /**
     * Busca todos os atletas que participaram de uma categoria
     */
    @Query("SELECT DISTINCT l.atleta FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.atleta IS NOT NULL")
    List<Object> findAtletasByCategoriaId(@Param("categoriaId") Long categoriaId);

    /**
     * Conta quantos workouts uma equipe finalizou em uma categoria
     */
    @Query("SELECT COUNT(l) FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.equipe.id = :equipeId AND l.finalizado = true")
    long countWorkoutsFinalizadosByEquipe(@Param("categoriaId") Long categoriaId, @Param("equipeId") Long equipeId);

    /**
     * Conta quantos workouts um atleta finalizou em uma categoria
     */
    @Query("SELECT COUNT(l) FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.atleta.id = :atletaId AND l.finalizado = true")
    long countWorkoutsFinalizadosByAtleta(@Param("categoriaId") Long categoriaId, @Param("atletaId") Long atletaId);

    /**
     * Soma das posições de uma equipe em uma categoria (pontuação total)
     */
    @Query("SELECT COALESCE(SUM(l.posicaoWorkout), 0) FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.equipe.id = :equipeId AND l.posicaoWorkout IS NOT NULL")
    Integer sumPosicoesByEquipe(@Param("categoriaId") Long categoriaId, @Param("equipeId") Long equipeId);

    /**
     * Soma das posições de um atleta em uma categoria (pontuação total)
     */
    @Query("SELECT COALESCE(SUM(l.posicaoWorkout), 0) FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.atleta.id = :atletaId AND l.posicaoWorkout IS NOT NULL")
    Integer sumPosicoesByAtleta(@Param("categoriaId") Long categoriaId, @Param("atletaId") Long atletaId);

    /**
     * Ranking de equipes por categoria (ordenado por soma das posições - menor primeiro)
     */
    @Query("""
        SELECT l.equipe, COALESCE(SUM(l.posicaoWorkout), 0) as pontuacaoTotal 
        FROM LeaderboardEntity l 
        WHERE l.categoria.id = :categoriaId 
        AND l.equipe IS NOT NULL 
        AND l.posicaoWorkout IS NOT NULL 
        GROUP BY l.equipe 
        ORDER BY pontuacaoTotal ASC
        """)
    List<Object[]> findRankingEquipesByCategoria(@Param("categoriaId") Long categoriaId);

    /**
     * Ranking de atletas por categoria (ordenado por soma das posições - menor primeiro)
     */
    @Query("""
        SELECT l.atleta, COALESCE(SUM(l.posicaoWorkout), 0) as pontuacaoTotal 
        FROM LeaderboardEntity l 
        WHERE l.categoria.id = :categoriaId 
        AND l.atleta IS NOT NULL 
        AND l.posicaoWorkout IS NOT NULL 
        GROUP BY l.atleta 
        ORDER BY pontuacaoTotal ASC
        """)
    List<Object[]> findRankingAtletasByCategoria(@Param("categoriaId") Long categoriaId);

    /**
     * Busca workouts de uma categoria que ainda não têm resultados registrados
     */
    @Query("""
        SELECT w FROM WorkoutEntity w 
        WHERE w.id IN (
            SELECT wc.id FROM WorkoutEntity wc 
            JOIN wc.categorias c 
            WHERE c.id = :categoriaId
        ) 
        AND w.id NOT IN (
            SELECT DISTINCT l.workout.id FROM LeaderboardEntity l 
            WHERE l.categoria.id = :categoriaId
        )
        """)
    List<Object> findWorkoutsSemResultados(@Param("categoriaId") Long categoriaId);

    /**
     * Verifica se existe resultado para um participante específico em um workout
     */
    @Query("""
        SELECT COUNT(l) > 0 FROM LeaderboardEntity l 
        WHERE l.categoria.id = :categoriaId 
        AND l.workout.id = :workoutId 
        AND (
            (l.equipe.id = :participanteId AND l.equipe IS NOT NULL) 
            OR (l.atleta.id = :participanteId AND l.atleta IS NOT NULL)
        )
        """)
    boolean existsResultadoParaParticipante(@Param("categoriaId") Long categoriaId, 
                                          @Param("workoutId") Long workoutId, 
                                          @Param("participanteId") Long participanteId);

    /**
     * Busca estatísticas gerais de uma categoria
     */
    @Query("""
        SELECT 
            COUNT(DISTINCT CASE WHEN l.equipe IS NOT NULL THEN l.equipe.id ELSE l.atleta.id END) as totalParticipantes,
            COUNT(DISTINCT l.workout.id) as totalWorkouts,
            COUNT(CASE WHEN l.finalizado = true THEN 1 END) as totalFinalizados,
            COUNT(l) as totalResultados
        FROM LeaderboardEntity l 
        WHERE l.categoria.id = :categoriaId
        """)
    Object[] findEstatisticasCategoria(@Param("categoriaId") Long categoriaId);
}
