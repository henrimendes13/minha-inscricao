package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.InscricaoEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.CategoriaEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
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
    @NonNull
    Optional<InscricaoEntity> findById(@NonNull Long id);

    @Override
    @NonNull
    List<InscricaoEntity> findAll();

    @Override
    @NonNull
    <S extends InscricaoEntity> S save(@NonNull S entity);

    @Override
    void deleteById(@NonNull Long id);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.evento.id = :eventoId")
    List<InscricaoEntity> findByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.categoria.id = :categoriaId")
    List<InscricaoEntity> findByCategoriaId(@Param("categoriaId") Long categoriaId);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.equipe.id = :equipeId")
    List<InscricaoEntity> findByEquipeId(@Param("equipeId") Long equipeId);

    List<InscricaoEntity> findByStatus(StatusInscricao status);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.evento = :evento")
    List<InscricaoEntity> findByEvento(@Param("evento") EventoEntity evento);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.categoria = :categoria")
    List<InscricaoEntity> findByCategoria(@Param("categoria") CategoriaEntity categoria);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.equipe = :equipe")
    List<InscricaoEntity> findByEquipe(@Param("equipe") EquipeEntity equipe);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.atleta = :atleta")
    List<InscricaoEntity> findByAtleta(@Param("atleta") AtletaEntity atleta);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.usuarioInscricao = :usuario")
    List<InscricaoEntity> findByUsuarioInscricao(@Param("usuario") UsuarioEntity usuario);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.evento.id = :eventoId AND i.status = :status")
    List<InscricaoEntity> findByEventoIdAndStatus(@Param("eventoId") Long eventoId, @Param("status") StatusInscricao status);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.categoria.id = :categoriaId AND i.status = :status")
    List<InscricaoEntity> findByCategoriaIdAndStatus(@Param("categoriaId") Long categoriaId, @Param("status") StatusInscricao status);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.dataInscricao BETWEEN :inicio AND :fim ORDER BY i.dataInscricao ASC")
    List<InscricaoEntity> findByDataInscricaoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.status IN :statuses ORDER BY i.dataInscricao DESC")
    List<InscricaoEntity> findByStatusIn(@Param("statuses") List<StatusInscricao> statuses);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.codigoDesconto = :codigo")
    List<InscricaoEntity> findByCodigoDesconto(@Param("codigo") String codigo);

    @Query("SELECT COUNT(i) FROM InscricaoEntity i WHERE i.evento.id = :eventoId AND i.status = :status")
    long countByEventoIdAndStatus(@Param("eventoId") Long eventoId, @Param("status") StatusInscricao status);

    @Query("SELECT COUNT(i) FROM InscricaoEntity i WHERE i.categoria.id = :categoriaId AND i.status = :status")
    long countByCategoriaIdAndStatus(@Param("categoriaId") Long categoriaId, @Param("status") StatusInscricao status);

    @Query("SELECT COUNT(i) FROM InscricaoEntity i WHERE i.evento.id = :eventoId")
    long countByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT i FROM InscricaoEntity i WHERE i.status = 'CONFIRMADA' ORDER BY i.dataConfirmacao DESC")
    List<InscricaoEntity> findInscricoesConfirmadas();

    @Query("SELECT i FROM InscricaoEntity i WHERE i.status = 'PENDENTE' ORDER BY i.dataInscricao ASC")
    List<InscricaoEntity> findInscricoesPendentes();

    @Query("SELECT i FROM InscricaoEntity i WHERE i.status = 'CANCELADA' ORDER BY i.dataCancelamento DESC")
    List<InscricaoEntity> findInscricoesCanceladas();

    @Query("SELECT i FROM InscricaoEntity i WHERE i.valorDesconto IS NOT NULL AND i.valorDesconto > 0")
    List<InscricaoEntity> findInscricoesComDesconto();

    @Query("UPDATE InscricaoEntity i SET i.status = :status WHERE i.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") StatusInscricao status);

    @Query("SELECT COUNT(i) > 0 FROM InscricaoEntity i WHERE i.atleta.id = :atletaId AND i.evento.id = :eventoId")
    boolean existsByAtletaIdAndEventoId(@Param("atletaId") Long atletaId, @Param("eventoId") Long eventoId);
}
