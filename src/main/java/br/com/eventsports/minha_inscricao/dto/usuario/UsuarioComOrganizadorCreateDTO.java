package br.com.eventsports.minha_inscricao.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para criação completa de usuário organizador")
public class UsuarioComOrganizadorCreateDTO {

    @NotNull(message = "Dados do usuário são obrigatórios")
    @Valid
    @Schema(description = "Dados básicos do usuário", required = true)
    private UsuarioCreateDTO usuario;

    @Valid
    @Schema(description = "Dados do organizador (obrigatório se usuário for ORGANIZADOR)")
    private OrganizadorCreateDTOSemUsuario organizador;

    // DTO simplificado do organizador (sem usuarioId pois será preenchido automaticamente)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "DTO para criação de organizador sem ID do usuário")
    public static class OrganizadorCreateDTOSemUsuario {

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
    }
}
