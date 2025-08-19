package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.WorkoutEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutRepository extends JpaRepository<WorkoutEntity, Long> {

    /**
     * Busca workouts por evento
     */
    @Query("SELECT w FROM WorkoutEntity w WHERE w.evento.id = :eventoId ORDER BY w.nome ASC")
    List<WorkoutEntity> findByEventoIdOrderByNomeAsc(@Param("eventoId") Long eventoId);

    /**
     * Busca workouts ativos
     */
    List<WorkoutEntity> findByAtivoTrue();

    /**
     * Busca workouts ativos por evento
     */
    @Query("SELECT w FROM WorkoutEntity w WHERE w.evento.id = :eventoId AND w.ativo = true")
    List<WorkoutEntity> findByEventoIdAndAtivoTrue(@Param("eventoId") Long eventoId);

    /**
     * Busca workouts por nome (case insensitive)
     */
    List<WorkoutEntity> findByNomeContainingIgnoreCase(String nome);

    /**
     * Verifica se existe workout com o mesmo nome no evento
     */
    @Query("SELECT COUNT(w) > 0 FROM WorkoutEntity w WHERE w.nome = :nome AND w.evento.id = :eventoId")
    boolean existsByNomeAndEventoId(@Param("nome") String nome, @Param("eventoId") Long eventoId);

    /**
     * Verifica se existe outro workout com o mesmo nome no evento (para update)
     */
    @Query("SELECT COUNT(w) > 0 FROM WorkoutEntity w WHERE w.nome = :nome AND w.evento.id = :eventoId AND w.id <> :id")
    boolean existsByNomeAndEventoIdAndIdNot(@Param("nome") String nome, @Param("eventoId") Long eventoId, @Param("id") Long id);

    /**
     * Busca workouts por categoria
     */
    @Query("SELECT DISTINCT w FROM WorkoutEntity w JOIN w.categorias c WHERE c.id = :categoriaId")
    List<WorkoutEntity> findByCategoriaId(@Param("categoriaId") Long categoriaId);

    /**
     * Busca workouts ativos por categoria
     */
    @Query("SELECT DISTINCT w FROM WorkoutEntity w JOIN w.categorias c WHERE c.id = :categoriaId AND w.ativo = true")
    List<WorkoutEntity> findByCategoriaIdAndAtivoTrue(@Param("categoriaId") Long categoriaId);

    /**
     * Busca workouts que não possuem categorias associadas
     */
    @Query("SELECT w FROM WorkoutEntity w WHERE w.categorias IS EMPTY")
    List<WorkoutEntity> findWorkoutsSemCategorias();

    /**
     * Conta workouts ativos por evento
     */
    @Query("SELECT COUNT(w) FROM WorkoutEntity w WHERE w.evento.id = :eventoId AND w.ativo = true")
    long countAtivosByEventoId(@Param("eventoId") Long eventoId);

    /**
     * Busca workouts por evento com contagem de categorias
     */
    @Query("SELECT w, COUNT(c) as totalCategorias FROM WorkoutEntity w " +
           "LEFT JOIN w.categorias c " +
           "WHERE w.evento.id = :eventoId " +
           "GROUP BY w.id " +
           "ORDER BY w.nome")
    List<Object[]> findByEventoIdWithCategoriaCount(@Param("eventoId") Long eventoId);

    /**
     * Busca workouts que podem receber categorias (ativos do evento)
     */
    @Query("SELECT w FROM WorkoutEntity w WHERE w.evento.id = :eventoId AND w.ativo = true")
    List<WorkoutEntity> findWorkoutsDisponiveis(@Param("eventoId") Long eventoId);

    /**
     * Busca workouts por múltiplas categorias
     */
    @Query("SELECT DISTINCT w FROM WorkoutEntity w JOIN w.categorias c WHERE c.id IN :categoriasIds")
    List<WorkoutEntity> findByCategoriasIds(@Param("categoriasIds") List<Long> categoriasIds);

    /**
     * Busca workouts com todas as categorias especificadas
     */
    @Query("SELECT w FROM WorkoutEntity w WHERE " +
           "(SELECT COUNT(c) FROM WorkoutEntity w2 JOIN w2.categorias c WHERE w2.id = w.id AND c.id IN :categoriasIds) = :totalCategorias")
    List<WorkoutEntity> findWorkoutsComTodasCategorias(@Param("categoriasIds") List<Long> categoriasIds, @Param("totalCategorias") long totalCategorias);
}
