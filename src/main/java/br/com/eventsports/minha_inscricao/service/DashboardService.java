package br.com.eventsports.minha_inscricao.service;

import br.com.eventsports.minha_inscricao.dto.dashboard.DashboardEstatisticasDTO;
import br.com.eventsports.minha_inscricao.dto.dashboard.DashboardEstatisticasDTO.*;
import br.com.eventsports.minha_inscricao.entity.EventoEntity;
import br.com.eventsports.minha_inscricao.entity.InscricaoEntity;
import br.com.eventsports.minha_inscricao.entity.UsuarioEntity;
import br.com.eventsports.minha_inscricao.enums.StatusEvento;
import br.com.eventsports.minha_inscricao.enums.StatusInscricao;
import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import br.com.eventsports.minha_inscricao.repository.EventoRepository;
import br.com.eventsports.minha_inscricao.repository.InscricaoRepository;
import br.com.eventsports.minha_inscricao.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final EventoRepository eventoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Retorna todas as estatísticas consolidadas do dashboard
     */
    public DashboardEstatisticasDTO obterEstatisticas() {
        log.info("Obtendo estatísticas do dashboard");

        return DashboardEstatisticasDTO.builder()
                .eventos(obterEstatisticasEventos())
                .inscricoes(obterEstatisticasInscricoes())
                .usuarios(obterEstatisticasUsuarios())
                .financeiro(obterEstatisticasFinanceiras())
                .inscricoesPorMes(obterInscricoesPorMes())
                .eventosMaisPopulares(obterEventosMaisPopulares())
                .build();
    }

    /**
     * Estatísticas de eventos por status
     */
    private EventoEstatisticasDTO obterEstatisticasEventos() {
        List<EventoEntity> eventos = eventoRepository.findAll();

        return EventoEstatisticasDTO.builder()
                .total((long) eventos.size())
                .rascunho(contarPorStatus(eventos, StatusEvento.RASCUNHO))
                .publicado(0L) // StatusEvento não tem PUBLICADO, mantém 0
                .inscricoesAbertas(contarPorStatus(eventos, StatusEvento.ABERTO))
                .inscricoesEncerradas(contarPorStatus(eventos, StatusEvento.INSCRICOES_ENCERRADAS))
                .emAndamento(contarPorStatus(eventos, StatusEvento.EM_ANDAMENTO))
                .concluido(contarPorStatus(eventos, StatusEvento.FINALIZADO))
                .cancelado(contarPorStatus(eventos, StatusEvento.CANCELADO))
                .build();
    }

    /**
     * Estatísticas de inscrições por status
     */
    private InscricaoEstatisticasDTO obterEstatisticasInscricoes() {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findAll();

        return InscricaoEstatisticasDTO.builder()
                .total((long) inscricoes.size())
                .pendente(contarPorStatusInscricao(inscricoes, StatusInscricao.PENDENTE))
                .confirmada(contarPorStatusInscricao(inscricoes, StatusInscricao.CONFIRMADA))
                .cancelada(contarPorStatusInscricao(inscricoes, StatusInscricao.CANCELADA))
                .aguardandoPagamento(contarPorStatusInscricao(inscricoes, StatusInscricao.PENDENTE)) // PENDENTE = aguardando pagamento
                .expirada(contarPorStatusInscricao(inscricoes, StatusInscricao.EXPIRADA))
                .build();
    }

    /**
     * Estatísticas de usuários por tipo
     */
    private UsuarioEstatisticasDTO obterEstatisticasUsuarios() {
        List<UsuarioEntity> usuarios = usuarioRepository.findAll();

        long admins = usuarios.stream()
                .filter(u -> u.getTipoUsuario() == TipoUsuario.ADMIN)
                .count();

        long organizadores = usuarios.stream()
                .filter(u -> u.getTipoUsuario() == TipoUsuario.ORGANIZADOR)
                .count();

        long atletas = usuarios.stream()
                .filter(u -> u.getTipoUsuario() == TipoUsuario.ATLETA)
                .count();

        long ativos = usuarios.stream()
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .count();

        long inativos = usuarios.size() - ativos;

        return UsuarioEstatisticasDTO.builder()
                .total((long) usuarios.size())
                .admins(admins)
                .organizadores(organizadores)
                .atletas(atletas)
                .ativos(ativos)
                .inativos(inativos)
                .build();
    }

    /**
     * Estatísticas financeiras simuladas
     * (Como pagamentos não estão implementados, simulamos com valores fixos por inscrição)
     */
    private FinanceiroEstatisticasDTO obterEstatisticasFinanceiras() {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findAll();

        // Valor médio simulado por inscrição
        BigDecimal valorMedioPorInscricao = new BigDecimal("150.00");

        long inscricoesConfirmadas = contarPorStatusInscricao(inscricoes, StatusInscricao.CONFIRMADA);
        long inscricoesPendentes = contarPorStatusInscricao(inscricoes, StatusInscricao.PENDENTE);

        BigDecimal receitaTotal = valorMedioPorInscricao.multiply(new BigDecimal(inscricoesConfirmadas));
        BigDecimal receitaPendente = valorMedioPorInscricao.multiply(new BigDecimal(inscricoesPendentes));

        return FinanceiroEstatisticasDTO.builder()
                .receitaTotal(receitaTotal)
                .receitaPendente(receitaPendente)
                .inscricoesPagas(inscricoesConfirmadas)
                .inscricoesPendentes(inscricoesPendentes)
                .build();
    }

    /**
     * Inscrições agrupadas por mês (últimos 12 meses)
     */
    private List<EstatisticaPorMesDTO> obterInscricoesPorMes() {
        List<InscricaoEntity> inscricoes = inscricaoRepository.findAll();
        LocalDateTime dataLimite = LocalDateTime.now().minusMonths(12);

        // Filtra inscrições dos últimos 12 meses
        List<InscricaoEntity> inscricoesRecentes = inscricoes.stream()
                .filter(i -> i.getCreatedAt() != null && i.getCreatedAt().isAfter(dataLimite))
                .collect(Collectors.toList());

        // Agrupa por mês
        Map<YearMonth, Long> inscricoesPorMes = inscricoesRecentes.stream()
                .collect(Collectors.groupingBy(
                        i -> YearMonth.from(i.getCreatedAt()),
                        Collectors.counting()
                ));

        // Gera lista dos últimos 12 meses (mesmo que não tenha inscrições)
        List<EstatisticaPorMesDTO> resultado = new ArrayList<>();
        YearMonth mesAtual = YearMonth.now();

        for (int i = 11; i >= 0; i--) {
            YearMonth mes = mesAtual.minusMonths(i);
            Long quantidade = inscricoesPorMes.getOrDefault(mes, 0L);

            resultado.add(EstatisticaPorMesDTO.builder()
                    .mes(mes.toString())
                    .mesNome(mes.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")) + " " + mes.getYear())
                    .quantidade(quantidade)
                    .build());
        }

        return resultado;
    }

    /**
     * Top 5 eventos com mais inscrições
     */
    private List<EventoPopularDTO> obterEventosMaisPopulares() {
        List<EventoEntity> eventos = eventoRepository.findAll();

        return eventos.stream()
                .map(evento -> EventoPopularDTO.builder()
                        .id(evento.getId())
                        .nome(evento.getNome())
                        .totalInscricoes((long) evento.getInscricoes().size())
                        .status(evento.getStatus().name())
                        .build())
                .sorted(Comparator.comparing(EventoPopularDTO::getTotalInscricoes).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    // Métodos auxiliares

    private Long contarPorStatus(List<EventoEntity> eventos, StatusEvento status) {
        return eventos.stream()
                .filter(e -> e.getStatus() == status)
                .count();
    }

    private Long contarPorStatusInscricao(List<InscricaoEntity> inscricoes, StatusInscricao status) {
        return inscricoes.stream()
                .filter(i -> i.getStatus() == status)
                .count();
    }
}
