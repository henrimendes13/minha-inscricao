package br.com.eventsports.minha_inscricao.service.Interfaces;

import br.com.eventsports.minha_inscricao.entity.AtletaEntity;
import br.com.eventsports.minha_inscricao.entity.EquipeEntity;
import br.com.eventsports.minha_inscricao.entity.LeaderboardEntity;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;

/**
 * Interface para serviços de gerenciamento de pontuação.
 * 
 * Define os contratos para operações de cálculo e atualização de pontuações
 * de atletas e equipes baseado em seus resultados nos workouts.
 */
public interface IPontuacaoService {

    /**
     * Atualiza a pontuação total de um atleta baseado em seus resultados
     * 
     * @param atleta O atleta para atualizar a pontuação
     */
    void atualizarPontuacaoAtleta(AtletaEntity atleta);

    /**
     * Atualiza a pontuação total de uma equipe baseado em seus resultados
     * 
     * @param equipe A equipe para atualizar a pontuação
     */
    void atualizarPontuacaoEquipe(EquipeEntity equipe);

    /**
     * Atualiza a pontuação total de um usuário baseado em seus resultados
     * 
     * @param usuario O usuário para atualizar a pontuação
     */
    void atualizarPontuacaoUsuario(UsuarioEntity usuario);

    /**
     * Atualiza a pontuação do participante após salvar um resultado
     * 
     * @param leaderboard O resultado do leaderboard que foi salvo
     */
    void atualizarPontuacaoAposSalvarResultado(LeaderboardEntity leaderboard);

    /**
     * Atualiza a pontuação do participante após excluir um resultado
     * 
     * @param leaderboard O resultado do leaderboard que foi excluído
     */
    void atualizarPontuacaoAposExcluirResultado(LeaderboardEntity leaderboard);

    /**
     * Recalcula todas as pontuações dos participantes de uma categoria
     * 
     * @param categoriaId O ID da categoria para recalcular pontuações
     */
    void recalcularTodasPontuacoesPorCategoria(Long categoriaId);

    /**
     * Adiciona pontos baseado na mudança de posição
     * 
     * @param leaderboard O resultado do leaderboard
     * @param posicaoAntiga A posição anterior (null se for novo resultado)
     */
    void adicionarPontosPorPosicao(LeaderboardEntity leaderboard, Integer posicaoAntiga);

    /**
     * Remove pontos quando um resultado é excluído
     * 
     * @param leaderboard O resultado do leaderboard a ser removido
     */
    void removerPontosPorPosicao(LeaderboardEntity leaderboard);
}