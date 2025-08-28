package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.leaderboard.*;
import br.com.eventsports.minha_inscricao.entity.*;
import br.com.eventsports.minha_inscricao.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service especializado para gerenciar resultados de workouts
 * Funciona como uma camada de abstração sobre LeaderboardService
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutResultService {

    private final LeaderboardService leaderboardService;
    private final WorkoutRepository workoutRepository;
    private final CategoriaRepository categoriaRepository;
    private final EquipeRepository equipeRepository;
    private final LeaderboardRepository leaderboardRepository;
    private final EventoRepository eventoRepository;
    private final PontuacaoService pontuacaoService;


    /**
     * Atualiza resultado de uma equipe específica em um workout
     */
    @Transactional
    public LeaderboardResponseDTO atualizarResultadoEquipe(Long workoutId, Long equipeId, 
                                                          Object resultadoValor, Boolean finalizado) {
        // Buscar o registro de leaderboard existente
        LeaderboardEntity leaderboard = leaderboardRepository.findByWorkoutIdAndEquipeId(workoutId, equipeId)
                .orElseThrow(() -> new RuntimeException("Resultado não encontrado para esta equipe neste workout"));

        // Criar DTO de atualização baseado no tipo do workout
        LeaderboardResultadoUpdateDTO.LeaderboardResultadoUpdateDTOBuilder dtoBuilder = 
                LeaderboardResultadoUpdateDTO.builder();

        switch (leaderboard.getWorkout().getTipo()) {
            case REPS:
                if (resultadoValor instanceof Integer) {
                    dtoBuilder.resultadoReps((Integer) resultadoValor);
                } else if (resultadoValor instanceof String) {
                    dtoBuilder.resultadoReps(Integer.valueOf((String) resultadoValor));
                }
                break;
            case PESO:
                if (resultadoValor instanceof Double) {
                    dtoBuilder.resultadoPeso((Double) resultadoValor);
                } else if (resultadoValor instanceof String) {
                    dtoBuilder.resultadoPeso(Double.valueOf((String) resultadoValor));
                }
                break;
            case TEMPO:
                if (resultadoValor instanceof String) {
                    dtoBuilder.resultadoTempo((String) resultadoValor);
                }
                break;
        }

        if (finalizado != null) {
            dtoBuilder.finalizado(finalizado);
        }

        LeaderboardResultadoUpdateDTO dto = dtoBuilder.build();
        LeaderboardResponseDTO resultado = leaderboardService.atualizarLeaderboardResultado(leaderboard.getId(), dto);
        
        // Recalcular posições do workout
        leaderboardService.calcularRankingWorkout(leaderboard.getCategoria().getId(), leaderboard.getWorkout().getId());
        
        // Recalcular pontuações totais após atualizar posições
        pontuacaoService.recalcularTodasPontuacoesPorCategoria(leaderboard.getCategoria().getId());
        
        return resultado;
    }

    /**
     * Atualiza resultado de um atleta específico em um workout
     */
    @Transactional
    public LeaderboardResponseDTO atualizarResultadoAtleta(Long workoutId, Long atletaId, 
                                                          Object resultadoValor, Boolean finalizado) {
        // Buscar o registro de leaderboard existente
        LeaderboardEntity leaderboard = leaderboardRepository.findByWorkoutIdAndAtletaId(workoutId, atletaId)
                .orElseThrow(() -> new RuntimeException("Resultado não encontrado para este atleta neste workout"));

        // Criar DTO de atualização baseado no tipo do workout
        LeaderboardResultadoUpdateDTO.LeaderboardResultadoUpdateDTOBuilder dtoBuilder = 
                LeaderboardResultadoUpdateDTO.builder();

        switch (leaderboard.getWorkout().getTipo()) {
            case REPS:
                if (resultadoValor instanceof Integer) {
                    dtoBuilder.resultadoReps((Integer) resultadoValor);
                } else if (resultadoValor instanceof String) {
                    dtoBuilder.resultadoReps(Integer.valueOf((String) resultadoValor));
                }
                break;
            case PESO:
                if (resultadoValor instanceof Double) {
                    dtoBuilder.resultadoPeso((Double) resultadoValor);
                } else if (resultadoValor instanceof String) {
                    dtoBuilder.resultadoPeso(Double.valueOf((String) resultadoValor));
                }
                break;
            case TEMPO:
                if (resultadoValor instanceof String) {
                    dtoBuilder.resultadoTempo((String) resultadoValor);
                }
                break;
        }

        if (finalizado != null) {
            dtoBuilder.finalizado(finalizado);
        }

        LeaderboardResultadoUpdateDTO dto = dtoBuilder.build();
        LeaderboardResponseDTO resultado = leaderboardService.atualizarLeaderboardResultado(leaderboard.getId(), dto);
        
        // Recalcular posições do workout
        leaderboardService.calcularRankingWorkout(leaderboard.getCategoria().getId(), leaderboard.getWorkout().getId());
        
        // Recalcular pontuações totais após atualizar posições
        pontuacaoService.recalcularTodasPontuacoesPorCategoria(leaderboard.getCategoria().getId());
        
        return resultado;
    }


    /**
     * Busca todos os resultados de um workout
     */
    public List<LeaderboardSummaryDTO> getResultadosWorkout(Long categoriaId, Long workoutId) {
        return leaderboardService.getLeaderboardWorkout(categoriaId, workoutId);
    }

    /**
     * Busca resultados de uma equipe específica
     */
    public List<LeaderboardSummaryDTO> getResultadosEquipe(Long equipeId) {
        return leaderboardService.getLeaderboardEquipe(equipeId);
    }

    /**
     * Busca resultados de um atleta específico
     */
    public List<LeaderboardSummaryDTO> getResultadosAtleta(Long atletaId) {
        return leaderboardService.getLeaderboardAtleta(atletaId);
    }

    /**
     * Remove resultado de um participante
     */
    @Transactional
    public void removerResultado(Long workoutId, Long participanteId, boolean isEquipe) {
        LeaderboardEntity leaderboard;
        
        if (isEquipe) {
            leaderboard = leaderboardRepository.findByWorkoutIdAndEquipeId(workoutId, participanteId)
                    .orElseThrow(() -> new RuntimeException("Resultado não encontrado para esta equipe"));
        } else {
            leaderboard = leaderboardRepository.findByWorkoutIdAndAtletaId(workoutId, participanteId)
                    .orElseThrow(() -> new RuntimeException("Resultado não encontrado para este atleta"));
        }

        leaderboardService.deletarLeaderboardResultado(leaderboard.getId());
    }

    /**
     * Verifica se um workout já tem resultados inicializados
     */
    public boolean workoutTemResultados(Long categoriaId, Long workoutId) {
        return leaderboardRepository.existsResultadosParaWorkout(categoriaId, workoutId);
    }

    /**
     * Conta quantos participantes finalizaram o workout
     */
    public long contarParticipantesFinalizados(Long categoriaId, Long workoutId) {
        return leaderboardRepository.countFinalizadosByWorkout(categoriaId, workoutId);
    }

    /**
     * Conta total de participantes no workout
     */
    public long contarTotalParticipantes(Long categoriaId, Long workoutId) {
        return leaderboardRepository.countTotalByWorkout(categoriaId, workoutId);
    }

    /**
     * Verifica se o workout está finalizado (todos os participantes têm resultado)
     */
    public boolean isWorkoutFinalizado(Long categoriaId, Long workoutId) {
        long finalizados = contarParticipantesFinalizados(categoriaId, workoutId);
        long total = contarTotalParticipantes(categoriaId, workoutId);
        return finalizados > 0 && finalizados == total;
    }

    /**
     * Busca participantes que ainda não finalizaram o workout
     */
    public List<String> getParticipantesPendentes(Long categoriaId, Long workoutId) {
        List<LeaderboardEntity> resultados = leaderboardRepository
                .findByCategoriaIdAndWorkoutIdAndFinalizadoFalse(categoriaId, workoutId);

        return resultados.stream()
                .map(LeaderboardEntity::getNomeParticipante)
                .collect(Collectors.toList());
    }

    /**
     * Registra resultado único (método simplificado) - funciona como upsert
     */
    @Transactional
    public LeaderboardResponseDTO registrarResultado(Long eventoId, Long workoutId, Long categoriaId, 
                                                    Long participanteId, boolean isEquipe,
                                                    Object resultadoValor, Boolean finalizado) {
        
        // Validar evento
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventoId));

        // Validar workout
        WorkoutEntity workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout não encontrado com ID: " + workoutId));

        // Validar categoria
        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + categoriaId));

        // Verificar se workout pertence à categoria
        if (!categoria.getWorkouts().contains(workout)) {
            throw new RuntimeException("Workout não pertence a esta categoria");
        }

        // Verificar se categoria pertence ao evento
        if (!categoria.getEvento().getId().equals(eventoId)) {
            throw new RuntimeException("Categoria não pertence ao evento especificado");
        }

        // Verificar se já existe resultado (para implementar upsert)
        LeaderboardEntity resultadoExistente = null;
        if (isEquipe) {
            resultadoExistente = leaderboardRepository.findByCategoriaIdAndWorkoutIdAndEquipeId(categoriaId, workoutId, participanteId)
                    .orElse(null);
        } else {
            resultadoExistente = leaderboardRepository.findByCategoriaIdAndWorkoutIdAndAtletaId(categoriaId, workoutId, participanteId)
                    .orElse(null);
        }

        if (resultadoExistente != null) {
            // Atualizar resultado existente
            LeaderboardResultadoUpdateDTO.LeaderboardResultadoUpdateDTOBuilder updateBuilder = 
                    LeaderboardResultadoUpdateDTO.builder();

            switch (workout.getTipo()) {
                case REPS:
                    if (resultadoValor instanceof Integer) {
                        updateBuilder.resultadoReps((Integer) resultadoValor);
                    } else if (resultadoValor instanceof String) {
                        updateBuilder.resultadoReps(Integer.valueOf((String) resultadoValor));
                    }
                    break;
                case PESO:
                    if (resultadoValor instanceof Double) {
                        updateBuilder.resultadoPeso((Double) resultadoValor);
                    } else if (resultadoValor instanceof String) {
                        updateBuilder.resultadoPeso(Double.valueOf((String) resultadoValor));
                    }
                    break;
                case TEMPO:
                    if (resultadoValor instanceof String) {
                        updateBuilder.resultadoTempo((String) resultadoValor);
                    }
                    break;
            }

            if (finalizado != null) {
                updateBuilder.finalizado(finalizado);
            }

            LeaderboardResultadoUpdateDTO updateDto = updateBuilder.build();
            LeaderboardResponseDTO resultado = leaderboardService.atualizarLeaderboardResultado(resultadoExistente.getId(), updateDto);
            
            // Recalcular posições do workout
            leaderboardService.calcularRankingWorkout(categoriaId, workoutId);
            
            // Recalcular pontuações totais após atualizar posições
            pontuacaoService.recalcularTodasPontuacoesPorCategoria(categoriaId);
            
            return resultado;
        } else {
            // Criar novo resultado
            LeaderboardResultadoCreateDTO.LeaderboardResultadoCreateDTOBuilder dtoBuilder = 
                    LeaderboardResultadoCreateDTO.builder()
                            .categoriaId(categoriaId)
                            .workoutId(workoutId)
                            .finalizado(finalizado != null ? finalizado : false);

            if (isEquipe) {
                dtoBuilder.equipeId(participanteId);
            } else {
                dtoBuilder.atletaId(participanteId);
            }

            switch (workout.getTipo()) {
                case REPS:
                    if (resultadoValor instanceof Integer) {
                        dtoBuilder.resultadoReps((Integer) resultadoValor);
                    } else if (resultadoValor instanceof String) {
                        dtoBuilder.resultadoReps(Integer.valueOf((String) resultadoValor));
                    }
                    break;
                case PESO:
                    if (resultadoValor instanceof Double) {
                        dtoBuilder.resultadoPeso((Double) resultadoValor);
                    } else if (resultadoValor instanceof String) {
                        dtoBuilder.resultadoPeso(Double.valueOf((String) resultadoValor));
                    }
                    break;
                case TEMPO:
                    if (resultadoValor instanceof String) {
                        dtoBuilder.resultadoTempo((String) resultadoValor);
                    }
                    break;
            }

            LeaderboardResultadoCreateDTO dto = dtoBuilder.build();
            LeaderboardResponseDTO resultado = leaderboardService.registrarLeaderboardResultado(dto);
            
            // Recalcular posições do workout
            leaderboardService.calcularRankingWorkout(categoriaId, workoutId);
            
            // Recalcular pontuações totais após atualizar posições
            pontuacaoService.recalcularTodasPontuacoesPorCategoria(categoriaId);
            
            return resultado;
        }
    }
}