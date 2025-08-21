package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
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
     * Busca usuários por nome (contendo)
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<UsuarioEntity> findByNomeContainingIgnoreCase(@Param("nome") String nome, Pageable pageable);

    /**
     * Busca usuários ativos por nome (contendo)
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE LOWER(u.nome) LIKE LOWER(CONCAT('%', :nome, '%')) AND u.ativo = true")
    Page<UsuarioEntity> findByNomeContainingIgnoreCaseAndAtivoTrue(@Param("nome") String nome, Pageable pageable);

    /**
     * Busca usuários que fizeram login após determinada data
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE u.ultimoLogin >= :dataInicio")
    List<UsuarioEntity> findUsuariosComLoginRecente(@Param("dataInicio") LocalDateTime dataInicio);



    /**
     * Busca usuários criados em um período
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE u.createdAt BETWEEN :inicio AND :fim")
    List<UsuarioEntity> findUsuariosCriadosNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
