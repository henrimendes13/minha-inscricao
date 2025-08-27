package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.atleta.AtletaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.categoria.CategoriaSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.equipe.EquipeSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.evento.EventoSummaryDTO;
import br.com.eventsports.minha_inscricao.dto.leaderboard.*;
import br.com.eventsports.minha_inscricao.dto.workout.WorkoutSummaryDTO;
import br.com.eventsports.minha_inscricao.entity.*;
import br.com.eventsports.minha_inscricao.repository.CategoriaRepository;
import br.com.eventsports.minha_inscricao.repository.LeaderboardRepository;
import br.com.eventsports.minha_inscricao.repository.WorkoutRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.ILeaderboardService;
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

    /**
     * Busca o leaderboard final de uma categoria (ranking geral)
     */
    @Cacheable(value = "leaderboards", key = "'final_categoria_' + #categoriaId")
    public List<LeaderboardFinalDTO> getLeaderboardFinalCategoria(Long categoriaId) {
        List<LeaderboardEntity> resultados = leaderboardRepository.findByCategoriaIdOrderByPosicaoWorkoutAsc(categoriaId);
        
        if (resultados.isEmpty()) {
            return new ArrayList<>();
        }

        LeaderboardEntity primeiroResultado = resultados.get(0);
        boolean isCategoriaEquipe = primeiroResultado.isCategoriaEquipe();

        if (isCategoriaEquipe) {
            return getLeaderboardFinalEquipes(categoriaId, resultados);
        } else {
            return getLeaderboardFinalAtletas(categoriaId, resultados);
        }
    }

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
     * Busca todos os resultados de uma categoria
     */
    @Cacheable(value = "leaderboards", key = "'categoria_' + #categoriaId")
    public List<LeaderboardResponseDTO> getLeaderboardCategoria(Long categoriaId) {
        List<LeaderboardEntity> resultados = leaderboardRepository.findByCategoriaIdOrderByPosicaoWorkoutAsc(categoriaId);
        
        return resultados.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
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

    /**
     * Busca todos os leaderboards de um evento
     */
    @Cacheable(value = "leaderboards", key = "'evento_' + #eventoId")
    public List<LeaderboardResponseDTO> getLeaderboardEvento(Long eventoId) {
        List<LeaderboardEntity> resultados = leaderboardRepository
                .findByEventoIdOrderByCategoriaNomeAscPosicaoWorkoutAsc(eventoId);
        
        return resultados.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca estatísticas de uma categoria
     */
    public Object[] getEstatisticasCategoria(Long categoriaId) {
        return leaderboardRepository.findEstatisticasCategoria(categoriaId);
    }

    // Métodos auxiliares privados

    private List<LeaderboardFinalDTO> getLeaderboardFinalEquipes(Long categoriaId, List<LeaderboardEntity> resultados) {
        List<Object[]> ranking = leaderboardRepository.findRankingEquipesByCategoria(categoriaId);
        AtomicInteger posicao = new AtomicInteger(1);

        return ranking.stream()
                .map(entry -> {
                    EquipeEntity equipe = (EquipeEntity) entry[0];
                    Integer pontuacaoTotal = (Integer) entry[1];
                    
                    List<LeaderboardEntity> resultadosEquipe = resultados.stream()
                            .filter(r -> equipe.equals(r.getEquipe()))
                            .collect(Collectors.toList());

                    return createLeaderboardFinalDTO(
                            posicao.getAndIncrement(),
                            pontuacaoTotal,
                            equipe,
                            null,
                            resultadosEquipe
                    );
                })
                .collect(Collectors.toList());
    }

    private List<LeaderboardFinalDTO> getLeaderboardFinalAtletas(Long categoriaId, List<LeaderboardEntity> resultados) {
        List<Object[]> ranking = leaderboardRepository.findRankingAtletasByCategoria(categoriaId);
        AtomicInteger posicao = new AtomicInteger(1);

        return ranking.stream()
                .map(entry -> {
                    UsuarioEntity atleta = (UsuarioEntity) entry[0];
                    Integer pontuacaoTotal = (Integer) entry[1];
                    
                    List<LeaderboardEntity> resultadosAtleta = resultados.stream()
                            .filter(r -> atleta.equals(r.getAtleta()))
                            .collect(Collectors.toList());

                    return createLeaderboardFinalDTO(
                            posicao.getAndIncrement(),
                            pontuacaoTotal,
                            null,
                            atleta,
                            resultadosAtleta
                    );
                })
                .collect(Collectors.toList());
    }

    private LeaderboardFinalDTO createLeaderboardFinalDTO(int posicaoFinal, Integer pontuacaoTotal, 
                                                          EquipeEntity equipe, UsuarioEntity atleta, 
                                                          List<LeaderboardEntity> resultados) {
        if (resultados.isEmpty()) {
            return null;
        }

        LeaderboardEntity primeiro = resultados.get(0);
        boolean isCategoriaEquipe = primeiro.isCategoriaEquipe();
        
        List<LeaderboardSummaryDTO> resultadosWorkouts = resultados.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());

        long workoutsFinalizados = resultados.stream()
                .filter(LeaderboardEntity::isFinalizadoWorkout)
                .count();

        int totalWorkouts = resultados.size();
        boolean finalizouTodos = workoutsFinalizados == totalWorkouts;

        String nomeParticipante = isCategoriaEquipe ? 
                (equipe != null ? equipe.getNome() : "") : 
                (atleta != null ? atleta.getNomeCompleto() : "");

        return LeaderboardFinalDTO.builder()
                .posicaoFinal(posicaoFinal)
                .pontuacaoTotal(pontuacaoTotal)
                .evento(convertEventoToSummaryDTO(primeiro.getEvento()))
                .categoria(convertCategoriaToSummaryDTO(primeiro.getCategoria()))
                .equipe(isCategoriaEquipe && equipe != null ? convertEquipeToSummaryDTO(equipe) : null)
                .atleta(!isCategoriaEquipe && atleta != null ? convertUsuarioToAtletaSummaryDTO(atleta) : null)
                .nomeParticipante(nomeParticipante)
                .resultadosWorkouts(resultadosWorkouts)
                .workoutsFinalizados((int) workoutsFinalizados)
                .totalWorkouts(totalWorkouts)
                .finalizouTodos(finalizouTodos)
                .isCategoriaEquipe(isCategoriaEquipe)
                .isPodioFinal(posicaoFinal <= 3)
                .medalhaFinal(getMedalhaFinal(posicaoFinal))
                .descricaoPerformance(createDescricaoPerformance(posicaoFinal, nomeParticipante, 
                        pontuacaoTotal, (int) workoutsFinalizados, totalWorkouts))
                .build();
    }

    private String getMedalhaFinal(int posicao) {
        return switch (posicao) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "";
        };
    }

    private String createDescricaoPerformance(int posicao, String nome, Integer pontuacao, 
                                            int finalizados, int total) {
        return String.format("%dº lugar: %s - %d pts (%d/%d workouts finalizados)", 
                posicao, nome, pontuacao, finalizados, total);
    }

    // Métodos de conversão

    private LeaderboardResponseDTO convertToResponseDTO(LeaderboardEntity leaderboard) {
        return LeaderboardResponseDTO.builder()
                .id(leaderboard.getId())
                .posicaoWorkout(leaderboard.getPosicaoWorkout())
                .pontuacaoTotal(leaderboard.getPontuacaoTotal())
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
                .build();
    }

    private WorkoutSummaryDTO convertWorkoutToSummaryDTO(WorkoutEntity workout) {
        return WorkoutSummaryDTO.builder()
                .id(workout.getId())
                .nome(workout.getNome())
                .tipo(workout.getTipo())
                .build();
    }

    private EquipeSummaryDTO convertEquipeToSummaryDTO(EquipeEntity equipe) {
        return EquipeSummaryDTO.builder()
                .id(equipe.getId())
                .nome(equipe.getNome())
                .build();
    }

    private AtletaSummaryDTO convertAtletaToSummaryDTO(UsuarioEntity atleta) {
        return AtletaSummaryDTO.builder()
                .id(atleta.getId())
                .nome(atleta.getNomeCompleto())
                .build();
    }

    private AtletaSummaryDTO convertUsuarioToAtletaSummaryDTO(UsuarioEntity usuario) {
        return AtletaSummaryDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNomeCompleto())
                .dataNascimento(usuario.getDataNascimento())
                .genero(usuario.getGenero())
                .telefone(usuario.getTelefone())
                .aceitaTermos(usuario.getAceitaTermos())
                .idade(usuario.getIdade())
                .podeParticipar(usuario.podeParticipar())
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
    
    private AtletaMinimalDTO convertAtletaToSimpleDTO(UsuarioEntity atleta) {
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
            EquipeEntity equipe = categoria.getEquipes().stream()
                    .filter(e -> e.getId().equals(dto.getEquipeId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Equipe não encontrada ou não pertence a esta categoria"));
            leaderboard.setEquipe(equipe);
        } else if (categoria.isIndividual() && dto.getAtletaId() != null) {
            // Buscar usuário atleta nas inscrições da categoria
            UsuarioEntity atleta = categoria.getInscricoes().stream()
                    .map(InscricaoEntity::getAtleta)
                    .filter(a -> a != null && a.getId().equals(dto.getAtletaId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Atleta não encontrado ou não está inscrito nesta categoria"));
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
}
