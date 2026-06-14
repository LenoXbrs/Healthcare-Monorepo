package br.com.bali.code.healthcareapitriagem.Triagem.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "diagnosticos",
        indexes = {
                @Index(name = "idx_diagnostico_triagem", columnList = "triagem_id"),
                @Index(name = "idx_diagnostico_medico", columnList = "medico_id")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "medicamentos")
public class Diagnostico extends BaseAuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triagem_id", nullable = false, unique = true)
    private Triagem triagem;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDiagnostico status;

    @Column(name = "assinatura_hash")
    private String assinaturaHash;

    @Column(name = "assinatura_base64", columnDefinition = "TEXT")
    private String assinaturaBase64;

    @Column(name = "assinado_em")
    private LocalDateTime assinadoEm;

    @OneToMany(mappedBy = "diagnostico", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MedicamentoPrescrito> medicamentos = new ArrayList<>();
}
