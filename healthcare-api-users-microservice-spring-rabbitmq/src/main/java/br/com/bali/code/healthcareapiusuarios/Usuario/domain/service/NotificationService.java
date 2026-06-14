package br.com.bali.code.healthcareapiusuarios.Usuario.domain.service;

import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto.TriagemClassificadaPayload;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto.TriagemCriadaPayload;
import br.com.bali.code.healthcareapiusuarios.Usuario.infrastructure.integrations.rabbitmq.dto.TriagemFinalizadaPayload;

public interface NotificationService {

    void notificarEnfermeirosTriagemCriada(TriagemCriadaPayload payload);

    void notificarMedicoTriagemClassificada(TriagemClassificadaPayload payload);

    void notificarEquipeTriagemFinalizada(TriagemFinalizadaPayload payload);
}
