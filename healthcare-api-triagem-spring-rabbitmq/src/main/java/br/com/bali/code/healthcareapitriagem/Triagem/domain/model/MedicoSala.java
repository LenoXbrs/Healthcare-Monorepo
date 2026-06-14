package br.com.bali.code.healthcareapitriagem.Triagem.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "medico_salas",
        indexes = {
                @Index(name = "idx_medico_sala_medico", columnList = "medico_id", unique = true)
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MedicoSala extends BaseAuditEntity {

    @Column(name = "medico_id", nullable = false, unique = true)
    private Long medicoId;

    @Column(name = "sala", nullable = false)
    private String sala;
}
