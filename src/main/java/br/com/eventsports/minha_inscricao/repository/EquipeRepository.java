package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipeRepository extends JpaRepository<EquipeEntity, Long> {

    @Override
    @NonNull
    Optional<EquipeEntity> findById(@NonNull Long id);

    @Override
    @NonNull
    List<EquipeEntity> findAll();

    @Override
    @NonNull
    <S extends EquipeEntity> S save(@NonNull S entity);

    @Override
    void deleteById(@NonNull Long id);

    List<EquipeEntity> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT e FROM EquipeEntity e WHERE e.evento.id = :eventoId ORDER BY e.nome ASC")
    List<EquipeEntity> findByEventoIdOrderByNomeAsc(@Param("eventoId") Long eventoId);

    @Query("SELECT e FROM EquipeEntity e WHERE e.categoria.id = :categoriaId ORDER BY e.nome ASC")
    List<EquipeEntity> findByCategoriaIdOrderByNomeAsc(@Param("categoriaId") Long categoriaId);

    @Query("SELECT e FROM EquipeEntity e WHERE e.capitao.id = :capitaoId ORDER BY e.nome ASC")
    List<EquipeEntity> findByCapitaoIdOrderByNomeAsc(@Param("capitaoId") Long capitaoId);

    List<EquipeEntity> findByAtivaTrue();

    List<EquipeEntity> findByAtivaFalse();

    @Query("SELECT e FROM EquipeEntity e WHERE e.evento.id = :eventoId AND e.categoria.id = :categoriaId ORDER BY e.nome ASC")
    List<EquipeEntity> findByEventoIdAndCategoriaIdOrderByNomeAsc(@Param("eventoId") Long eventoId, @Param("categoriaId") Long categoriaId);

    @Query("SELECT e FROM EquipeEntity e WHERE e.evento.id = :eventoId AND e.ativa = :ativa ORDER BY e.nome ASC")
    List<EquipeEntity> findByEventoIdAndAtivaOrderByNomeAsc(@Param("eventoId") Long eventoId, @Param("ativa") Boolean ativa);

    @Query("SELECT e FROM EquipeEntity e WHERE e.evento.id = :eventoId AND e.ativa = true AND SIZE(e.atletas) >= 2")
    List<EquipeEntity> findEquipesCompletasByEvento(@Param("eventoId") Long eventoId);

    @Query("SELECT e FROM EquipeEntity e WHERE e.evento.id = :eventoId AND e.ativa = true AND SIZE(e.atletas) < 6")
    List<EquipeEntity> findEquipesComVagasByEvento(@Param("eventoId") Long eventoId);

    @Query("SELECT e FROM EquipeEntity e WHERE e.categoria.id = :categoriaId AND e.ativa = true AND SIZE(e.atletas) >= 2")
    List<EquipeEntity> findEquipesCompletasByCategoria(@Param("categoriaId") Long categoriaId);

    @Query("SELECT a.equipe FROM AtletaEntity a WHERE a.id = :atletaId AND a.equipe IS NOT NULL")
    List<EquipeEntity> findEquipesByAtleta(@Param("atletaId") Long atletaId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EquipeEntity e WHERE e.nome = :nome AND e.evento.id = :eventoId")
    boolean existsByNomeAndEventoId(@Param("nome") String nome, @Param("eventoId") Long eventoId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EquipeEntity e WHERE e.nome = :nome AND e.evento.id = :eventoId AND e.id != :equipeId")
    boolean existsByNomeAndEventoIdAndIdNot(@Param("nome") String nome, @Param("eventoId") Long eventoId, @Param("equipeId") Long equipeId);

    @Query("SELECT COUNT(e) FROM EquipeEntity e WHERE e.evento.id = :eventoId")
    long countByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT COUNT(e) FROM EquipeEntity e WHERE e.categoria.id = :categoriaId")
    long countByCategoriaId(@Param("categoriaId") Long categoriaId);

    @Query("SELECT COUNT(e) FROM EquipeEntity e WHERE e.evento.id = :eventoId AND e.ativa = true")
    long countByEventoIdAndAtivaTrue(@Param("eventoId") Long eventoId);

    @Query("SELECT a.nome FROM AtletaEntity a WHERE a.equipe.id = :equipeId ORDER BY a.nome")
    List<String> findNomesAtletasByEquipeId(@Param("equipeId") Long equipeId);

    @Modifying
    @Query("UPDATE EquipeEntity e SET e.pontuacaoTotal = :pontuacao WHERE e.id = :id")
    void updatePontuacaoTotal(@Param("id") Long id, @Param("pontuacao") Integer pontuacao);
}
