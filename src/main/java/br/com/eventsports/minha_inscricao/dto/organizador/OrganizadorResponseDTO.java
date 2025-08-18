package br.com.eventsports.minha_inscricao.dto.organizador;

import br.com.eventsports.minha_inscricao.dto.usuario.UsuarioSummaryDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de resposta completa do organizador")
public class OrganizadorResponseDTO {

    @Schema(description = "ID único do organizador", example = "1")
    private Long id;

    @Schema(description = "Dados do usuário associado")
    private UsuarioSummaryDTO usuario;

    @Schema(description = "Nome da empresa organizadora", example = "EventSports LTDA")
    private String nomeEmpresa;

    @Schema(description = "CNPJ da empresa", example = "12.345.678/0001-90")
    private String cnpj;

    @Schema(description = "Telefone de contato", example = "(11) 99999-9999")
    private String telefone;

    @Schema(description = "Endereço completo", example = "Rua das Flores, 123 - Centro - São Paulo/SP")
    private String endereco;

    @Schema(description = "Descrição do organizador/empresa", 
            example = "Empresa especializada em eventos esportivos de CrossFit")
    private String descricao;

    @Schema(description = "Site da empresa", example = "https://www.eventsports.com.br")
    private String site;

    @Schema(description = "Indica se o organizador foi verificado", example = "true")
    private Boolean verificado;

    @Schema(description = "Data de criação")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização")
    private LocalDateTime updatedAt;

    @Schema(description = "Nome para exibição (empresa ou usuário)")
    private String nomeExibicao;

    @Schema(description = "Total de eventos criados")
    private Integer totalEventos;

    @Schema(description = "Indica se pode organizar eventos")
    private Boolean podeOrganizarEventos;
}
