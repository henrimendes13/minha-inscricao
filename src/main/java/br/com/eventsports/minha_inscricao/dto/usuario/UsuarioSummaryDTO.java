package br.com.eventsports.minha_inscricao.dto.usuario;

import br.com.eventsports.minha_inscricao.enums.TipoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de resumo do usuário")
public class UsuarioSummaryDTO {

    @Schema(description = "ID único do usuário", example = "1")
    private Long id;

    @Schema(description = "Email do usuário", example = "joao@exemplo.com")
    private String email;

    @Schema(description = "Nome completo do usuário", example = "João Silva Santos")
    private String nome;

    @Schema(description = "Tipo do usuário no sistema", example = "ATLETA")
    private TipoUsuario tipo;

    @Schema(description = "Indica se o usuário está ativo", example = "true")
    private Boolean ativo;
}
