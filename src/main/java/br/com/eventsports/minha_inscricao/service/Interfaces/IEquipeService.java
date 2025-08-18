package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.equipe.*;

import java.util.List;

public interface IEquipeService {
    
    EquipeResponseDTO findById(Long id);
    
    List<EquipeSummaryDTO> findAll();
    
    EquipeResponseDTO save(EquipeCreateDTO equipeCreateDTO);
    
    EquipeResponseDTO criarEquipeParaInscricao(Long eventoId, EquipeInscricaoDTO equipeInscricaoDTO);
    
    EquipeResponseDTO criarEquipeParaInscricao(Long eventoId, EquipeInscricaoDTO equipeInscricaoDTO, Long usuarioLogadoId);
    
    EquipeResponseDTO update(Long id, EquipeUpdateDTO equipeUpdateDTO);
    
    void deleteById(Long id);
    
    List<EquipeSummaryDTO> findByNome(String nome);
    
    List<EquipeSummaryDTO> findByEventoId(Long eventoId);
    
    List<EquipeSummaryDTO> findByCategoriaId(Long categoriaId);
    
    List<EquipeSummaryDTO> findEquipesAtivas();
    
    List<EquipeSummaryDTO> findEquipesCompletasByEvento(Long eventoId);
    
    List<EquipeSummaryDTO> findEquipesByAtleta(Long atletaId);
}
