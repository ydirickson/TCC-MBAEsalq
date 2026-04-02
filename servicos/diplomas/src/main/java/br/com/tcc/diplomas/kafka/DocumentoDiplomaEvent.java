package br.com.tcc.diplomas.kafka;

import java.time.LocalDate;

public record DocumentoDiplomaEvent(
    Long id,
    Long diplomaId,
    Integer versao,
    LocalDate dataGeracao,
    String urlArquivo,
    String hashDocumento
) {
}
