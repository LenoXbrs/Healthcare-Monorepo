package br.com.bali.code.healthcareapipackientes.Paciente.domain.model;

import br.com.bali.code.healthcareapipackientes.Paciente.application.StatusPaciente;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "pacientes",
        indexes = {
                @Index(name = "idx_paciente_cpf", columnList = "cpf")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Paciente extends BaseAuditEntity {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPaciente status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;
}
