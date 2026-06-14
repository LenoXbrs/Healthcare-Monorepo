package br.com.bali.code.healthcareapitriagem.Triagem.domain.model;

import br.com.bali.code.healthcareapitriagem.Triagem.application.PrioridadeTriagem;
import br.com.bali.code.healthcareapitriagem.Triagem.application.StatusTriagem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "triagens",
        indexes = {
                @Index(name = "idx_triagem_paciente", columnList = "paciente_id"),
                @Index(name = "idx_triagem_status", columnList = "status")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Triagem extends BaseAuditEntity {

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "enfermeiro_id")
    private Long enfermeiroId;

    @Column(name = "medico_id")
    private Long medicoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTriagem status;

    @Enumerated(EnumType.STRING)
    private PrioridadeTriagem prioridade;

    @Column(name = "queixa_principal", columnDefinition = "TEXT")
    private String queixaPrincipal;

    @Column(name = "classificado_em")
    private LocalDateTime classificadoEm;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "chamado")
    private Boolean chamado = false;

    @Column(name = "sala_chamada")
    private String salaChamada;

    @Column(name = "chamado_em")
    private java.time.LocalDateTime chamadoEm;

    @OneToOne(mappedBy = "triagem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SinaisVitais sinaisVitais;

    @OneToOne(mappedBy = "triagem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Diagnostico diagnostico;
}
