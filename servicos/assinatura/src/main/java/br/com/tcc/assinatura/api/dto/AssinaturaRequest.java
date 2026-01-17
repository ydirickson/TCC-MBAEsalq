package br.com.tcc.assinatura.api.dto;

import br.com.tcc.assinatura.domain.model.StatusAssinatura;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AssinaturaRequest(
    @NotNull(message = "Solicitacao e obrigatoria")
    Long solicitacaoId,
    @NotNull(message = "Usuario assinante e obrigatorio")
    Long usuarioAssinanteId,
    @NotNull(message = "Status e obrigatorio")
    StatusAssinatura status,
    @NotNull(message = "Data de assinatura e obrigatoria")
    LocalDateTime dataAssinatura,
    String motivoRecusa
) {
}
