package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.entity.LeaderboardEntity;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.repository.AtletaRepository;
import br.com.eventsports.minha_inscricao.repository.EquipeRepository;
import br.com.eventsports.minha_inscricao.repository.LeaderboardRepository;
import br.com.eventsports.minha_inscricao.service.Interfaces.IPontuacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PontuacaoService implements IPontuacaoService {

    private final LeaderboardRepository leaderboardRepository;
    private final EquipeRepository equipeRepository;
    private final AtletaRepository atletaRepository;

    public void atualizarPontuacaoAtleta(AtletaEntity atleta) {
        if (atleta == null || atleta.getId() == null) {
            return;
        }

        List<LeaderboardEntity> resultados = leaderboardRepository.findByAtletaIdOrderByWorkoutNomeAsc(atleta.getId());
        
        int pontuacaoTotal = resultados.stream()
                .filter(r -> r.getPosicaoWorkout() != null)
                .mapToInt(LeaderboardEntity::getPosicaoWorkout)
                .sum();

        atletaRepository.updatePontuacaoTotal(atleta.getId(), pontuacaoTotal);
        
        log.debug("Pontuação do atleta {} atualizada para {} pontos", 
                atleta.getNomeCompleto(), pontuacaoTotal);
    }

    public void atualizarPontuacaoEquipe(EquipeEntity equipe) {
        if (equipe == null || equipe.getId() == null) {
            return;
        }

        List<LeaderboardEntity> resultados = leaderboardRepository.findByEquipeIdOrderByWorkoutNomeAsc(equipe.getId());
        
        int pontuacaoTotal = resultados.stream()
                .filter(r -> r.getPosicaoWorkout() != null)
                .mapToInt(LeaderboardEntity::getPosicaoWorkout)
                .sum();

        equipeRepository.updatePontuacaoTotal(equipe.getId(), pontuacaoTotal);
        
        log.debug("Pontuação da equipe {} atualizada para {} pontos", 
                equipe.getNome(), pontuacaoTotal);
    }

    public void atualizarPontuacaoUsuario(UsuarioEntity usuario) {
        if (usuario == null || usuario.getId() == null) {
            return;
        }

        List<LeaderboardEntity> resultados = leaderboardRepository.findByAtletaIdOrderByWorkoutNomeAsc(usuario.getId());
        
        int pontuacaoTotal = resultados.stream()
                .filter(r -> r.getPosicaoWorkout() != null)
                .mapToInt(LeaderboardEntity::getPosicaoWorkout)
                .sum();

        atletaRepository.updatePontuacaoTotal(usuario.getId(), pontuacaoTotal);
        log.debug("Pontuação do usuário {} atualizada para {} pontos", 
                usuario.getNomeCompleto(), pontuacaoTotal);
    }


    public void atualizarPontuacaoEquipePorId(Long equipeId) {
        if (equipeId == null) {
            return;
        }

        List<LeaderboardEntity> resultados = leaderboardRepository.findByEquipeIdOrderByWorkoutNomeAsc(equipeId);
        
        int pontuacaoTotal = resultados.stream()
                .filter(r -> r.getPosicaoWorkout() != null)
                .mapToInt(LeaderboardEntity::getPosicaoWorkout)
                .sum();

        equipeRepository.updatePontuacaoTotal(equipeId, pontuacaoTotal);
        
        log.debug("Pontuação da equipe ID {} atualizada para {} pontos", 
                equipeId, pontuacaoTotal);
    }

    public void atualizarPontuacaoAtletaPorId(Long atletaId) {
        if (atletaId == null) {
            return;
        }

        List<LeaderboardEntity> resultados = leaderboardRepository.findByAtletaIdOrderByWorkoutNomeAsc(atletaId);
        
        int pontuacaoTotal = resultados.stream()
                .filter(r -> r.getPosicaoWorkout() != null)
                .mapToInt(LeaderboardEntity::getPosicaoWorkout)
                .sum();

        atletaRepository.updatePontuacaoTotal(atletaId, pontuacaoTotal);
        
        log.debug("Pontuação do atleta ID {} atualizada para {} pontos", 
                atletaId, pontuacaoTotal);
    }

    public void atualizarPontuacaoAposSalvarResultado(LeaderboardEntity leaderboard) {
        if (leaderboard == null) {
            return;
        }

        if (leaderboard.isCategoriaEquipe() && leaderboard.getEquipe() != null) {
            atualizarPontuacaoEquipe(leaderboard.getEquipe());
        } else if (leaderboard.isCategoriaIndividual() && leaderboard.getAtleta() != null) {
            atualizarPontuacaoAtleta(leaderboard.getAtleta());
        }
    }

    public void atualizarPontuacaoAposExcluirResultado(LeaderboardEntity leaderboard) {
        atualizarPontuacaoAposSalvarResultado(leaderboard);
    }

    public void recalcularTodasPontuacoesPorCategoria(Long categoriaId) {
        log.info("Iniciando recálculo de todas as pontuações para categoria ID: {}", categoriaId);

        List<LeaderboardEntity> todosResultados = leaderboardRepository.findByCategoriaIdOrderByPosicaoWorkoutAsc(categoriaId);
        
        if (todosResultados.isEmpty()) {
            log.info("Nenhum resultado encontrado para a categoria ID: {}", categoriaId);
            return;
        }

        boolean isCategoriaEquipe = todosResultados.get(0).isCategoriaEquipe();

        if (isCategoriaEquipe) {
            todosResultados.stream()
                    .filter(r -> r.getEquipe() != null)
                    .map(r -> r.getEquipe().getId())
                    .distinct()
                    .forEach(this::atualizarPontuacaoEquipePorId);
        } else {
            todosResultados.stream()
                    .filter(r -> r.getAtleta() != null)
                    .map(r -> r.getAtleta().getId())
                    .distinct()
                    .forEach(this::atualizarPontuacaoAtletaPorId);
        }

        log.info("Recálculo de pontuações concluído para categoria ID: {}", categoriaId);
    }

    public void adicionarPontosPorPosicao(LeaderboardEntity leaderboard, Integer posicaoAntiga) {
        if (leaderboard == null || leaderboard.getPosicaoWorkout() == null) {
            return;
        }

        Integer novaPosicao = leaderboard.getPosicaoWorkout();
        Integer diferenca = 0;

        if (posicaoAntiga != null) {
            diferenca = posicaoAntiga - novaPosicao;
        } else {
            diferenca = -novaPosicao;
        }

        if (leaderboard.isCategoriaEquipe() && leaderboard.getEquipe() != null) {
            if (diferenca > 0) {
                leaderboard.getEquipe().subtrairPontos(diferenca);
            } else if (diferenca < 0) {
                leaderboard.getEquipe().adicionarPontos(-diferenca);
            }
        } else if (leaderboard.isCategoriaIndividual() && leaderboard.getAtleta() != null) {
            // Para UsuarioEntity será necessário implementar métodos de pontuação depois
            log.debug("Ajuste de pontuação para usuário {} - diferença: {}", 
                    leaderboard.getAtleta().getNomeCompleto(), diferenca);
        }
    }

    public void removerPontosPorPosicao(LeaderboardEntity leaderboard) {
        if (leaderboard == null || leaderboard.getPosicaoWorkout() == null) {
            return;
        }

        Integer pontos = leaderboard.getPosicaoWorkout();

        if (leaderboard.isCategoriaEquipe() && leaderboard.getEquipe() != null) {
            leaderboard.getEquipe().subtrairPontos(pontos);
        } else if (leaderboard.isCategoriaIndividual() && leaderboard.getAtleta() != null) {
            // Para UsuarioEntity será necessário implementar método subtrairPontos depois
            log.debug("Remoção de {} pontos para usuário {}", pontos, 
                    leaderboard.getAtleta().getNomeCompleto());
        }
    }
}