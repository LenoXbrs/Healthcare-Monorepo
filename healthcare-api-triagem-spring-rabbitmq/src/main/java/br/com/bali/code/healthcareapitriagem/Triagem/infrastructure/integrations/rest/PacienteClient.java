package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest;

import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.exception.PacienteNaoEncontradoException;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.dto.PacienteCacheDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class PacienteClient {

    private final RestClient restClient;
    private final AuthTokenHolder authTokenHolder;

    public PacienteClient(
            @Value("${services.pacientes.url}") String baseUrl,
            AuthTokenHolder authTokenHolder) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.authTokenHolder = authTokenHolder;
    }

    @Cacheable(value = "paciente", key = "#id")
    public PacienteCacheDto buscarPorId(Long id) {
        log.debug("[PacienteClient] Buscando paciente id={}", id);
        try {
            return restClient.get()
                    .uri("/pacientes/{id}", id)
                    .headers(h -> {
                        String token = authTokenHolder.getBearerToken();
                        if (token != null) {
                            h.setBearerAuth(token);
                        }
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new PacienteNaoEncontradoException(id);
                    })
                    .body(PacienteCacheDto.class);
        } catch (PacienteNaoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            log.error("[PacienteClient] Erro ao buscar paciente id={}: {}", id, e.getMessage());
            throw new PacienteNaoEncontradoException(id);
        }
    }
}
