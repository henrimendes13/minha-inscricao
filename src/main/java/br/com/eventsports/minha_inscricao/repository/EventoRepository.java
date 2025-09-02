package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<EventoEntity, Long> {

    @Override
    Optional<EventoEntity> findById(Long id);

    @Override
    List<EventoEntity> findAll();

    @Override
    <S extends EventoEntity> S save(S entity);

    @Override
    void deleteById(Long id);

    List<EventoEntity> findByNomeContainingIgnoreCase(String nome);

    @Query("SELECT e FROM EventoEntity e WHERE e.dataInicioDoEvento BETWEEN :inicio AND :fim ORDER BY e.dataInicioDoEvento ASC")
    List<EventoEntity> findEventosByDataBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT e FROM EventoEntity e WHERE e.dataInicioDoEvento > CURRENT_TIMESTAMP ORDER BY e.dataInicioDoEvento ASC")
    List<EventoEntity> findEventosUpcoming();

    @Query("SELECT e FROM EventoEntity e WHERE e.dataFimDoEvento < CURRENT_TIMESTAMP ORDER BY e.dataFimDoEvento DESC")
    List<EventoEntity> findEventosPast();

    @Query("SELECT e FROM EventoEntity e WHERE e.dataInicioDoEvento > :data ORDER BY e.dataInicioDoEvento ASC")
    List<EventoEntity> findByDataInicioDoEventoAfterOrderByDataInicioDoEventoAsc(@Param("data") LocalDateTime data);

    @Query("SELECT e FROM EventoEntity e WHERE e.dataFimDoEvento < :data ORDER BY e.dataFimDoEvento DESC")
    List<EventoEntity> findByDataFimDoEventoBeforeOrderByDataFimDoEventoDesc(@Param("data") LocalDateTime data);
}
