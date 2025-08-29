package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
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
     * Métodos específicos para WorkoutResultService
     */
    
    /**
     * Busca resultado de uma equipe específica em um workout
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.workout.id = :workoutId AND l.equipe.id = :equipeId")
    Optional<LeaderboardEntity> findByWorkoutIdAndEquipeId(@Param("workoutId") Long workoutId, @Param("equipeId") Long equipeId);

    /**
     * Busca resultado de um atleta específico em um workout
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.workout.id = :workoutId AND l.atleta.id = :atletaId")
    Optional<LeaderboardEntity> findByWorkoutIdAndAtletaId(@Param("workoutId") Long workoutId, @Param("atletaId") Long atletaId);

    /**
     * Verifica se existem resultados para um workout em uma categoria
     */
    @Query("SELECT COUNT(l) > 0 FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.workout.id = :workoutId")
    boolean existsResultadosParaWorkout(@Param("categoriaId") Long categoriaId, @Param("workoutId") Long workoutId);

    /**
     * Conta participantes que finalizaram um workout específico
     */
    @Query("SELECT COUNT(l) FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.workout.id = :workoutId AND l.finalizado = true")
    long countFinalizadosByWorkout(@Param("categoriaId") Long categoriaId, @Param("workoutId") Long workoutId);

    /**
     * Conta total de participantes em um workout específico
     */
    @Query("SELECT COUNT(l) FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.workout.id = :workoutId")
    long countTotalByWorkout(@Param("categoriaId") Long categoriaId, @Param("workoutId") Long workoutId);

    /**
     * Busca participantes que não finalizaram um workout
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND l.workout.id = :workoutId AND l.finalizado = false")
    List<LeaderboardEntity> findByCategoriaIdAndWorkoutIdAndFinalizadoFalse(@Param("categoriaId") Long categoriaId, @Param("workoutId") Long workoutId);

    /**
     * Queries adicionais para o novo modelo de pontuação
     */
    
    /**
     * Busca ranking completo de equipes de uma categoria por pontuação
     */
    @Query(value = """
        SELECT e.* FROM equipes e 
        WHERE e.categoria_id = :categoriaId 
        AND e.pontuacao_total IS NOT NULL
        ORDER BY e.pontuacao_total ASC
        """, nativeQuery = true)
    List<EquipeEntity> findEquipesRankingByCategoria(@Param("categoriaId") Long categoriaId);

    /**
     * Busca ranking completo de atletas de uma categoria por pontuação
     */
    @Query(value = """
        SELECT a.* FROM atletas a 
        WHERE a.categoria_id = :categoriaId 
        AND a.pontuacao_total IS NOT NULL
        ORDER BY a.pontuacao_total ASC
        """, nativeQuery = true)
    List<AtletaEntity> findAtletasRankingByCategoria(@Param("categoriaId") Long categoriaId);

    /**
     * Busca top N equipes de uma categoria por pontuação (mantido para compatibilidade)
     */
    @Query(value = """
        SELECT e.* FROM equipes e 
        WHERE e.categoria_id = :categoriaId 
        AND e.pontuacao_total IS NOT NULL
        ORDER BY e.pontuacao_total ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<EquipeEntity> findTopEquipesByCategoria(@Param("categoriaId") Long categoriaId, @Param("limit") int limit);

    /**
     * Busca top N atletas de uma categoria por pontuação (mantido para compatibilidade)
     */
    @Query(value = """
        SELECT a.* FROM atletas a 
        WHERE a.categoria_id = :categoriaId 
        AND a.pontuacao_total IS NOT NULL
        ORDER BY a.pontuacao_total ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<AtletaEntity> findTopAtletasByCategoria(@Param("categoriaId") Long categoriaId, @Param("limit") int limit);

    /**
     * Recalcula pontuação de um atleta baseado em seus resultados
     */
    @Query("SELECT COALESCE(SUM(l.posicaoWorkout), 0) FROM LeaderboardEntity l WHERE l.atleta.id = :atletaId AND l.posicaoWorkout IS NOT NULL")
    Integer calcularPontuacaoAtleta(@Param("atletaId") Long atletaId);

    /**
     * Recalcula pontuação de uma equipe baseado em seus resultados
     */
    @Query("SELECT COALESCE(SUM(l.posicaoWorkout), 0) FROM LeaderboardEntity l WHERE l.equipe.id = :equipeId AND l.posicaoWorkout IS NOT NULL")
    Integer calcularPontuacaoEquipe(@Param("equipeId") Long equipeId);

    /**
     * Busca todas as posições de um participante (atleta ou equipe) em uma categoria
     */
    @Query("SELECT l FROM LeaderboardEntity l WHERE l.categoria.id = :categoriaId AND (l.atleta.id = :participanteId OR l.equipe.id = :participanteId) ORDER BY l.workout.nome ASC")
    List<LeaderboardEntity> findPosicoesByParticipanteAndCategoria(@Param("categoriaId") Long categoriaId, @Param("participanteId") Long participanteId);
}
