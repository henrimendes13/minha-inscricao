package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.dto.workout.*;

import java.util.List;

public interface IWorkoutService {
    
    WorkoutResponseDTO findById(Long id);
    
    List<WorkoutSummaryDTO> findAll();
    
    WorkoutResponseDTO save(WorkoutCreateDTO workoutCreateDTO);
    
    WorkoutResponseDTO update(Long id, WorkoutUpdateDTO workoutUpdateDTO);
    
    void delete(Long id);
    
    List<WorkoutSummaryDTO> findByEventoId(Long eventoId);
    
    List<WorkoutSummaryDTO> findByEventoIdAndAtivo(Long eventoId, boolean ativo);
    
    List<WorkoutSummaryDTO> findByCategoriaId(Long categoriaId);
    
    List<WorkoutSummaryDTO> findByNome(String nome);
    
    WorkoutResponseDTO ativar(Long id);
    
    WorkoutResponseDTO desativar(Long id);
    
    WorkoutResponseDTO adicionarCategoria(Long workoutId, Long categoriaId);
    
    WorkoutResponseDTO removerCategoria(Long workoutId, Long categoriaId);
}
