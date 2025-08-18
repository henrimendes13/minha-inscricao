package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.inscricao.*;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;

import java.util.List;

public interface IInscricaoService {
    
    InscricaoResponseDTO findById(Long id);
    
    List<InscricaoSummaryDTO> findAll();
    
    InscricaoResponseDTO save(InscricaoCreateDTO inscricaoCreateDTO);
    
    InscricaoResponseDTO saveForEvento(InscricaoCreateDTO inscricaoCreateDTO, Long eventoId);
    
    InscricaoResponseDTO saveComEquipeForEvento(InscricaoComEquipeCreateDTO inscricaoComEquipeDTO, Long eventoId);
    
    InscricaoResponseDTO update(Long id, InscricaoUpdateDTO inscricaoUpdateDTO);
    
    void deleteById(Long id);
    
    List<InscricaoSummaryDTO> findByEventoId(Long eventoId);
    
    List<InscricaoSummaryDTO> findByCategoriaId(Long categoriaId);
    
    List<InscricaoSummaryDTO> findByEquipeId(Long equipeId);
    
    List<InscricaoSummaryDTO> findByStatus(StatusInscricao status);
    
    List<InscricaoSummaryDTO> findInscricoesConfirmadas();
    
    List<InscricaoSummaryDTO> findInscricoesPendentes();
    
    List<InscricaoSummaryDTO> findInscricoesCanceladas();
    
    InscricaoResponseDTO confirmar(Long id);
    
    InscricaoResponseDTO cancelar(Long id, String motivo);
    
    InscricaoResponseDTO colocarEmListaEspera(Long id);
    
    long countByEventoIdAndStatus(Long eventoId, StatusInscricao status);
    
    long countByCategoriaIdAndStatus(Long categoriaId, StatusInscricao status);
}
