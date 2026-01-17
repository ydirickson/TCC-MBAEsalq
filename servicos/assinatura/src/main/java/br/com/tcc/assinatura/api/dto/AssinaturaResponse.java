package br.com.tcc.assinatura.api.dto;

import br.com.tcc.assinatura.domain.model.StatusAssinatura;
import java.time.LocalDateTime;

public record AssinaturaResponse(
    Long id,
    Long solicitacaoId,
    Long usuarioAssinanteId,
    StatusAssinatura status,
    LocalDateTime dataAssinatura,
    String motivoRecusa
) {
}
