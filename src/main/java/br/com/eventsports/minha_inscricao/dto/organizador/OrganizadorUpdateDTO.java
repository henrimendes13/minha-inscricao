package br.com.eventsports.minha_inscricao.dto.organizador;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para atualização de organizador")
public class OrganizadorUpdateDTO {

    @Size(max = 100, message = "Nome da empresa deve ter no máximo 100 caracteres")
    @Schema(description = "Nome da empresa organizadora", example = "EventSports LTDA")
    private String nomeEmpresa;

    @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}", 
             message = "CNPJ deve estar no formato XX.XXX.XXX/XXXX-XX")
    @Schema(description = "CNPJ da empresa", example = "12.345.678/0001-90")
    private String cnpj;

    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", 
             message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    @Schema(description = "Telefone de contato", example = "(11) 99999-9999")
    private String telefone;

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    @Schema(description = "Endereço completo", example = "Rua das Flores, 123 - Centro - São Paulo/SP")
    private String endereco;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Schema(description = "Descrição do organizador/empresa", 
            example = "Empresa especializada em eventos esportivos de CrossFit")
    private String descricao;

    @Size(max = 100, message = "Site deve ter no máximo 100 caracteres")
    @Schema(description = "Site da empresa", example = "https://www.eventsports.com.br")
    private String site;

    @Schema(description = "Indica se o organizador foi verificado", example = "true")
    private Boolean verificado;
}
