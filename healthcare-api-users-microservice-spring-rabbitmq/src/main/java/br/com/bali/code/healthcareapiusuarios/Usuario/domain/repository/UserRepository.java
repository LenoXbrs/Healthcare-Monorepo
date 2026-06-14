package br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository;

import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.bali.code.healthcareapiusuarios.Usuario.application.Role;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>  {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);

    List<User> findByRoleAndActiveTrue(Role role);
}
