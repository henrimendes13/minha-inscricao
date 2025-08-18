package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.categoria.*;
import br.com.eventsports.minha_inscricao.enums.TipoParticipacao;

import java.util.List;

public interface ICategoriaService {
    
    CategoriaResponseDTO findById(Long id);
    
    List<CategoriaSummaryDTO> findAll();
    
    CategoriaResponseDTO save(Long eventoId, CategoriaCreateDTO categoriaCreateDTO);
    
    CategoriaResponseDTO update(Long id, CategoriaUpdateDTO categoriaUpdateDTO);
    
    void deleteById(Long id);
    
    List<CategoriaSummaryDTO> findByEventoId(Long eventoId);
    
    List<CategoriaSummaryDTO> findCategoriasAtivas();
    
    List<CategoriaSummaryDTO> findCategoriasAtivasByEvento(Long eventoId);
    
    List<CategoriaSummaryDTO> findByTipoParticipacao(TipoParticipacao tipoParticipacao);
    
    List<CategoriaSummaryDTO> findByEventoIdAndTipoParticipacao(Long eventoId, TipoParticipacao tipoParticipacao);
    
    List<CategoriaSummaryDTO> findByNome(String nome);
    
    List<CategoriaSummaryDTO> findCategoriasDisponiveis(Long eventoId);
    
    CategoriaResponseDTO ativar(Long id);
    
    CategoriaResponseDTO desativar(Long id);
}
