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

    /**
     * Inicializa resultados vazios para todas as equipes de uma categoria em um workout
     */
    @Transactional
    public List<LeaderboardResponseDTO> inicializarResultadosWorkout(Long workoutId, Long categoriaId) {
        WorkoutEntity workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout não encontrado com ID: " + workoutId));

        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + categoriaId));

        // Verificar se workout pertence à categoria
        if (!categoria.getWorkouts().contains(workout)) {
            throw new RuntimeException("Workout não pertence a esta categoria");
        }

        // Verificar se já existem resultados
        boolean jaExistemResultados = leaderboardRepository.existsResultadosParaWorkout(categoriaId, workoutId);
        if (jaExistemResultados) {
            throw new RuntimeException("Já existem resultados cadastrados para este workout nesta categoria");
        }

        List<LeaderboardResponseDTO> resultados = new java.util.ArrayList<>();

        if (categoria.isEquipe()) {
            // Criar resultados para todas as equipes da categoria
            for (EquipeEntity equipe : categoria.getEquipes()) {
                if (equipe.getAtiva() && equipe.inscricaoConfirmada()) {
                    LeaderboardResultadoCreateDTO dto = LeaderboardResultadoCreateDTO.builder()
                            .categoriaId(categoriaId)
                            .workoutId(workoutId)
                            .equipeId(equipe.getId())
                            .finalizado(false)
                            .build();

                    try {
                        LeaderboardResponseDTO resultado = leaderboardService.registrarLeaderboardResultado(dto);
                        resultados.add(resultado);
                    } catch (RuntimeException e) {
                        System.err.println("Erro ao criar resultado para equipe " + equipe.getNome() + ": " + e.getMessage());
                    }
                }
            }
        } else {
            // Criar resultados para todos os atletas inscritos na categoria
            for (InscricaoEntity inscricao : categoria.getInscricoes()) {
                if (inscricao.isConfirmada() && inscricao.getAtleta() != null) {
                    LeaderboardResultadoCreateDTO dto = LeaderboardResultadoCreateDTO.builder()
                            .categoriaId(categoriaId)
                            .workoutId(workoutId)
                            .atletaId(inscricao.getAtleta().getId())
                            .finalizado(false)
                            .build();

                    try {
                        LeaderboardResponseDTO resultado = leaderboardService.registrarLeaderboardResultado(dto);
                        resultados.add(resultado);
                    } catch (RuntimeException e) {
                        System.err.println("Erro ao criar resultado para atleta " + inscricao.getAtleta().getNomeCompleto() + ": " + e.getMessage());
                    }
                }
            }
        }

        return resultados;
    }

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
        return leaderboardService.atualizarLeaderboardResultado(leaderboard.getId(), dto);
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
        return leaderboardService.atualizarLeaderboardResultado(leaderboard.getId(), dto);
    }

    /**
     * Finaliza um workout (calcula ranking e posições)
     */
    @Transactional
    public List<LeaderboardSummaryDTO> finalizarWorkout(Long categoriaId, Long workoutId) {
        return leaderboardService.calcularRankingWorkout(categoriaId, workoutId);
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
     * Registra resultado único (método simplificado)
     */
    @Transactional
    public LeaderboardResponseDTO registrarResultado(Long workoutId, Long categoriaId, 
                                                    Long participanteId, boolean isEquipe,
                                                    Object resultadoValor, Boolean finalizado) {
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

        // Buscar o workout para determinar o tipo
        WorkoutEntity workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new RuntimeException("Workout não encontrado"));

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
        return leaderboardService.registrarLeaderboardResultado(dto);
    }
}