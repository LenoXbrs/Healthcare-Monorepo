package br.com.bali.code.healthcareapiusuarios.Usuario.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_triagem_log")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventoTriagemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(name = "triagem_id", nullable = false)
    private Long triagemId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(name = "recebido_em", nullable = false)
    private LocalDateTime recebidoEm;
}
