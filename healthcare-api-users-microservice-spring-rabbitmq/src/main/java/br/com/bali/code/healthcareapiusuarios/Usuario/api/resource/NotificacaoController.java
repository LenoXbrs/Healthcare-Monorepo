package br.com.bali.code.healthcareapiusuarios.Usuario.api.resource;

import br.com.bali.code.healthcareapiusuarios.Usuario.api.model.response.NotificacaoResponse;
import br.com.bali.code.healthcareapiusuarios.Usuario.domain.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public ResponseEntity<List<NotificacaoResponse>> listar(
            @RequestParam Long usuarioId,
            @RequestParam(required = false) Boolean lida) {
        return ResponseEntity.ok(notificacaoService.listarPorUsuario(usuarioId, lida));
    }

    @GetMapping("/contagem")
    public ResponseEntity<Map<String, Long>> contarNaoLidas(@RequestParam Long usuarioId) {
        long total = notificacaoService.contarNaoLidas(usuarioId);
        return ResponseEntity.ok(Map.of("naoLidas", total));
    }

    @PatchMapping("/{id}/lida")
    public ResponseEntity<NotificacaoResponse> marcarComoLida(
            @PathVariable Long id,
            @RequestParam Long usuarioId) {
        return ResponseEntity.ok(notificacaoService.marcarComoLida(id, usuarioId));
    }
}
