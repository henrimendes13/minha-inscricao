package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.TimelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimelineRepository extends JpaRepository<TimelineEntity, Long> {

    @Override
    Optional<TimelineEntity> findById(Long id);

    @Override
    <S extends TimelineEntity> S save(S entity);

    @Override
    void delete(TimelineEntity timeline);

    @Override
    void deleteById(Long id);

    @Query("SELECT t FROM TimelineEntity t WHERE t.evento.id = :eventoId")
    Optional<TimelineEntity> findByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT COUNT(t) > 0 FROM TimelineEntity t WHERE t.evento.id = :eventoId")
    boolean existsByEventoId(@Param("eventoId") Long eventoId);

    @Query("DELETE FROM TimelineEntity t WHERE t.evento.id = :eventoId")
    void deleteByEventoId(@Param("eventoId") Long eventoId);
}