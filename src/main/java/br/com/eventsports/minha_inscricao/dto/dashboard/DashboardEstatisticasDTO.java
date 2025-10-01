package br.com.eventsports.minha_inscricao.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Estatísticas gerais do dashboard administrativo")
public class DashboardEstatisticasDTO {

    @Schema(description = "Estatísticas de eventos")
    private EventoEstatisticasDTO eventos;

    @Schema(description = "Estatísticas de inscrições")
    private InscricaoEstatisticasDTO inscricoes;

    @Schema(description = "Estatísticas de usuários")
    private UsuarioEstatisticasDTO usuarios;

    @Schema(description = "Estatísticas financeiras simuladas")
    private FinanceiroEstatisticasDTO financeiro;

    @Schema(description = "Inscrições por mês nos últimos 12 meses")
    private List<EstatisticaPorMesDTO> inscricoesPorMes;

    @Schema(description = "Top 5 eventos mais populares (por número de inscrições)")
    private List<EventoPopularDTO> eventosMaisPopulares;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventoEstatisticasDTO {
        @Schema(description = "Total de eventos no sistema", example = "50")
        private Long total;

        @Schema(description = "Eventos com status RASCUNHO", example = "5")
        private Long rascunho;

        @Schema(description = "Eventos com status PUBLICADO", example = "15")
        private Long publicado;

        @Schema(description = "Eventos com status INSCRICOES_ABERTAS", example = "10")
        private Long inscricoesAbertas;

        @Schema(description = "Eventos com status INSCRICOES_ENCERRADAS", example = "8")
        private Long inscricoesEncerradas;

        @Schema(description = "Eventos com status EM_ANDAMENTO", example = "3")
        private Long emAndamento;

        @Schema(description = "Eventos com status CONCLUIDO", example = "9")
        private Long concluido;

        @Schema(description = "Eventos com status CANCELADO", example = "0")
        private Long cancelado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InscricaoEstatisticasDTO {
        @Schema(description = "Total de inscrições no sistema", example = "250")
        private Long total;

        @Schema(description = "Inscrições com status PENDENTE", example = "20")
        private Long pendente;

        @Schema(description = "Inscrições com status CONFIRMADA", example = "180")
        private Long confirmada;

        @Schema(description = "Inscrições com status CANCELADA", example = "30")
        private Long cancelada;

        @Schema(description = "Inscrições com status AGUARDANDO_PAGAMENTO", example = "15")
        private Long aguardandoPagamento;

        @Schema(description = "Inscrições com status EXPIRADA", example = "5")
        private Long expirada;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsuarioEstatisticasDTO {
        @Schema(description = "Total de usuários no sistema", example = "180")
        private Long total;

        @Schema(description = "Usuários do tipo ADMIN", example = "2")
        private Long admins;

        @Schema(description = "Usuários do tipo ORGANIZADOR", example = "15")
        private Long organizadores;

        @Schema(description = "Usuários do tipo ATLETA", example = "163")
        private Long atletas;

        @Schema(description = "Usuários ativos", example = "175")
        private Long ativos;

        @Schema(description = "Usuários inativos", example = "5")
        private Long inativos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FinanceiroEstatisticasDTO {
        @Schema(description = "Receita total simulada (valor total de todas inscrições confirmadas)", example = "25000.00")
        private BigDecimal receitaTotal;

        @Schema(description = "Receita pendente (inscrições aguardando pagamento)", example = "1500.00")
        private BigDecimal receitaPendente;

        @Schema(description = "Total de inscrições pagas", example = "180")
        private Long inscricoesPagas;

        @Schema(description = "Total de inscrições pendentes de pagamento", example = "15")
        private Long inscricoesPendentes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstatisticaPorMesDTO {
        @Schema(description = "Mês no formato YYYY-MM", example = "2025-01")
        private String mes;

        @Schema(description = "Nome do mês para exibição", example = "Janeiro 2025")
        private String mesNome;

        @Schema(description = "Quantidade de inscrições no mês", example = "25")
        private Long quantidade;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventoPopularDTO {
        @Schema(description = "ID do evento", example = "1")
        private Long id;

        @Schema(description = "Nome do evento", example = "CrossFit Open 2025")
        private String nome;

        @Schema(description = "Total de inscrições", example = "150")
        private Long totalInscricoes;

        @Schema(description = "Status do evento", example = "INSCRICOES_ABERTAS")
        private String status;
    }
}
