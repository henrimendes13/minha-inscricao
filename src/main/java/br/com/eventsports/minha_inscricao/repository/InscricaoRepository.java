package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.InscricaoEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.CategoriaEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InscricaoRepository extends JpaRepository<InscricaoEntity, Long> {

    @Override
    @Cacheable(value = "inscricoes", key = "#id")
    @NonNull
    Optional<InscricaoEntity> findById(@NonNull Long id);

    @Override
    @Cacheable(value = "inscricoes")
    @NonNull
    List<InscricaoEntity> findAll();

    @Override
    @CachePut(value = "inscricoes", key = "#result.id")
    @NonNull
    <S extends InscricaoEntity> S save(@NonNull S entity);

    @Override
    @CacheEvict(value = "inscricoes", key = "#id")
    void deleteById(@NonNull Long id);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.evento.id = :eventoId")
    @Cacheable(value = "inscricoes", key = "'byEvento:' + #eventoId")
    List<InscricaoEntity> findByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.categoria.id = :categoriaId")
    @Cacheable(value = "inscricoes", key = "'byCategoria:' + #categoriaId")
    List<InscricaoEntity> findByCategoriaId(@Param("categoriaId") Long categoriaId);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.equipe.id = :equipeId")
    @Cacheable(value = "inscricoes", key = "'byEquipe:' + #equipeId")
    List<InscricaoEntity> findByEquipeId(@Param("equipeId") Long equipeId);

    @Cacheable(value = "inscricoes", key = "'byStatus:' + #status")
    List<InscricaoEntity> findByStatus(StatusInscricao status);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.evento = :evento")
    @Cacheable(value = "inscricoes", key = "'byEventoEntity:' + #evento.id")
    List<InscricaoEntity> findByEvento(@Param("evento") EventoEntity evento);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.categoria = :categoria")
    @Cacheable(value = "inscricoes", key = "'byCategoriaEntity:' + #categoria.id")
    List<InscricaoEntity> findByCategoria(@Param("categoria") CategoriaEntity categoria);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.equipe = :equipe")
    @Cacheable(value = "inscricoes", key = "'byEquipeEntity:' + #equipe.id")
    List<InscricaoEntity> findByEquipe(@Param("equipe") EquipeEntity equipe);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.atleta = :atleta")
    @Cacheable(value = "inscricoes", key = "'byAtleta:' + #atleta.id")
    List<InscricaoEntity> findByAtleta(@Param("atleta") UsuarioEntity atleta);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.evento.id = :eventoId AND i.status = :status")
    @Cacheable(value = "inscricoes", key = "'byEventoAndStatus:' + #eventoId + ':' + #status")
    List<InscricaoEntity> findByEventoIdAndStatus(@Param("eventoId") Long eventoId, @Param("status") StatusInscricao status);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.categoria.id = :categoriaId AND i.status = :status")
    @Cacheable(value = "inscricoes", key = "'byCategoriaAndStatus:' + #categoriaId + ':' + #status")
    List<InscricaoEntity> findByCategoriaIdAndStatus(@Param("categoriaId") Long categoriaId, @Param("status") StatusInscricao status);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.dataInscricao BETWEEN :inicio AND :fim ORDER BY i.dataInscricao ASC")
    @Cacheable(value = "inscricoes", key = "'byDataBetween:' + #inicio + ':' + #fim")
    List<InscricaoEntity> findByDataInscricaoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.status IN :statuses ORDER BY i.dataInscricao DESC")
    @Cacheable(value = "inscricoes", key = "'byStatusIn:' + #statuses")
    List<InscricaoEntity> findByStatusIn(@Param("statuses") List<StatusInscricao> statuses);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.codigoDesconto = :codigo")
    @Cacheable(value = "inscricoes", key = "'byCodigoDesconto:' + #codigo")
    List<InscricaoEntity> findByCodigoDesconto(@Param("codigo") String codigo);

    @Query("SELECT COUNT(i) FROM InscricaoEntity i WHERE i.evento.id = :eventoId AND i.status = :status")
    long countByEventoIdAndStatus(@Param("eventoId") Long eventoId, @Param("status") StatusInscricao status);

    @Query("SELECT COUNT(i) FROM InscricaoEntity i WHERE i.categoria.id = :categoriaId AND i.status = :status")
    long countByCategoriaIdAndStatus(@Param("categoriaId") Long categoriaId, @Param("status") StatusInscricao status);

    @Query("SELECT COUNT(i) FROM InscricaoEntity i WHERE i.evento.id = :eventoId")
    long countByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.status = 'CONFIRMADA' ORDER BY i.dataConfirmacao DESC")
    @Cacheable(value = "inscricoes", key = "'inscricoesConfirmadas'")
    List<InscricaoEntity> findInscricoesConfirmadas();

    @Query("SELECT i FROM InscricaoEntity i WHERE i.status = 'PENDENTE' ORDER BY i.dataInscricao ASC")
    @Cacheable(value = "inscricoes", key = "'inscricoesPendentes'")
    List<InscricaoEntity> findInscricoesPendentes();

    @Query("SELECT i FROM InscricaoEntity i WHERE i.status = 'CANCELADA' ORDER BY i.dataCancelamento DESC")
    @Cacheable(value = "inscricoes", key = "'inscricoesCanceladas'")
    List<InscricaoEntity> findInscricoesCanceladas();

    @Query("SELECT i FROM InscricaoEntity i WHERE i.valorDesconto IS NOT NULL AND i.valorDesconto > 0")
    @Cacheable(value = "inscricoes", key = "'inscricoesComDesconto'")
    List<InscricaoEntity> findInscricoesComDesconto();

    @CacheEvict(value = "inscricoes", allEntries = true)
    @Query("UPDATE InscricaoEntity i SET i.status = :status WHERE i.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") StatusInscricao status);

    @Query("SELECT COUNT(i) > 0 FROM InscricaoEntity i WHERE i.atleta.id = :atletaId AND i.evento.id = :eventoId")
    boolean existsByAtletaIdAndEventoId(@Param("atletaId") Long atletaId, @Param("eventoId") Long eventoId);
}
