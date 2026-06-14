package br.com.bali.code.healthcareapitriagem.Triagem.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sinais_vitais")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SinaisVitais {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triagem_id", nullable = false, unique = true)
    private Triagem triagem;

    @Column(name = "pressao_sist", nullable = false)
    private Integer pressaoSist;

    @Column(name = "pressao_diast", nullable = false)
    private Integer pressaoDiast;

    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(nullable = false)
    private Integer frequencia;

    @Column(nullable = false)
    private Integer saturacao;

    @Column(name = "coletado_em", nullable = false)
    private LocalDateTime coletadoEm;

    @PrePersist
    protected void prePersist() {
        if (coletadoEm == null) {
            coletadoEm = LocalDateTime.now();
        }
    }
}
