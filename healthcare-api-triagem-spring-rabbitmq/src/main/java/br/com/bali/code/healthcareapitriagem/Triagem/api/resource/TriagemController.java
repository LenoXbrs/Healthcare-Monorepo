package br.com.bali.code.healthcareapitriagem.Triagem.api.resource;

import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.AbrirTriagemRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.ClassificarTriagemRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.FinalizarTriagemRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.*;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.service.TriagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/triagem")
@Tag(name = "Triagem", description = "Fluxo: paciente.criado → triagem automática → enfermeiro abre → médico classifica")
public class TriagemController {

    private final TriagemService triagemService;

    public TriagemController(TriagemService triagemService) {
        this.triagemService = triagemService;
    }

    /**
     * Enfermeiro registra queixa na triagem já criada pelo evento paciente.criado.
     */
    @PostMapping
    @Operation(summary = "Abrir triagem (enfermeiro assume paciente da fila)")
    public ResponseEntity<TriagemAbertaResponse> abrir(@RequestBody @Valid AbrirTriagemRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(triagemService.abrir(request));
    }

    @PostMapping("/check-in")
    @Operation(summary = "Realizar check-in de paciente já cadastrado para nova triagem")
    public ResponseEntity<TriagemListItemResponse> checkIn(@RequestParam Long pacienteId) {
        return ResponseEntity.ok(triagemService.checkIn(pacienteId));
    }

    @PostMapping("/{id}/classificar")
    @Operation(summary = "Classificar triagem com prioridade e sinais vitais")
    public ResponseEntity<TriagemClassificadaResponse> classificar(
            @PathVariable Long id,
            @RequestBody @Valid ClassificarTriagemRequest request) {
        return ResponseEntity.ok(triagemService.classificar(id, request));
    }

    @GetMapping
    @Operation(summary = "Listar todas as triagens")
    public ResponseEntity<List<TriagemListItemResponse>> listar() {
        return ResponseEntity.ok(triagemService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar triagem por ID")
    public ResponseEntity<TriagemDetalheResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(triagemService.buscarPorId(id));
    }

    @PutMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar triagem")
    public ResponseEntity<TriagemFinalizadaResponse> finalizar(
            @PathVariable Long id,
            @RequestBody FinalizarTriagemRequest request) {
        return ResponseEntity.ok(triagemService.finalizar(id, request));
    }

    @PostMapping("/{id}/chamar")
    @Operation(summary = "Chamar paciente no painel público")
    public ResponseEntity<TriagemDetalheResponse> chamar(
            @PathVariable Long id,
            @RequestParam String sala) {
        return ResponseEntity.ok(triagemService.chamar(id, sala));
    }
}
