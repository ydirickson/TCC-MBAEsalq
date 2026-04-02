package br.com.tcc.assinatura.kafka;

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
