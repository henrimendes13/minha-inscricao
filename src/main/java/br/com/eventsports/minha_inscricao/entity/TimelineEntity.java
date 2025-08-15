package br.com.eventsports.minha_inscricao.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "timelines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Cronograma de um evento esportivo")
public class TimelineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único da timeline", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Size(max = 5000, message = "Descrição do dia um deve ter no máximo 5000 caracteres")
    @Lob
    @Column(name = "descricao_dia_um", columnDefinition = "TEXT")
    @Schema(description = "Cronograma do primeiro dia do evento", 
            example = "08:00 - Check-in dos atletas | 09:00 - Aquecimento | 10:00 - Primeira prova")
    private String descricaoDiaUm;

    @Size(max = 5000, message = "Descrição do dia dois deve ter no máximo 5000 caracteres")
    @Lob
    @Column(name = "descricao_dia_dois", columnDefinition = "TEXT")
    @Schema(description = "Cronograma do segundo dia do evento", 
            example = "08:00 - Check-in | 09:00 - Aquecimento | 10:00 - Segunda prova")
    private String descricaoDiaDois;

    @Size(max = 5000, message = "Descrição do dia três deve ter no máximo 5000 caracteres")
    @Lob
    @Column(name = "descricao_dia_tres", columnDefinition = "TEXT")
    @Schema(description = "Cronograma do terceiro dia do evento", 
            example = "08:00 - Check-in | 09:00 - Aquecimento | 10:00 - Terceira prova")
    private String descricaoDiaTres;

    @Size(max = 5000, message = "Descrição do dia quatro deve ter no máximo 5000 caracteres")
    @Lob
    @Column(name = "descricao_dia_quatro", columnDefinition = "TEXT")
    @Schema(description = "Cronograma do quarto dia do evento", 
            example = "08:00 - Check-in | 09:00 - Aquecimento | 10:00 - Prova final | 16:00 - Premiação")
    private String descricaoDiaQuatro;

    // Relacionamento com Evento (One-to-One)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false, unique = true)
    @Schema(description = "Evento ao qual a timeline pertence")
    private EventoEntity evento;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Schema(description = "Data de criação", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Schema(description = "Data da última atualização", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    // Lifecycle methods
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de conveniência
    public boolean temDescricaoDiaUm() {
        return this.descricaoDiaUm != null && !this.descricaoDiaUm.trim().isEmpty();
    }

    public boolean temDescricaoDiaDois() {
        return this.descricaoDiaDois != null && !this.descricaoDiaDois.trim().isEmpty();
    }

    public boolean temDescricaoDiaTres() {
        return this.descricaoDiaTres != null && !this.descricaoDiaTres.trim().isEmpty();
    }

    public boolean temDescricaoDiaQuatro() {
        return this.descricaoDiaQuatro != null && !this.descricaoDiaQuatro.trim().isEmpty();
    }

    public int getTotalDiasComDescricao() {
        int total = 0;
        if (temDescricaoDiaUm()) total++;
        if (temDescricaoDiaDois()) total++;
        if (temDescricaoDiaTres()) total++;
        if (temDescricaoDiaQuatro()) total++;
        return total;
    }

    public String getNomeEvento() {
        return this.evento != null ? this.evento.getNome() : "";
    }

    public String getDescricaoCompleta() {
        StringBuilder sb = new StringBuilder();
        
        if (temDescricaoDiaUm()) {
            sb.append("Dia 1: ").append(this.descricaoDiaUm);
        }
        
        if (temDescricaoDiaDois()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Dia 2: ").append(this.descricaoDiaDois);
        }
        
        if (temDescricaoDiaTres()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Dia 3: ").append(this.descricaoDiaTres);
        }
        
        if (temDescricaoDiaQuatro()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Dia 4: ").append(this.descricaoDiaQuatro);
        }
        
        return sb.toString();
    }

    public boolean isVazia() {
        return !temDescricaoDiaUm() && !temDescricaoDiaDois() && 
               !temDescricaoDiaTres() && !temDescricaoDiaQuatro();
    }
}
