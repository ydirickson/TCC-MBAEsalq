package br.com.tcc.assinatura.api.dto;

import java.time.LocalDateTime;

public record DocumentoAssinavelResponse(
    Long id,
    Long documentoDiplomaId,
    String descricao,
    LocalDateTime dataCriacao
) {
}
