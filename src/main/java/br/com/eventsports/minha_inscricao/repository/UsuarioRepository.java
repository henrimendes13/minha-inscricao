package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    /**
     * Busca usuário por email
     */
    Optional<UsuarioEntity> findByEmail(String email);

    /**
     * Verifica se existe usuário com o email informado
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuários ativos
     */
    List<UsuarioEntity> findByAtivoTrue();

    /**
     * Busca usuários ativos com paginação
     */
    Page<UsuarioEntity> findByAtivoTrue(Pageable pageable);

    /**
     * Busca usuários por tipo
     */
    List<UsuarioEntity> findByTipo(TipoUsuario tipo);

    /**
     * Busca usuários ativos por tipo
     */
    List<UsuarioEntity> findByTipoAndAtivoTrue(TipoUsuario tipo);

    /**
     * Busca usuários por nome (contendo)
     */
    Page<UsuarioEntity> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    /**
     * Busca usuários ativos por nome (contendo)
     */
    Page<UsuarioEntity> findByNomeContainingIgnoreCaseAndAtivoTrue(String nome, Pageable pageable);

    /**
     * Busca usuários que fizeram login após determinada data
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE u.ultimoLogin >= :dataInicio")
    List<UsuarioEntity> findUsuariosComLoginRecente(@Param("dataInicio") LocalDateTime dataInicio);

    /**
     * Conta usuários por tipo
     */
    @Query("SELECT COUNT(u) FROM UsuarioEntity u WHERE u.tipo = :tipo AND u.ativo = true")
    Long countByTipoAndAtivoTrue(@Param("tipo") TipoUsuario tipo);

    /**
     * Busca usuários por tipo com paginação
     */
    Page<UsuarioEntity> findByTipoAndAtivoTrue(TipoUsuario tipo, Pageable pageable);

    /**
     * Busca usuários criados em um período
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE u.createdAt BETWEEN :inicio AND :fim")
    List<UsuarioEntity> findUsuariosCriadosNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
