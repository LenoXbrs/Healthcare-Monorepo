package br.com.bali.code.healthcareapitriagem.Triagem.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medicamentos_prescritos")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "diagnostico")
@EqualsAndHashCode(exclude = "diagnostico")
public class MedicamentoPrescrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diagnostico_id", nullable = false)
    @JsonIgnore
    private Diagnostico diagnostico;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String dosagem;

    @Column(nullable = false)
    private String frequencia;

    @Column(name = "prazo_uso", nullable = false)
    private String prazoUso;
}
