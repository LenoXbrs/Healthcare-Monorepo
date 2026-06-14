package br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest;

import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.exception.UsuarioNaoEncontradoException;
import br.com.bali.code.healthcareapitriagem.Triagem.infrastructure.integrations.rest.dto.UsuarioCacheDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
public class UsuarioClient {

    private static final String ROLE_MEDICO = "MEDICO";

    private final RestClient restClient;
    private final AuthTokenHolder authTokenHolder;

    public UsuarioClient(
            @Value("${services.usuarios.url}") String baseUrl,
            AuthTokenHolder authTokenHolder) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.authTokenHolder = authTokenHolder;
    }

    @Cacheable(value = "usuario", key = "#id")
    public UsuarioCacheDto buscarPorId(Long id) {
        log.debug("[UsuarioClient] Buscando usuário id={}", id);
        try {
            UsuarioCacheDto usuario = restClient.get()
                    .uri("/usuarios/{id}", id)
                    .headers(h -> {
                        String token = authTokenHolder.getBearerToken();
                        if (token != null) {
                            h.setBearerAuth(token);
                        }
                    })
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new UsuarioNaoEncontradoException(id);
                    })
                    .body(UsuarioCacheDto.class);
            if (usuario == null) {
                throw new UsuarioNaoEncontradoException(id);
            }
            return usuario;
        } catch (UsuarioNaoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            log.error("[UsuarioClient] Erro ao buscar usuário id={}: {}", id, e.getMessage());
            throw new UsuarioNaoEncontradoException(id);
        }
    }

    @Cacheable(value = "medicos")
    public List<UsuarioCacheDto> listarMedicos() {
        return restClient.get()
                .uri(uri -> uri.path("/usuarios").queryParam("role", ROLE_MEDICO).build())
                .headers(h -> {
                    String token = authTokenHolder.getBearerToken();
                    if (token != null) {
                        h.setBearerAuth(token);
                    }
                })
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void validarMedico(Long medicoId) {
        UsuarioCacheDto medico = buscarPorId(medicoId);
        if (!ROLE_MEDICO.equals(medico.role())) {
            throw new IllegalArgumentException("Usuário informado não é médico");
        }
    }

    public void validarEnfermeiro(Long enfermeiroId) {
        UsuarioCacheDto enfermeiro = buscarPorId(enfermeiroId);
        if (!"ENFERMEIRO".equals(enfermeiro.role())) {
            throw new IllegalArgumentException("Usuário informado não é enfermeiro");
        }
    }
}
