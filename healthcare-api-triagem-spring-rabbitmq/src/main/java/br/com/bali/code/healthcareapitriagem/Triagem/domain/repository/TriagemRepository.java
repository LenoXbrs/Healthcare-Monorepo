package br.com.bali.code.healthcareapitriagem.Triagem.domain.repository;

import br.com.bali.code.healthcareapitriagem.Triagem.application.StatusTriagem;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.Triagem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    @EntityGraph(attributePaths = "sinaisVitais")
    Optional<Triagem> findWithSinaisVitaisById(Long id);

    Optional<Triagem> findByPacienteId(Long pacienteId);

    boolean existsByPacienteId(Long pacienteId);

    boolean existsByPacienteIdAndStatusNot(Long pacienteId, StatusTriagem status);

    Optional<Triagem> findByPacienteIdAndStatus(Long pacienteId, StatusTriagem status);

    List<Triagem> findAllByOrderByCreatedAtDesc();

    long countByMedicoIdAndStatus(Long medicoId, StatusTriagem status);
}
