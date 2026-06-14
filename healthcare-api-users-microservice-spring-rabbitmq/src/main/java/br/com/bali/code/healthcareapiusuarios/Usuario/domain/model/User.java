package br.com.bali.code.healthcareapiusuarios.Usuario.domain.model;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseAuditEntity {
    @Column
    private String name;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private Boolean active = true;
}
