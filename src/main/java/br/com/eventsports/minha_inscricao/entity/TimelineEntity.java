package br.com.eventsports.minha_inscricao.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "timelines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimelineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao_dia_um", columnDefinition = "TEXT")
    private String descricaoDiaUm;

    @Column(name = "descricao_dia_dois", columnDefinition = "TEXT")
    private String descricaoDiaDois;

    @Column(name = "descricao_dia_tres", columnDefinition = "TEXT")
    private String descricaoDiaTres;

    @Column(name = "descricao_dia_quatro", columnDefinition = "TEXT")
    private String descricaoDiaQuatro;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false, unique = true)
    private EventoEntity evento;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
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
