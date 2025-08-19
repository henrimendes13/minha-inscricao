package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.TimelineEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimelineRepository extends JpaRepository<TimelineEntity, Long> {

    @Override
    @Cacheable(value = "timelines", key = "#id")
    Optional<TimelineEntity> findById(Long id);

    @Override
    @CachePut(value = "timelines", key = "#result.id")
    <S extends TimelineEntity> S save(S entity);

    @Override
    @CacheEvict(value = "timelines", key = "#timeline.id")
    void delete(TimelineEntity timeline);

    @Override
    @CacheEvict(value = "timelines", key = "#id")
    void deleteById(Long id);

    @Cacheable(value = "timelines", key = "'evento-' + #eventoId")
    @Query("SELECT t FROM TimelineEntity t WHERE t.evento.id = :eventoId")
    Optional<TimelineEntity> findByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT COUNT(t) > 0 FROM TimelineEntity t WHERE t.evento.id = :eventoId")
    boolean existsByEventoId(@Param("eventoId") Long eventoId);

    @CacheEvict(value = "timelines", key = "'evento-' + #eventoId")
    @Query("DELETE FROM TimelineEntity t WHERE t.evento.id = :eventoId")
    void deleteByEventoId(@Param("eventoId") Long eventoId);
}