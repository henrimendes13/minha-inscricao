package br.com.eventsports.minha_inscricao.dto.usuario;

import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de resposta completa do usuário - Tipo determinado dinamicamente")
public class UsuarioResponseDTO {

    @Schema(description = "ID único do usuário", example = "1")
    private Long id;

    @Schema(description = "Email do usuário (login)", example = "joao@exemplo.com")
    private String email;

    @Schema(description = "Nome completo do usuário", example = "João Silva Santos")
    private String nome;

    @Schema(description = "Tipo atual do usuário (baseado nas ações)", example = "ATLETA")
    private TipoUsuario tipo;

    @Schema(description = "Indica se é organizador (tem eventos)", example = "false")
    private Boolean isOrganizador;

    @Schema(description = "Indica se é atleta (tem inscrições)", example = "true") 
    private Boolean isAtleta;

    @Schema(description = "Indica se o usuário está ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Indica se está verificado (para organizar eventos)", example = "false")
    private Boolean verificado;

    @Schema(description = "Total de eventos organizados", example = "0")
    private Integer totalEventos;

    @Schema(description = "Total de inscrições", example = "2")
    private Integer totalInscricoes;

    @Schema(description = "Data/hora do último login")
    private LocalDateTime ultimoLogin;

    @Schema(description = "Data de criação do usuário")
    private LocalDateTime createdAt;

    @Schema(description = "Data da última atualização")
    private LocalDateTime updatedAt;
}
