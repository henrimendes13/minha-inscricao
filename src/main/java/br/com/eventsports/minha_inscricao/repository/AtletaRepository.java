package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.enums.Genero;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtletaRepository extends JpaRepository<AtletaEntity, Long> {

    @Override
    @Cacheable(value = "atletas", key = "#id")
    @NonNull
    Optional<AtletaEntity> findById(@NonNull Long id);

    @Override
    @Cacheable(value = "atletas")
    @NonNull
    List<AtletaEntity> findAll();

    @Override
    @CachePut(value = "atletas", key = "#result.id")
    @NonNull
    <S extends AtletaEntity> S save(@NonNull S entity);

    @Override
    @CacheEvict(value = "atletas", key = "#id")
    void deleteById(@NonNull Long id);

    @Cacheable(value = "atletas", key = "'byCpf:' + #cpf")
    Optional<AtletaEntity> findByCpf(String cpf);

    @Cacheable(value = "atletas", key = "'byNome:' + #nome")
    List<AtletaEntity> findByNomeContainingIgnoreCase(String nome);

    @Cacheable(value = "atletas", key = "'byGenero:' + #genero")
    List<AtletaEntity> findByGenero(Genero genero);

    @Cacheable(value = "atletas", key = "'byEvento:' + #eventoId")
    List<AtletaEntity> findByEventoId(Long eventoId);

    @Cacheable(value = "atletas", key = "'byEquipe:' + #equipeId")
    List<AtletaEntity> findByEquipeId(Long equipeId);

    @Cacheable(value = "atletas", key = "'byInscricao:' + #inscricaoId")
    List<AtletaEntity> findByInscricaoId(Long inscricaoId);

    @Query("SELECT a FROM AtletaEntity a WHERE a.evento = :evento AND a.aceitaTermos = true")
    @Cacheable(value = "atletas", key = "'atletasAtivosEvento:' + #evento.id")
    List<AtletaEntity> findAtletasAtivosByEvento(@Param("evento") EventoEntity evento);

    @Query("SELECT a FROM AtletaEntity a WHERE a.equipe = :equipe")
    @Cacheable(value = "atletas", key = "'atletasEquipe:' + #equipe.id")
    List<AtletaEntity> findByEquipe(@Param("equipe") EquipeEntity equipe);

    @Query("SELECT a FROM AtletaEntity a WHERE a.dataNascimento BETWEEN :dataInicio AND :dataFim")
    @Cacheable(value = "atletas", key = "'byIdadeBetween:' + #dataInicio + ':' + #dataFim")
    List<AtletaEntity> findByIdadeBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COUNT(a) FROM AtletaEntity a WHERE a.evento.id = :eventoId AND a.aceitaTermos = true")
    long countAtletasAtivosByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT COUNT(a) FROM AtletaEntity a WHERE a.equipe.id = :equipeId")
    long countAtletasByEquipeId(@Param("equipeId") Long equipeId);

    @Query("SELECT a FROM AtletaEntity a WHERE a.aceitaTermos = true ORDER BY a.nome ASC")
    @Cacheable(value = "atletas", key = "'atletasAtivos'")
    List<AtletaEntity> findAtletasAtivos();

    @Query("SELECT a FROM AtletaEntity a WHERE a.aceitaTermos = false ORDER BY a.nome ASC")
    @Cacheable(value = "atletas", key = "'atletasInativos'")
    List<AtletaEntity> findAtletasInativos();

    @Query("SELECT a FROM AtletaEntity a WHERE a.emergenciaNome IS NOT NULL AND a.emergenciaTelefone IS NOT NULL")
    @Cacheable(value = "atletas", key = "'atletasComContatoEmergencia'")
    List<AtletaEntity> findAtletasComContatoEmergencia();

    boolean existsByCpf(String cpf);

    @CacheEvict(value = "atletas", allEntries = true)
    @Query("UPDATE AtletaEntity a SET a.aceitaTermos = :aceitaTermos WHERE a.id = :id")
    void updateAceitaTermos(@Param("id") Long id, @Param("aceitaTermos") Boolean aceitaTermos);
}
