package br.com.tcc.diplomas.api.dto;

import java.time.LocalDate;

public record DiplomaResponse(
    Long id,
    Long requerimentoId,
    Long baseEmissaoId,
    String numeroRegistro,
    LocalDate dataEmissao
) {
}
