package br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository;

import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.EventoTriagemLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoTriagemLogRepository extends JpaRepository<EventoTriagemLog, Long> {
}
