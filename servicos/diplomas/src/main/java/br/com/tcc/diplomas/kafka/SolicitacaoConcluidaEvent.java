package br.com.tcc.diplomas.kafka;

import java.time.LocalDateTime;

public record SolicitacaoConcluidaEvent(
    Long documentoDiplomaId,
    String status,
    LocalDateTime dataConclusao
) {
}
