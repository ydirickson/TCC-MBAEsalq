package br.com.tcc.assinatura.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ManifestoAssinaturaRequest(
    @NotNull(message = "Solicitacao e obrigatoria")
    Long solicitacaoId,
    @NotBlank(message = "Auditoria e obrigatoria")
    String auditoria,
    @NotNull(message = "Carimbo de tempo e obrigatorio")
    LocalDateTime carimboTempo,
    @NotBlank(message = "Hash final e obrigatorio")
    String hashFinal
) {
}
