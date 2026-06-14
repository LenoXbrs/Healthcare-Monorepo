package br.com.bali.code.healthcareapiusuarios.Usuario.domain.model;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.TipoNotificacao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notificacoes",
        indexes = {
                @Index(name = "idx_notificacao_usuario", columnList = "usuario_id"),
                @Index(name = "idx_notificacao_lida", columnList = "lida")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacao tipo;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "triagem_id")
    private Long triagemId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean lida = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }
}
