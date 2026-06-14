package br.com.bali.code.healthcareapitriagem.Triagem.domain.repository;

import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.MedicoSala;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MedicoSalaRepository extends JpaRepository<MedicoSala, Long> {
    Optional<MedicoSala> findByMedicoId(Long medicoId);
}
