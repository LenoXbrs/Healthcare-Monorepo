package br.com.bali.code.healthcareapitriagem.Triagem.domain.repository;

import br.com.bali.code.healthcareapitriagem.Triagem.domain.model.SinaisVitais;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SinaisVitaisRepository extends JpaRepository<SinaisVitais, Long> {
}
