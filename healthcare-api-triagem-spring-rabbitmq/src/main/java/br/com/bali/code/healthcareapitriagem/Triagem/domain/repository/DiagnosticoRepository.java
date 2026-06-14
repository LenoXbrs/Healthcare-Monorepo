package br.com.bali.code.healthcareapitriagem.Triagem.domain.repository;

import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long> {

    Optional<Diagnostico> findByTriagemId(Long triagemId);

    @Query("SELECT d FROM Diagnostico d " +
           "LEFT JOIN FETCH d.medicamentos " +
           "LEFT JOIN FETCH d.triagem t " +
           "LEFT JOIN FETCH t.sinaisVitais " +
           "WHERE d.id = :id")
    Optional<Diagnostico> findByIdWithDetails(@Param("id") Long id);
}
