package br.com.tcc.diplomas.api.dto;

import java.time.LocalDate;

public record DocumentoDiplomaResponse(
    Long id,
    Long diplomaId,
    Integer versao,
    LocalDate dataGeracao,
    String urlArquivo,
    String hashDocumento
) {
}
