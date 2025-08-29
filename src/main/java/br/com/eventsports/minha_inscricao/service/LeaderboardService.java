package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.atleta.AtletaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.*;
import br.com.eventsports.minha_inscricao.dto.leaderboard.WorkoutPosicaoDTO;
import br.com.eventsports.minha_inscricao.dto.workout.WorkoutSummaryDTO;
import br.com.eventsports.minha_inscricao.entity.*;
import br.com.eventsports.minha_inscricao.repository.AtletaRepository;
import br.com.eventsports.minha_inscricao.repository.CategoriaRepository;
import br.com.eventsports.minha_inscricao.repository.EquipeRepository;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.repository.LeaderboardRepository;
import br.com.eventsports.minha_inscricao.repository.WorkoutRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.ILeaderboardService;
import br.com.eventsports.minha_inscricao.service.Interfaces.IPontuacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LeaderboardService implements ILeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final CategoriaRepository categoriaRepository;
    private final WorkoutRepository workoutRepository;
    private final AtletaRepository atletaRepository;
    private final EquipeRepository equipeRepository;
    private final EventoRepository eventoRepository;
    private final IPontuacaoService pontuacaoService;


    /**
     * Busca resultados de um workout específico em uma categoria
     */
    @Cacheable(value = "leaderboards", key = "'workout_' + #categoriaId + '_' + #workoutId")
    public List<LeaderboardSummaryDTO> getLeaderboardWorkout(Long categoriaId, Long workoutId) {
        List<LeaderboardEntity> resultados = leaderboardRepository
                .findByCategoriaIdAndWorkoutIdOrderByPosicaoWorkoutAsc(categoriaId, workoutId);
        
        return resultados.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca resultados de um workout com recálculo automático de posições
     */
    @Transactional
    @CacheEvict(value = "leaderboards", key = "'workout_' + #categoriaId + '_' + #workoutId")
    public List<LeaderboardSummaryDTO> getLeaderboardWorkoutComRecalculo(Long categoriaId, Long workoutId) {
        // Primeiro recalcular posições
        calcularRankingWorkout(categoriaId, workoutId);
        
        // Depois retornar resultados atualizados
        return getLeaderboardWorkout(categoriaId, workoutId);
    }


    /**
     * Busca resultados de uma equipe específica
     */
    @Cacheable(value = "leaderboards", key = "'equipe_' + #equipeId")
    public List<LeaderboardSummaryDTO> getLeaderboardEquipe(Long equipeId) {
        List<LeaderboardEntity> resultados = leaderboardRepository.findByEquipeIdOrderByWorkoutNomeAsc(equipeId);
        
        return resultados.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca resultados de um atleta específico
     */
    @Cacheable(value = "leaderboards", key = "'atleta_' + #atletaId")
    public List<LeaderboardSummaryDTO> getLeaderboardAtleta(Long atletaId) {
        List<LeaderboardEntity> resultados = leaderboardRepository.findByAtletaIdOrderByWorkoutNomeAsc(atletaId);
        
        return resultados.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }



    // Métodos auxiliares privados






    // Métodos de conversão

    private LeaderboardResponseDTO convertToResponseDTO(LeaderboardEntity leaderboard) {
        return LeaderboardResponseDTO.builder()
                .id(leaderboard.getId())
                .posicaoWorkout(leaderboard.getPosicaoWorkout())
                .finalizado(leaderboard.getFinalizado())
                .evento(convertEventoToSimpleDTO(leaderboard.getEvento()))
                .categoria(convertCategoriaToSimpleDTO(leaderboard.getCategoria()))
                .workout(convertWorkoutToSimpleDTO(leaderboard.getWorkout()))
                .equipe(leaderboard.getEquipe() != null ? convertEquipeToSimpleDTO(leaderboard.getEquipe()) : null)
                .atleta(leaderboard.getAtleta() != null ? convertAtletaToSimpleDTO(leaderboard.getAtleta()) : null)
                .resultadoReps(leaderboard.getResultadoReps())
                .resultadoPeso(leaderboard.getResultadoPeso())
                .resultadoTempo(leaderboard.formatarTempo(leaderboard.getResultadoTempoSegundos()))
                .resultadoTempoSegundos(leaderboard.getResultadoTempoSegundos())
                .resultadoFormatado(leaderboard.getResultadoFormatado())
                .nomeParticipante(leaderboard.getNomeParticipante())
                .isCategoriaEquipe(leaderboard.isCategoriaEquipe())
                .temResultado(leaderboard.temResultado())
                .isPodioWorkout(leaderboard.isPodioWorkout())
                .medalhaWorkout(leaderboard.getMedalhaWorkout())
                .createdAt(leaderboard.getCreatedAt())
                .updatedAt(leaderboard.getUpdatedAt())
                .build();
    }

    private LeaderboardSummaryDTO convertToSummaryDTO(LeaderboardEntity leaderboard) {
        return LeaderboardSummaryDTO.builder()
                .id(leaderboard.getId())
                .posicaoWorkout(leaderboard.getPosicaoWorkout())
                .nomeParticipante(leaderboard.getNomeParticipante())
                .nomeWorkout(leaderboard.getNomeWorkout())
                .nomeCategoria(leaderboard.getNomeCategoria())
                .resultadoFormatado(leaderboard.getResultadoFormatado())
                .finalizado(leaderboard.getFinalizado())
                .isPodioWorkout(leaderboard.isPodioWorkout())
                .medalhaWorkout(leaderboard.getMedalhaWorkout())
                .isCategoriaEquipe(leaderboard.isCategoriaEquipe())
                .build();
    }







    // Métodos de conversão mínimos para LeaderboardResponseDTO
    
    private EventoMinimalDTO convertEventoToSimpleDTO(EventoEntity evento) {
        return EventoMinimalDTO.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .build();
    }
    
    private CategoriaMinimalDTO convertCategoriaToSimpleDTO(CategoriaEntity categoria) {
        return CategoriaMinimalDTO.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .build();
    }
    
    private WorkoutMinimalDTO convertWorkoutToSimpleDTO(WorkoutEntity workout) {
        return WorkoutMinimalDTO.builder()
                .id(workout.getId())
                .nome(workout.getNome())
                .tipo(workout.getTipo())
                .build();
    }
    
    private EquipeMinimalDTO convertEquipeToSimpleDTO(EquipeEntity equipe) {
        return EquipeMinimalDTO.builder()
                .id(equipe.getId())
                .nome(equipe.getNome())
                .build();
    }
    
    private AtletaMinimalDTO convertAtletaToSimpleDTO(AtletaEntity atleta) {
        return AtletaMinimalDTO.builder()
                .id(atleta.getId())
                .nome(atleta.getNomeCompleto())
                .build();
    }

    // Métodos para registro de resultados

    /**
     * Registra resultado de um participante em um workout
     */
    @Transactional
    public LeaderboardResponseDTO registrarLeaderboardResultado(LeaderboardResultadoCreateDTO dto) {
        // Validar dados
        validateLeaderboardResultadoData(dto);

        // Buscar entidades
        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + dto.getCategoriaId()));

        WorkoutEntity workout = workoutRepository.findById(dto.getWorkoutId())
                .orElseThrow(() -> new RuntimeException("Workout não encontrado com ID: " + dto.getWorkoutId()));

        EventoEntity evento = categoria.getEvento();

        // Verificar se já existe resultado para este participante neste workout
        if (categoria.isEquipe() && dto.getEquipeId() != null) {
            if (leaderboardRepository.existsResultadoParaParticipante(dto.getCategoriaId(), dto.getWorkoutId(), dto.getEquipeId())) {
                throw new RuntimeException("Já existe resultado registrado para esta equipe neste workout");
            }
        } else if (categoria.isIndividual() && dto.getAtletaId() != null) {
            if (leaderboardRepository.existsResultadoParaParticipante(dto.getCategoriaId(), dto.getWorkoutId(), dto.getAtletaId())) {
                throw new RuntimeException("Já existe resultado registrado para este atleta neste workout");
            }
        }

        // Criar leaderboard
        LeaderboardEntity leaderboard = LeaderboardEntity.builder()
                .evento(evento)
                .categoria(categoria)
                .workout(workout)
                .posicaoWorkout(999) // Posição padrão até ser recalculada
                .finalizado(dto.getFinalizado() != null ? dto.getFinalizado() : false)
                .build();

        // Definir participante baseado no tipo da categoria
        if (categoria.isEquipe() && dto.getEquipeId() != null) {
            // Buscar equipe e verificar se pertence à categoria
            EquipeEntity equipe = equipeRepository.findById(dto.getEquipeId())
                    .orElseThrow(() -> new RuntimeException("Equipe não encontrada com ID: " + dto.getEquipeId()));
            
            // Verificar se equipe pertence à categoria
            if (!equipe.getCategoria().getId().equals(dto.getCategoriaId())) {
                throw new RuntimeException("Equipe não pertence a esta categoria");
            }
            
            // Verificar se equipe está ativa
            if (equipe.getAtiva() == null || !equipe.getAtiva()) {
                throw new RuntimeException("Não é possível registrar resultados para equipe inativa: " + equipe.getNome());
            }
            
            leaderboard.setEquipe(equipe);
        } else if (categoria.isIndividual() && dto.getAtletaId() != null) {
            // Buscar atleta diretamente no repositório
            AtletaEntity atleta = atletaRepository.findById(dto.getAtletaId())
                    .orElseThrow(() -> new RuntimeException("Atleta não encontrado com ID: " + dto.getAtletaId()));
            
            // Verificar se atleta pertence à categoria
            if (atleta.getCategoria() == null || !atleta.getCategoria().getId().equals(dto.getCategoriaId())) {
                throw new RuntimeException("Atleta não pertence a esta categoria");
            }
            
            // Verificar se atleta está ativo (aceita termos)
            if (atleta.getAceitaTermos() == null || !atleta.getAceitaTermos()) {
                throw new RuntimeException("Não é possível registrar resultados para atleta inativo: " + atleta.getNome());
            }
            
            leaderboard.setAtleta(atleta);
        } else {
            throw new RuntimeException("Deve informar equipeId para categoria EQUIPE ou atletaId para categoria INDIVIDUAL");
        }

        // Definir resultado baseado no tipo do workout
        defineResultadoFromCreateDTO(leaderboard, dto);

        // Salvar
        LeaderboardEntity savedLeaderboard = leaderboardRepository.save(leaderboard);

        return convertToResponseDTO(savedLeaderboard);
    }

    /**
     * Registra múltiplos resultados em lote
     */
    @Transactional
    public List<LeaderboardResponseDTO> registrarLeaderboardResultadosLote(LeaderboardResultadoLoteDTO dto) {
        List<LeaderboardResponseDTO> resultados = new ArrayList<>();

        for (LeaderboardResultadoLoteDTO.ResultadoLoteItemDTO item : dto.getResultados()) {
            LeaderboardResultadoCreateDTO createDTO = LeaderboardResultadoCreateDTO.builder()
                    .categoriaId(dto.getCategoriaId())
                    .workoutId(dto.getWorkoutId())
                    .equipeId(item.getEquipeId())
                    .atletaId(item.getAtletaId())
                    .resultadoReps(item.getResultadoReps())
                    .resultadoPeso(item.getResultadoPeso())
                    .resultadoTempo(item.getResultadoTempo())
                    .observacoes(item.getObservacoes())
                    .finalizado(item.getFinalizado())
                    .build();

            try {
                LeaderboardResponseDTO resultado = registrarLeaderboardResultado(createDTO);
                resultados.add(resultado);
            } catch (RuntimeException e) {
                // Log erro mas continua processando outros resultados
                System.err.println("Erro ao registrar resultado: " + e.getMessage());
            }
        }

        return resultados;
    }

    /**
     * Atualiza resultado existente
     */
    @Transactional
    public LeaderboardResponseDTO atualizarLeaderboardResultado(Long id, LeaderboardResultadoUpdateDTO dto) {
        LeaderboardEntity leaderboard = leaderboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado não encontrado com ID: " + id));

        // Atualizar campos se fornecidos
        if (dto.getFinalizado() != null) {
            leaderboard.setFinalizado(dto.getFinalizado());
        }

        // Atualizar resultado baseado no tipo do workout
        defineResultadoFromUpdateDTO(leaderboard, dto);

        LeaderboardEntity updatedLeaderboard = leaderboardRepository.save(leaderboard);
        
        return convertToResponseDTO(updatedLeaderboard);
    }

    /**
     * Calcula ranking de um workout (atribui posições)
     */
    @Transactional
    public List<LeaderboardSummaryDTO> calcularRankingWorkout(Long categoriaId, Long workoutId) {
        List<LeaderboardEntity> resultados = leaderboardRepository
                .findByCategoriaIdAndWorkoutIdOrderByPosicaoWorkoutAsc(categoriaId, workoutId);

        if (resultados.isEmpty()) {
            return new ArrayList<>();
        }

        // Ordenar por performance baseado no tipo do workout
        WorkoutEntity workout = resultados.get(0).getWorkout();
        resultados.sort((a, b) -> compareByPerformance(a, b, workout.getTipo()));

        // Atribuir posições
        AtomicInteger posicao = new AtomicInteger(1);
        resultados.forEach(resultado -> {
            resultado.setPosicaoWorkout(posicao.getAndIncrement());
            leaderboardRepository.save(resultado);
        });

        return resultados.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    /**
     * Deleta resultado
     */
    @Transactional
    public void deletarLeaderboardResultado(Long id) {
        LeaderboardEntity leaderboard = leaderboardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resultado não encontrado com ID: " + id));
        
        // Atualizar pontuação antes de excluir
        pontuacaoService.atualizarPontuacaoAposExcluirResultado(leaderboard);
        
        leaderboardRepository.delete(leaderboard);
    }

    // Métodos auxiliares

    private void validateLeaderboardResultadoData(LeaderboardResultadoCreateDTO dto) {
        // Verificar se categoria e workout existem
        CategoriaEntity categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        WorkoutEntity workout = workoutRepository.findById(dto.getWorkoutId())
                .orElseThrow(() -> new RuntimeException("Workout não encontrado"));

        // Verificar se workout pertence à categoria
        if (!categoria.getWorkouts().contains(workout)) {
            throw new RuntimeException("Workout não pertence a esta categoria");
        }

        // Verificar participante baseado no tipo da categoria
        if (categoria.isEquipe()) {
            if (dto.getEquipeId() == null) {
                throw new RuntimeException("ID da equipe é obrigatório para categoria do tipo EQUIPE");
            }
            if (dto.getAtletaId() != null) {
                throw new RuntimeException("ID do atleta deve ser null para categoria do tipo EQUIPE");
            }
        } else if (categoria.isIndividual()) {
            if (dto.getAtletaId() == null) {
                throw new RuntimeException("ID do atleta é obrigatório para categoria do tipo INDIVIDUAL");
            }
            if (dto.getEquipeId() != null) {
                throw new RuntimeException("ID da equipe deve ser null para categoria do tipo INDIVIDUAL");
            }
        }

        // Verificar se resultado é compatível com tipo do workout
        validateResultadoByWorkoutType(workout.getTipo(), dto.getResultadoReps(), dto.getResultadoPeso(), dto.getResultadoTempo());
    }

    private void validateResultadoByWorkoutType(br.com.eventsports.minha_inscricao.enums.TipoWorkout tipo, 
                                              Integer reps, Double peso, String tempo) {
        switch (tipo) {
            case REPS:
                if (reps == null || reps <= 0) {
                    throw new RuntimeException("Resultado em repetições é obrigatório para workout tipo REPS");
                }
                break;
            case PESO:
                if (peso == null || peso <= 0) {
                    throw new RuntimeException("Resultado em peso é obrigatório para workout tipo PESO");
                }
                break;
            case TEMPO:
                if (tempo == null || tempo.trim().isEmpty()) {
                    throw new RuntimeException("Resultado em tempo é obrigatório para workout tipo TEMPO");
                }
                break;
        }
    }

    private void defineResultadoFromCreateDTO(LeaderboardEntity leaderboard, LeaderboardResultadoCreateDTO dto) {
        switch (leaderboard.getWorkout().getTipo()) {
            case REPS:
                if (dto.getResultadoReps() != null) {
                    leaderboard.setResultadoReps(dto.getResultadoReps());
                }
                break;
            case PESO:
                if (dto.getResultadoPeso() != null) {
                    leaderboard.setResultadoPeso(dto.getResultadoPeso());
                }
                break;
            case TEMPO:
                if (dto.getResultadoTempo() != null && !dto.getResultadoTempo().trim().isEmpty()) {
                    try {
                        Integer segundos = converterTempoParaSegundos(dto.getResultadoTempo());
                        leaderboard.setResultadoTempoSegundos(segundos);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Formato de tempo inválido: " + e.getMessage());
                    }
                }
                break;
        }
    }

    private void defineResultadoFromUpdateDTO(LeaderboardEntity leaderboard, LeaderboardResultadoUpdateDTO dto) {
        switch (leaderboard.getWorkout().getTipo()) {
            case REPS:
                if (dto.getResultadoReps() != null) {
                    leaderboard.setResultadoReps(dto.getResultadoReps());
                }
                break;
            case PESO:
                if (dto.getResultadoPeso() != null) {
                    leaderboard.setResultadoPeso(dto.getResultadoPeso());
                }
                break;
            case TEMPO:
                if (dto.getResultadoTempo() != null && !dto.getResultadoTempo().trim().isEmpty()) {
                    try {
                        Integer segundos = converterTempoParaSegundos(dto.getResultadoTempo());
                        leaderboard.setResultadoTempoSegundos(segundos);
                    } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Formato de tempo inválido: " + e.getMessage());
                    }
                }
                break;
        }
    }

    private int compareByPerformance(LeaderboardEntity a, LeaderboardEntity b, br.com.eventsports.minha_inscricao.enums.TipoWorkout tipo) {
        switch (tipo) {
            case REPS:
                // Mais repetições = melhor (ordem decrescente)
                return Integer.compare(
                    b.getResultadoReps() != null ? b.getResultadoReps() : 0,
                    a.getResultadoReps() != null ? a.getResultadoReps() : 0
                );
            case PESO:
                // Mais peso = melhor (ordem decrescente)
                return Double.compare(
                    b.getResultadoPeso() != null ? b.getResultadoPeso() : 0.0,
                    a.getResultadoPeso() != null ? a.getResultadoPeso() : 0.0
                );
            case TEMPO:
                // Menos tempo = melhor (ordem crescente)
                return Integer.compare(
                    a.getResultadoTempoSegundos() != null ? a.getResultadoTempoSegundos() : Integer.MAX_VALUE,
                    b.getResultadoTempoSegundos() != null ? b.getResultadoTempoSegundos() : Integer.MAX_VALUE
                );
            default:
                return 0;
        }
    }

    /**
     * Converte string de tempo (mm:ss ou hh:mm:ss) para segundos
     */
    private static Integer converterTempoParaSegundos(String tempo) {
        if (tempo == null || tempo.trim().isEmpty()) {
            return null;
        }
        
        String[] partes = tempo.trim().split(":");
        
        try {
            if (partes.length == 2) {
                // Formato mm:ss
                int minutos = Integer.parseInt(partes[0]);
                int segundos = Integer.parseInt(partes[1]);
                return minutos * 60 + segundos;
            } else if (partes.length == 3) {
                // Formato hh:mm:ss
                int horas = Integer.parseInt(partes[0]);
                int minutos = Integer.parseInt(partes[1]);
                int segundos = Integer.parseInt(partes[2]);
                return horas * 3600 + minutos * 60 + segundos;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato de tempo inválido. Use mm:ss ou hh:mm:ss");
        }
        
        throw new IllegalArgumentException("Formato de tempo inválido. Use mm:ss ou hh:mm:ss");
    }

    /**
     * Busca ranking completo de uma categoria (evento)
     */
    public List<LeaderboardRankingDTO> getRankingCategoria(Long eventoId, Long categoriaId) {
        // Validar evento
        EventoEntity evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado com ID: " + eventoId));

        // Validar categoria
        CategoriaEntity categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + categoriaId));

        // Verificar se categoria pertence ao evento
        if (!categoria.getEvento().getId().equals(eventoId)) {
            throw new RuntimeException("Categoria não pertence ao evento especificado");
        }

        List<LeaderboardRankingDTO> ranking = new ArrayList<>();

        if (categoria.isEquipe()) {
            // Buscar ranking de equipes
            List<EquipeEntity> equipes = leaderboardRepository.findEquipesRankingByCategoria(categoriaId);
            
            for (int i = 0; i < equipes.size(); i++) {
                EquipeEntity equipe = equipes.get(i);
                
                // Contar workouts completados pela equipe
                long workoutsCompletados = leaderboardRepository
                        .countWorkoutsFinalizadosByEquipe(categoriaId, equipe.getId());
                
                // Buscar posições detalhadas da equipe em cada workout
                List<WorkoutPosicaoDTO> posicoesWorkouts = buscarPosicoesPorWorkout(categoriaId, equipe.getId());
                
                LeaderboardRankingDTO item = LeaderboardRankingDTO.builder()
                        .posicao(i + 1)
                        .nomeParticipante(equipe.getNome())
                        .pontuacaoTotal(equipe.getPontuacaoTotal())
                        .isEquipe(true)
                        .participanteId(equipe.getId())
                        .nomeCategoria(categoria.getNome())
                        .workoutsCompletados(workoutsCompletados)
                        .posicoesWorkouts(posicoesWorkouts)
                        .build();
                
                ranking.add(item);
            }
        } else {
            // Buscar ranking de atletas
            List<AtletaEntity> atletas = leaderboardRepository.findAtletasRankingByCategoria(categoriaId);
            
            for (int i = 0; i < atletas.size(); i++) {
                AtletaEntity atleta = atletas.get(i);
                
                // Contar workouts completados pelo atleta
                long workoutsCompletados = leaderboardRepository
                        .countWorkoutsFinalizadosByAtleta(categoriaId, atleta.getId());
                
                // Buscar posições detalhadas do atleta em cada workout
                List<WorkoutPosicaoDTO> posicoesWorkouts = buscarPosicoesPorWorkout(categoriaId, atleta.getId());
                
                LeaderboardRankingDTO item = LeaderboardRankingDTO.builder()
                        .posicao(i + 1)
                        .nomeParticipante(atleta.getNome())
                        .pontuacaoTotal(atleta.getPontuacaoTotal())
                        .isEquipe(false)
                        .participanteId(atleta.getId())
                        .nomeCategoria(categoria.getNome())
                        .workoutsCompletados(workoutsCompletados)
                        .posicoesWorkouts(posicoesWorkouts)
                        .build();
                
                ranking.add(item);
            }
        }

        return ranking;
    }

    /**
     * Busca as posições de um participante em todos os workouts de uma categoria
     */
    private List<WorkoutPosicaoDTO> buscarPosicoesPorWorkout(Long categoriaId, Long participanteId) {
        List<LeaderboardEntity> posicoes = leaderboardRepository
                .findPosicoesByParticipanteAndCategoria(categoriaId, participanteId);
        
        return posicoes.stream()
                .map(this::convertToWorkoutPosicaoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Converte LeaderboardEntity para WorkoutPosicaoDTO
     */
    private WorkoutPosicaoDTO convertToWorkoutPosicaoDTO(LeaderboardEntity leaderboard) {
        return WorkoutPosicaoDTO.builder()
                .workoutId(leaderboard.getWorkout().getId())
                .nomeWorkout(leaderboard.getWorkout().getNome())
                .posicaoWorkout(leaderboard.getPosicaoWorkout())
                .resultadoFormatado(leaderboard.getResultadoFormatado())
                .build();
    }

}
