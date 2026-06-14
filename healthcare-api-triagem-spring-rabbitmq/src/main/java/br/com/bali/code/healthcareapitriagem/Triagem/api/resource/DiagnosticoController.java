package br.com.bali.code.healthcareapitriagem.Triagem.api.resource;

import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.SalvarDiagnosticoRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.request.AssinarDiagnosticoRequest;
import br.com.bali.code.healthcareapitriagem.Triagem.api.model.response.DiagnosticoResponse;
import br.com.bali.code.healthcareapitriagem.Triagem.domain.service.DiagnosticoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diagnosticos")
@Tag(name = "Diagnóstico", description = "Módulo de atendimento médico e prescrição")
public class DiagnosticoController {

    private final DiagnosticoService diagnosticoService;

    public DiagnosticoController(DiagnosticoService diagnosticoService) {
        this.diagnosticoService = diagnosticoService;
    }

    @PostMapping
    @Operation(summary = "Médico aceita a triagem e inicia o atendimento")
    public ResponseEntity<DiagnosticoResponse> iniciar(
            @RequestParam Long triagemId,
            @RequestParam Long medicoId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(diagnosticoService.iniciar(triagemId, medicoId));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Salvar rascunho de diagnóstico clínico e medicamentos")
    public ResponseEntity<DiagnosticoResponse> salvar(
            @PathVariable Long id,
            @RequestBody @Valid SalvarDiagnosticoRequest request,
            @RequestParam Long medicoId
    ) {
        return ResponseEntity.ok(diagnosticoService.salvar(id, request, medicoId));
    }

    @PostMapping("/{id}/assinar")
    @Operation(summary = "Finalizar e assinar eletronicamente o diagnóstico")
    public ResponseEntity<DiagnosticoResponse> assinar(
            @PathVariable Long id,
            @RequestParam Long medicoId,
            @RequestBody @Valid AssinarDiagnosticoRequest request
    ) {
        return ResponseEntity.ok(diagnosticoService.assinar(id, medicoId, request.assinaturaBase64()));
    }

    @GetMapping("/{id}/pdf")
    @Operation(summary = "Emitir receita e laudo médico em PDF")
    public ResponseEntity<byte[]> gerarPdf(@PathVariable Long id) {
        byte[] pdfBytes = diagnosticoService.gerarPdf(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "diagnostico-" + id + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
