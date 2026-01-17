package br.com.tcc.diplomas.api.dto;

import java.time.LocalDate;

public record RequerimentoDiplomaResponse(
    Long id,
    Long pessoaId,
    Long vinculoId,
    LocalDate dataSolicitacao,
    Long baseEmissaoId,
    Long statusEmissaoId,
    Long diplomaId
) {
}
