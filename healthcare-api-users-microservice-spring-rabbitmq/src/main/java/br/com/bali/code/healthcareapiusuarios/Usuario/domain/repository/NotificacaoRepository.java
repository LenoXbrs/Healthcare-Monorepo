package br.com.bali.code.healthcareapiusuarios.Usuario.domain.repository;

import br.com.bali.code.healthcareapiusuarios.Usuario.domain.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    List<Notificacao> findByUsuarioIdAndLidaOrderByCriadoEmDesc(Long usuarioId, Boolean lida);

    long countByUsuarioIdAndLidaFalse(Long usuarioId);
}
