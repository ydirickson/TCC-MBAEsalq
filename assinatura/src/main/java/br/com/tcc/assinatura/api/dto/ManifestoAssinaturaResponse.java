package br.com.tcc.assinatura.api.dto;

import java.time.LocalDateTime;

public record ManifestoAssinaturaResponse(
    Long id,
    Long solicitacaoId,
    String auditoria,
    LocalDateTime carimboTempo,
    String hashFinal
) {
}
