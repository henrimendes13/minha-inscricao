package br.com.eventsports.minha_inscricao.repository;

import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.enums.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    @NonNull
    Optional<AtletaEntity> findById(@NonNull Long id);

    @Override
    @NonNull
    List<AtletaEntity> findAll();

    @Override
    @NonNull
    <S extends AtletaEntity> S save(@NonNull S entity);

    @Override
    void deleteById(@NonNull Long id);

    Optional<AtletaEntity> findByCpf(String cpf);

    List<AtletaEntity> findByNomeContainingIgnoreCase(String nome);

    List<AtletaEntity> findByGenero(Genero genero);

    @Query("SELECT a FROM AtletaEntity a WHERE a.evento.id = :eventoId")
    List<AtletaEntity> findByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT a FROM AtletaEntity a WHERE a.evento.id = :eventoId AND a.categoria.id = :categoriaId")
    List<AtletaEntity> findByEventoIdAndCategoriaId(@Param("eventoId") Long eventoId, @Param("categoriaId") Long categoriaId);

    @Query("SELECT a FROM AtletaEntity a WHERE a.equipe.id = :equipeId")
    List<AtletaEntity> findByEquipeId(@Param("equipeId") Long equipeId);


    @Query("SELECT a FROM AtletaEntity a WHERE a.evento = :evento AND a.aceitaTermos = true")
    List<AtletaEntity> findAtletasAtivosByEvento(@Param("evento") EventoEntity evento);

    @Query("SELECT a FROM AtletaEntity a WHERE a.equipe = :equipe")
    List<AtletaEntity> findByEquipe(@Param("equipe") EquipeEntity equipe);

    @Query("SELECT a FROM AtletaEntity a WHERE a.dataNascimento BETWEEN :dataInicio AND :dataFim")
    List<AtletaEntity> findByIdadeBetween(@Param("dataInicio") LocalDate dataInicio, @Param("dataFim") LocalDate dataFim);

    @Query("SELECT COUNT(a) FROM AtletaEntity a WHERE a.evento.id = :eventoId AND a.aceitaTermos = true")
    long countAtletasAtivosByEventoId(@Param("eventoId") Long eventoId);

    @Query("SELECT COUNT(a) FROM AtletaEntity a WHERE a.equipe.id = :equipeId")
    long countAtletasByEquipeId(@Param("equipeId") Long equipeId);

    @Query("SELECT a FROM AtletaEntity a WHERE a.aceitaTermos = true ORDER BY a.nome ASC")
    List<AtletaEntity> findAtletasAtivos();

    @Query("SELECT a FROM AtletaEntity a WHERE a.aceitaTermos = false ORDER BY a.nome ASC")
    List<AtletaEntity> findAtletasInativos();

    @Query("SELECT a FROM AtletaEntity a WHERE a.emergenciaNome IS NOT NULL AND a.emergenciaTelefone IS NOT NULL")
    List<AtletaEntity> findAtletasComContatoEmergencia();

    boolean existsByCpf(String cpf);

    @Modifying
    @Query("UPDATE AtletaEntity a SET a.aceitaTermos = :aceitaTermos WHERE a.id = :id")
    void updateAceitaTermos(@Param("id") Long id, @Param("aceitaTermos") Boolean aceitaTermos);

    @Modifying
    @Query("UPDATE AtletaEntity a SET a.pontuacaoTotal = :pontuacao WHERE a.id = :id")
    void updatePontuacaoTotal(@Param("id") Long id, @Param("pontuacao") Integer pontuacao);
}
