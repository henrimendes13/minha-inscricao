package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.OrganizadorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizadorRepository extends JpaRepository<OrganizadorEntity, Long> {

    /**
     * Busca organizador por ID do usuário
     */
    @Query("SELECT o FROM OrganizadorEntity o WHERE o.usuario.id = :usuarioId")
    Optional<OrganizadorEntity> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Verifica se existe organizador para o usuário
     */
    @Query("SELECT COUNT(o) > 0 FROM OrganizadorEntity o WHERE o.usuario.id = :usuarioId")
    boolean existsByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Busca organizador por CNPJ
     */
    Optional<OrganizadorEntity> findByCnpj(String cnpj);

    /**
     * Verifica se existe CNPJ já cadastrado
     */
    boolean existsByCnpj(String cnpj);

    /**
     * Busca organizadores verificados
     */
    List<OrganizadorEntity> findByVerificadoTrue();

    /**
     * Busca organizadores verificados com paginação
     */
    Page<OrganizadorEntity> findByVerificadoTrue(Pageable pageable);

    /**
     * Busca organizadores por nome da empresa (contendo)
     */
    @Query("SELECT o FROM OrganizadorEntity o WHERE LOWER(o.nomeEmpresa) LIKE LOWER(CONCAT('%', :nomeEmpresa, '%'))")
    Page<OrganizadorEntity> findByNomeEmpresaContainingIgnoreCase(@Param("nomeEmpresa") String nomeEmpresa, Pageable pageable);

    /**
     * Busca organizadores verificados por nome da empresa
     */
    @Query("SELECT o FROM OrganizadorEntity o WHERE LOWER(o.nomeEmpresa) LIKE LOWER(CONCAT('%', :nomeEmpresa, '%')) AND o.verificado = true")
    Page<OrganizadorEntity> findByNomeEmpresaContainingIgnoreCaseAndVerificadoTrue(@Param("nomeEmpresa") String nomeEmpresa, Pageable pageable);

    /**
     * Busca organizadores com usuário ativo
     */
    @Query("SELECT o FROM OrganizadorEntity o WHERE o.usuario.ativo = true")
    List<OrganizadorEntity> findComUsuarioAtivo();

    /**
     * Busca organizadores que podem organizar eventos
     */
    @Query("SELECT o FROM OrganizadorEntity o WHERE o.usuario.ativo = true AND o.verificado = true")
    List<OrganizadorEntity> findQuePodemOrganizarEventos();

    /**
     * Busca organizadores que podem organizar eventos com paginação
     */
    @Query("SELECT o FROM OrganizadorEntity o WHERE o.usuario.ativo = true AND o.verificado = true")
    Page<OrganizadorEntity> findQuePodemOrganizarEventos(Pageable pageable);

    /**
     * Conta organizadores verificados
     */
    @Query("SELECT COUNT(o) FROM OrganizadorEntity o WHERE o.verificado = true")
    Long countVerificados();

    /**
     * Busca organizadores por cidade (extraindo da coluna endereço)
     */
    @Query("SELECT o FROM OrganizadorEntity o WHERE UPPER(o.endereco) LIKE UPPER(CONCAT('%', :cidade, '%'))")
    List<OrganizadorEntity> findByCidade(@Param("cidade") String cidade);

    /**
     * Busca organizadores com eventos
     * Usando junção com EventoEntity via usuario.id
     */
    @Query("SELECT DISTINCT o FROM OrganizadorEntity o JOIN EventoEntity e ON e.organizador.id = o.usuario.id")
    List<OrganizadorEntity> findComEventos();

    /**
     * Busca organizadores por quantidade mínima de eventos
     * Usando junção com EventoEntity via usuario.id
     */
    @Query("SELECT o FROM OrganizadorEntity o WHERE (SELECT COUNT(e) FROM EventoEntity e WHERE e.organizador.id = o.usuario.id) >= :minEventos")
    List<OrganizadorEntity> findComMinimoEventos(@Param("minEventos") int minEventos);
}
