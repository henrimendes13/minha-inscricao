package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.CategoriaEntity;
import br.com.eventsports.minha_inscricao.enums.TipoParticipacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<CategoriaEntity, Long> {

    /**
     * Busca categorias por evento
     */
    List<CategoriaEntity> findByEventoIdOrderByNomeAsc(Long eventoId);

    /**
     * Busca categorias ativas
     */
    List<CategoriaEntity> findByAtivaTrue();

    /**
     * Busca categorias ativas por evento
     */
    List<CategoriaEntity> findByEventoIdAndAtivaTrue(Long eventoId);

    /**
     * Busca categorias por tipo de participação
     */
    List<CategoriaEntity> findByTipoParticipacao(TipoParticipacao tipoParticipacao);

    /**
     * Busca categorias por evento e tipo de participação
     */
    List<CategoriaEntity> findByEventoIdAndTipoParticipacao(Long eventoId, TipoParticipacao tipoParticipacao);

    /**
     * Busca categorias por nome (case insensitive)
     */
    List<CategoriaEntity> findByNomeContainingIgnoreCase(String nome);

    /**
     * Verifica se existe categoria com o mesmo nome no evento
     */
    boolean existsByNomeAndEventoId(String nome, Long eventoId);

    /**
     * Verifica se existe outra categoria com o mesmo nome no evento (para update)
     */
    boolean existsByNomeAndEventoIdAndIdNot(String nome, Long eventoId, Long id);

    /**
     * Busca categorias com quantidade específica de atletas por equipe
     */
    List<CategoriaEntity> findByQuantidadeDeAtletasPorEquipe(Integer quantidade);

    /**
     * Busca categorias por evento ordenadas por tipo de participação e nome
     */
    @Query("SELECT c FROM CategoriaEntity c WHERE c.evento.id = :eventoId ORDER BY c.tipoParticipacao, c.nome")
    List<CategoriaEntity> findByEventoIdOrderByTipoParticipacaoAndNome(@Param("eventoId") Long eventoId);

    /**
     * Conta categorias ativas por evento
     */
    @Query("SELECT COUNT(c) FROM CategoriaEntity c WHERE c.evento.id = :eventoId AND c.ativa = true")
    long countAtivasByEventoId(@Param("eventoId") Long eventoId);

    /**
     * Busca categorias que podem receber inscrições (ativas e do evento ativo)
     */
    @Query("SELECT c FROM CategoriaEntity c WHERE c.evento.id = :eventoId AND c.ativa = true AND c.evento.ativo = true")
    List<CategoriaEntity> findCategoriasDisponiveis(@Param("eventoId") Long eventoId);

    /**
     * Busca categorias com inscrições ativas
     */
    @Query("SELECT DISTINCT c FROM CategoriaEntity c JOIN c.inscricoes i WHERE i.status IN ('PENDENTE', 'CONFIRMADA')")
    List<CategoriaEntity> findCategoriasComInscricoesAtivas();

    /**
     * Busca categorias por evento com contagem de inscrições
     */
    @Query("SELECT c, COUNT(i) as totalInscricoes FROM CategoriaEntity c " +
           "LEFT JOIN c.inscricoes i " +
           "WHERE c.evento.id = :eventoId " +
           "GROUP BY c.id " +
           "ORDER BY c.nome")
    List<Object[]> findByEventoIdWithInscricaoCount(@Param("eventoId") Long eventoId);
}
