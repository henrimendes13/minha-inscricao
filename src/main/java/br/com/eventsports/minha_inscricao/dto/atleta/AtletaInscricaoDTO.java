package br.com.eventsports.minha_inscricao.dto.atleta;

import br.com.eventsports.minha_inscricao.enums.Genero;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dados para criação de atleta individual com inscrição em evento")
public class AtletaInscricaoDTO {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    @Schema(description = "Nome completo do atleta", example = "João Silva Santos", required = true)
    private String nome;

    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", 
             message = "CPF deve estar no formato XXX.XXX.XXX-XX")
    @Schema(description = "CPF do atleta", example = "123.456.789-00")
    private String cpf;

    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    @Schema(description = "Data de nascimento do atleta", example = "1990-05-15", required = true)
    private LocalDate dataNascimento;

    @NotNull(message = "Gênero é obrigatório")
    @Schema(description = "Gênero do atleta", example = "MASCULINO", required = true)
    private Genero genero;

    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", 
             message = "Telefone deve estar no formato (XX) XXXXX-XXXX")
    @Schema(description = "Telefone de contato", example = "(11) 99999-9999")
    private String telefone;

    @Size(max = 100, message = "Nome de emergência deve ter no máximo 100 caracteres")
    @Schema(description = "Nome do contato de emergência", example = "Maria Silva Santos")
    private String emergenciaNome;

    @Pattern(regexp = "\\(\\d{2}\\)\\s\\d{4,5}-\\d{4}", 
             message = "Telefone de emergência deve estar no formato (XX) XXXXX-XXXX")
    @Schema(description = "Telefone do contato de emergência", example = "(11) 88888-8888")
    private String emergenciaTelefone;

    @Size(max = 500, message = "Observações médicas devem ter no máximo 500 caracteres")
    @Schema(description = "Observações médicas importantes", 
            example = "Alérgico a anti-inflamatórios. Problema no joelho direito.")
    private String observacoesMedicas;

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    @Schema(description = "Endereço completo", example = "Rua das Palmeiras, 456 - Vila Nova - São Paulo/SP")
    private String endereco;

    @Email(message = "Email deve ter formato válido")
    @Size(max = 150, message = "Email deve ter no máximo 150 caracteres")
    @Schema(description = "Email para notificações (opcional)", example = "joao@email.com")
    private String email;

    @NotNull(message = "Aceite dos termos é obrigatório")
    @Schema(description = "Indica se aceitou os termos", example = "true", required = true)
    private Boolean aceitaTermos;

    // Campos específicos para inscrição
    @NotNull(message = "Categoria é obrigatória")
    @Schema(description = "ID da categoria em que o atleta irá competir", example = "5", required = true)
    private Long categoriaId;

    @Schema(description = "Valor personalizado da inscrição (opcional - se não informado, será usado o valor da categoria)", 
            example = "75.00")
    private BigDecimal valorInscricao;

    @Schema(description = "Código de desconto aplicável (opcional)", 
            example = "DESCONTO10")
    private String codigoDesconto;

    @NotNull(message = "Aceitação dos termos e condições da inscrição é obrigatória")
    @Schema(description = "Confirmação de que o atleta aceita os termos e condições do evento", 
            example = "true", required = true)
    private Boolean termosInscricaoAceitos;
}