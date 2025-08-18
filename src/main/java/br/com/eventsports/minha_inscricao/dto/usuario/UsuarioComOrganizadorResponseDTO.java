package br.com.eventsports.minha_inscricao.dto.usuario;

import br.com.eventsports.minha_inscricao.dto.organizador.OrganizadorResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de resposta para criação completa de usuário organizador")
public class UsuarioComOrganizadorResponseDTO {

    @Schema(description = "Dados do usuário criado")
    private UsuarioResponseDTO usuario;

    @Schema(description = "Dados do organizador criado (null se não for organizador ou se não foi criado)")
    private OrganizadorResponseDTO organizador;

    @Schema(description = "Indica se o perfil está completo", example = "true")
    private Boolean perfilCompleto;

    @Schema(description = "Próximo passo necessário (se aplicável)", example = "criar-organizador")
    private String proximoPasso;

    @Schema(description = "Mensagem informativa sobre o status do perfil")
    private String mensagem;

    // Método utilitário para criar resposta com perfil completo
    public static UsuarioComOrganizadorResponseDTO criarCompleto(
            UsuarioResponseDTO usuario, 
            OrganizadorResponseDTO organizador) {
        return UsuarioComOrganizadorResponseDTO.builder()
                .usuario(usuario)
                .organizador(organizador)
                .perfilCompleto(true)
                .proximoPasso(null)
                .mensagem("Perfil de organizador criado com sucesso. Aguarde verificação para organizar eventos.")
                .build();
    }

    // Método utilitário para criar resposta com perfil incompleto
    public static UsuarioComOrganizadorResponseDTO criarIncompleto(
            UsuarioResponseDTO usuario, 
            String proximoPasso,
            String mensagem) {
        return UsuarioComOrganizadorResponseDTO.builder()
                .usuario(usuario)
                .organizador(null)
                .perfilCompleto(false)
                .proximoPasso(proximoPasso)
                .mensagem(mensagem)
                .build();
    }
}
