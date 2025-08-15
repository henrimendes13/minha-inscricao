package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(value = "eventos", key = "#id")
    Optional<EventoEntity> findById(Long id);

    @Override
    @Cacheable(value = "eventos")
    List<EventoEntity> findAll();

    @Override
    @CachePut(value = "eventos", key = "#result.id")
    <S extends EventoEntity> S save(S entity);

    @Override
    @CacheEvict(value = "eventos", key = "#id")
    void deleteById(Long id);

    @Cacheable(value = "eventos", key = "'byNome:' + #nome")
    List<EventoEntity> findByNomeContainingIgnoreCase(String nome);

    @Cacheable(value = "eventos", key = "'byDataBetween:' + #inicio + ':' + #fim")
    @Query("SELECT e FROM EventoEntity e WHERE e.data BETWEEN :inicio AND :fim ORDER BY e.data ASC")
    List<EventoEntity> findEventosByDataBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Cacheable(value = "eventos", key = "'upcoming'")
    @Query("SELECT e FROM EventoEntity e WHERE e.data > CURRENT_TIMESTAMP ORDER BY e.data ASC")
    List<EventoEntity> findEventosUpcoming();

    @Cacheable(value = "eventos", key = "'past'")
    @Query("SELECT e FROM EventoEntity e WHERE e.data < CURRENT_TIMESTAMP ORDER BY e.data DESC")
    List<EventoEntity> findEventosPast();

    @Cacheable(value = "eventos", key = "'byDataAfter:' + #data")
    List<EventoEntity> findByDataAfterOrderByDataAsc(LocalDateTime data);

    @Cacheable(value = "eventos", key = "'byDataBefore:' + #data")
    List<EventoEntity> findByDataBeforeOrderByDataDesc(LocalDateTime data);
}
