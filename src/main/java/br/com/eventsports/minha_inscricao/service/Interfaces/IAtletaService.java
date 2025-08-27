package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.atleta.*;
import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.enums.Genero;

import java.util.List;
import java.util.Optional;

public interface IAtletaService {
    
    AtletaResponseDTO findById(Long id);
    
    Optional<AtletaEntity> findEntityById(Long id);
    
    List<AtletaSummaryDTO> findAll();
    
    AtletaResponseDTO save(AtletaCreateDTO atletaCreateDTO);
    
    AtletaResponseDTO saveForInscricao(AtletaCreateDTO atletaCreateDTO, Long eventoId, Long equipeId);
    
    AtletaResponseDTO criarAtletaParaInscricao(Long eventoId, AtletaInscricaoDTO atletaInscricaoDTO);
    
    AtletaResponseDTO saveForEvento(AtletaCreateDTO atletaCreateDTO, Long eventoId);
    
    AtletaResponseDTO update(Long id, AtletaUpdateDTO atletaUpdateDTO);
    
    void deleteById(Long id);
    
    Optional<AtletaResponseDTO> findByCpf(String cpf);
    
    Optional<AtletaEntity> findEntityByCpf(String cpf);
    
    List<AtletaSummaryDTO> findByNome(String nome);
    
    List<AtletaSummaryDTO> findByGenero(Genero genero);
    
    List<AtletaSummaryDTO> findByEventoId(Long eventoId);
    
    List<AtletaSummaryDTO> findByEventoIdAndCategoriaId(Long eventoId, Long categoriaId);
    
    List<AtletaSummaryDTO> findByEquipeId(Long equipeId);
    
    List<AtletaSummaryDTO> findAtletasAtivos();
    
    List<AtletaSummaryDTO> findAtletasComContatoEmergencia();
    
    void updateAceitaTermos(Long id, Boolean aceitaTermos);
    
    boolean existsByCpf(String cpf);
    
    long countAtletasAtivosByEventoId(Long eventoId);
    
    long countAtletasByEquipeId(Long equipeId);
}
