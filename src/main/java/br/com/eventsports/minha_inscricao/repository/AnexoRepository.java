package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.AnexoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnexoRepository extends JpaRepository<AnexoEntity, Long> {
    
    /**
     * Busca todos os anexos de um evento
     */
    List<AnexoEntity> findByEventoIdAndAtivoTrue(Long eventoId);
    
    /**
     * Busca todos os anexos de um evento (incluindo inativos)
     */
    List<AnexoEntity> findByEventoId(Long eventoId);
    
    /**
     * Busca anexos por tipo MIME
     */
    List<AnexoEntity> findByTipoMimeAndAtivoTrue(String tipoMime);
    
    /**
     * Busca anexos por extensão
     */
    List<AnexoEntity> findByExtensaoAndAtivoTrue(String extensao);
    
    /**
     * Busca anexo por nome do arquivo
     */
    Optional<AnexoEntity> findByNomeArquivoAndEventoId(String nomeArquivo, Long eventoId);
    
    /**
     * Busca anexo por checksum MD5 (para evitar duplicatas)
     */
    Optional<AnexoEntity> findByChecksumMd5(String checksumMd5);
    
    /**
     * Busca anexos que contenham texto na descrição
     */
    @Query("SELECT a FROM AnexoEntity a WHERE a.descricao ILIKE %:texto% AND a.ativo = true")
    List<AnexoEntity> findByDescricaoContaining(@Param("texto") String texto);
    
    /**
     * Conta anexos de um evento
     */
    long countByEventoIdAndAtivoTrue(Long eventoId);
    
    /**
     * Soma total de bytes dos anexos de um evento
     */
    @Query("SELECT COALESCE(SUM(a.tamanhoBytes), 0) FROM AnexoEntity a WHERE a.evento.id = :eventoId AND a.ativo = true")
    Long sumTamanhoByEventoId(@Param("eventoId") Long eventoId);
    
    /**
     * Busca anexos maiores que determinado tamanho
     */
    @Query("SELECT a FROM AnexoEntity a WHERE a.tamanhoBytes > :tamanho AND a.ativo = true")
    List<AnexoEntity> findByTamanhoMaiorQue(@Param("tamanho") Long tamanho);
    
    /**
     * Busca anexos por tipo (imagem, documento, etc.)
     */
    @Query("SELECT a FROM AnexoEntity a WHERE a.tipoMime LIKE :tipoBase% AND a.ativo = true")
    List<AnexoEntity> findByTipoBase(@Param("tipoBase") String tipoBase);
}
