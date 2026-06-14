package br.com.bali.code.healthcareapitriagem.Triagem.api.resource;

import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.MedicoSalaResponse;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.service.MedicoSalaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/triagem/medicos")
@Tag(name = "Médico Sala", description = "Endpoints para associar médicos a consultórios de atendimento")
public class MedicoSalaController {

    private final MedicoSalaService medicoSalaService;

    public MedicoSalaController(MedicoSalaService medicoSalaService) {
        this.medicoSalaService = medicoSalaService;
    }

    @PutMapping("/{medicoId}/sala")
    @Operation(summary = "Associar ou atualizar o consultório de um médico")
    public ResponseEntity<MedicoSalaResponse> salvarOuAtualizar(
            @PathVariable Long medicoId,
            @RequestParam String sala) {
        return ResponseEntity.ok(medicoSalaService.salvarOuAtualizar(medicoId, sala));
    }

    @GetMapping("/{medicoId}/sala")
    @Operation(summary = "Buscar o consultório atual de um médico")
    public ResponseEntity<MedicoSalaResponse> buscarPorMedicoId(@PathVariable Long medicoId) {
        return ResponseEntity.ok(medicoSalaService.buscarPorMedicoId(medicoId));
    }

    @GetMapping("/salas")
    @Operation(summary = "Listar todas as associações de médicos a consultórios")
    public ResponseEntity<List<MedicoSalaResponse>> listarTodos() {
        return ResponseEntity.ok(medicoSalaService.listarTodos());
    }

    @DeleteMapping("/{medicoId}/sala")
    @Operation(summary = "Registrar saída (check-out) do consultório do médico")
    public ResponseEntity<Void> remover(@PathVariable Long medicoId) {
        medicoSalaService.remover(medicoId);
        return ResponseEntity.noContent().build();
    }
}
