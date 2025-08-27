package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.categoria.CategoriaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.workout.*;
import br.com.eventsports.minha_inscricao.entity.CategoriaEntity;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.WorkoutEntity;
import br.com.eventsports.minha_inscricao.repository.CategoriaRepository;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.repository.WorkoutRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IWorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class WorkoutService implements IWorkoutService {

    private final WorkoutRepository workoutRepository;
    private final EventoRepository eventoRepository;
    private final CategoriaRepository categoriaRepository;

    @Cacheable(value = "workouts", key = "#id")
    @Transactional(readOnly = true)
    public WorkoutResponseDTO findById(Long id) {
        WorkoutEntity workout = workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout não encontrado com ID: " + id));
        return convertToResponseDTO(workout);
    }

    @Cacheable(value = "workouts", key = "'all'")
    @Transactional(readOnly = true)
    public List<WorkoutSummaryDTO> findAll() {
        List<WorkoutEntity> workouts = workoutRepository.findAll();
        return workouts.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CachePut(value = "workouts", key = "#result.id")
    @CacheEvict(value = "workouts", key = "'all'")
    public WorkoutResponseDTO save(WorkoutCreateDTO workoutCreateDTO) {
        validateWorkoutData(workoutCreateDTO);
        WorkoutEntity workout = convertCreateDTOToEntity(workoutCreateDTO);
        WorkoutEntity savedWorkout = workoutRepository.save(workout);
        return convertToResponseDTO(savedWorkout);
    }

    @CachePut(value = "workouts", key = "#id")
    @CacheEvict(value = "workouts", key = "'all'")
    public WorkoutResponseDTO update(Long id, WorkoutUpdateDTO workoutUpdateDTO) {
        WorkoutEntity existingWorkout = workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout não encontrado com ID: " + id));

        validateWorkoutUpdateData(id, workoutUpdateDTO);
        updateWorkoutFromDTO(existingWorkout, workoutUpdateDTO);
        WorkoutEntity updatedWorkout = workoutRepository.save(existingWorkout);
        return convertToResponseDTO(updatedWorkout);
    }

    @CacheEvict(value = "workouts", allEntries = true)
    public void delete(Long id) {
        WorkoutEntity workout = workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout não encontrado com ID: " + id));
        workoutRepository.delete(workout);
    }

    @Transactional(readOnly = true)
    public List<WorkoutSummaryDTO> findByEventoId(Long eventoId) {
        List<WorkoutEntity> workouts = workoutRepository.findByEventoIdOrderByNomeAsc(eventoId);
        return workouts.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkoutSummaryDTO> findByEventoIdAndAtivo(Long eventoId, boolean ativo) {
        List<WorkoutEntity> workouts = ativo 
            ? workoutRepository.findByEventoIdAndAtivoTrue(eventoId)
            : workoutRepository.findByEventoIdOrderByNomeAsc(eventoId);
        return workouts.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkoutSummaryDTO> findByCategoriaId(Long categoriaId) {
        List<WorkoutEntity> workouts = workoutRepository.findByCategoriaId(categoriaId);
        return workouts.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkoutSummaryDTO> findByNome(String nome) {
        List<WorkoutEntity> workouts = workoutRepository.findByNomeContainingIgnoreCase(nome);
        return workouts.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "workouts", allEntries = true)
    public WorkoutResponseDTO ativar(Long id) {
        WorkoutEntity workout = workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout não encontrado com ID: " + id));
        workout.ativar();
        WorkoutEntity updatedWorkout = workoutRepository.save(workout);
        return convertToResponseDTO(updatedWorkout);
    }

    @CacheEvict(value = "workouts", allEntries = true)
    public WorkoutResponseDTO desativar(Long id) {
        WorkoutEntity workout = workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout não encontrado com ID: " + id));
        workout.desativar();
        WorkoutEntity updatedWorkout = workoutRepository.save(workout);
        return convertToResponseDTO(updatedWorkout);
    }


    @CacheEvict(value = "workouts", allEntries = true)
    public WorkoutResponseDTO removerCategoria(Long workoutId, Long categoriaId) {
        WorkoutEntity workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout não encontrado com ID: " + workoutId));
        
        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + categoriaId));

        workout.removerCategoria(categoria);
        WorkoutEntity updatedWorkout = workoutRepository.save(workout);
        return convertToResponseDTO(updatedWorkout);
    }

    // Métodos de conversão e validação

    private void validateWorkoutData(WorkoutCreateDTO workoutCreateDTO) {
        // Verificar se o evento existe
        eventoRepository.findById(workoutCreateDTO.getEventoId())
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + workoutCreateDTO.getEventoId()));

        // Verificar se já existe workout com mesmo nome no evento
        if (workoutRepository.existsByNomeAndEventoId(workoutCreateDTO.getNome(), workoutCreateDTO.getEventoId())) {
            throw new RuntimeException("Já existe um workout com o nome '" + workoutCreateDTO.getNome() + "' neste evento");
        }

        // Validar categorias se fornecidas
        if (workoutCreateDTO.getCategoriasIds() != null && !workoutCreateDTO.getCategoriasIds().isEmpty()) {
            List<CategoriaEntity> categorias = categoriaRepository.findAllById(workoutCreateDTO.getCategoriasIds());
            
            if (categorias.size() != workoutCreateDTO.getCategoriasIds().size()) {
                throw new RuntimeException("Uma ou mais categorias não foram encontradas");
            }

            // Verificar se todas as categorias pertencem ao mesmo evento
            boolean todasDoMesmoEvento = categorias.stream()
                    .allMatch(categoria -> categoria.getEvento().getId().equals(workoutCreateDTO.getEventoId()));
            
            if (!todasDoMesmoEvento) {
                throw new RuntimeException("Todas as categorias devem pertencer ao mesmo evento do workout");
            }
        }
    }

    private void validateWorkoutUpdateData(Long workoutId, WorkoutUpdateDTO workoutUpdateDTO) {
        // Verificar nome duplicado se fornecido
        if (workoutUpdateDTO.getNome() != null && !workoutUpdateDTO.getNome().trim().isEmpty()) {
            WorkoutEntity existingWorkout = workoutRepository.findById(workoutId).orElseThrow();
            
            if (workoutRepository.existsByNomeAndEventoIdAndIdNot(
                    workoutUpdateDTO.getNome(), 
                    existingWorkout.getEvento().getId(), 
                    workoutId)) {
                throw new RuntimeException("Já existe outro workout com o nome '" + workoutUpdateDTO.getNome() + "' neste evento");
            }
        }

        // Validar categorias se fornecidas
        if (workoutUpdateDTO.getCategoriasIds() != null) {
            if (!workoutUpdateDTO.getCategoriasIds().isEmpty()) {
                List<CategoriaEntity> categorias = categoriaRepository.findAllById(workoutUpdateDTO.getCategoriasIds());
                
                if (categorias.size() != workoutUpdateDTO.getCategoriasIds().size()) {
                    throw new RuntimeException("Uma ou mais categorias não foram encontradas");
                }

                // Verificar se todas as categorias pertencem ao mesmo evento
                WorkoutEntity existingWorkout = workoutRepository.findById(workoutId).orElseThrow();
                boolean todasDoMesmoEvento = categorias.stream()
                        .allMatch(categoria -> categoria.getEvento().getId().equals(existingWorkout.getEvento().getId()));
                
                if (!todasDoMesmoEvento) {
                    throw new RuntimeException("Todas as categorias devem pertencer ao mesmo evento do workout");
                }
            }
        }
    }

    private WorkoutEntity convertCreateDTOToEntity(WorkoutCreateDTO workoutCreateDTO) {
        EventoEntity evento = eventoRepository.findById(workoutCreateDTO.getEventoId()).orElseThrow();

        WorkoutEntity workout = WorkoutEntity.builder()
                .nome(workoutCreateDTO.getNome())
                .descricao(workoutCreateDTO.getDescricao())
                .tipo(workoutCreateDTO.getTipo())
                .evento(evento)
                .ativo(workoutCreateDTO.getAtivo() != null ? workoutCreateDTO.getAtivo() : true)
                .categorias(new ArrayList<>())
                .build();


        // Adicionar categorias se fornecidas
        if (workoutCreateDTO.getCategoriasIds() != null && !workoutCreateDTO.getCategoriasIds().isEmpty()) {
            List<CategoriaEntity> categorias = categoriaRepository.findAllById(workoutCreateDTO.getCategoriasIds());
            workout.setCategorias(categorias);
        }

        return workout;
    }

    private void updateWorkoutFromDTO(WorkoutEntity workout, WorkoutUpdateDTO workoutUpdateDTO) {
        if (workoutUpdateDTO.getNome() != null && !workoutUpdateDTO.getNome().trim().isEmpty()) {
            workout.setNome(workoutUpdateDTO.getNome());
        }

        if (workoutUpdateDTO.getDescricao() != null) {
            workout.setDescricao(workoutUpdateDTO.getDescricao());
        }

        if (workoutUpdateDTO.getTipo() != null) {
            workout.setTipo(workoutUpdateDTO.getTipo());
        }

        if (workoutUpdateDTO.getAtivo() != null) {
            workout.setAtivo(workoutUpdateDTO.getAtivo());
        }


        // Atualizar categorias se fornecidas
        if (workoutUpdateDTO.getCategoriasIds() != null) {
            // Limpar categorias existentes
            workout.getCategorias().clear();
            
            if (!workoutUpdateDTO.getCategoriasIds().isEmpty()) {
                List<CategoriaEntity> novasCategorias = categoriaRepository.findAllById(workoutUpdateDTO.getCategoriasIds());
                workout.setCategorias(novasCategorias);
            }
        }
    }

    private WorkoutResponseDTO convertToResponseDTO(WorkoutEntity workout) {
        return WorkoutResponseDTO.builder()
                .id(workout.getId())
                .nome(workout.getNome())
                .descricao(workout.getDescricao())
                .tipo(workout.getTipo())
                .ativo(workout.getAtivo())
                .evento(convertEventoToSummaryDTO(workout.getEvento()))
                .categorias(workout.getCategorias().stream()
                        .map(this::convertCategoriaToSummaryDTO)
                        .collect(Collectors.toList()))
                .quantidadeCategorias(workout.getQuantidadeCategorias())
                .nomesCategorias(workout.getNomesCategorias())
                .unidadeMedida(workout.getUnidadeMedida())
                .createdAt(workout.getCreatedAt())
                .updatedAt(workout.getUpdatedAt())
                .build();
    }

    private WorkoutSummaryDTO convertToSummaryDTO(WorkoutEntity workout) {
        return WorkoutSummaryDTO.builder()
                .id(workout.getId())
                .nome(workout.getNome())
                .tipo(workout.getTipo())
                .ativo(workout.getAtivo())
                .quantidadeCategorias(workout.getQuantidadeCategorias())
                .nomesCategorias(workout.getNomesCategorias())
                .nomeEvento(workout.getNomeEvento())
                .unidadeMedida(workout.getUnidadeMedida())
                .build();
    }

    private EventoSummaryDTO convertEventoToSummaryDTO(EventoEntity evento) {
        return EventoSummaryDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .build();
    }

    private CategoriaSummaryDTO convertCategoriaToSummaryDTO(CategoriaEntity categoria) {
        return CategoriaSummaryDTO.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .ativa(categoria.getAtiva())
                .build();
    }

}
