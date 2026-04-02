package br.com.tcc.assinatura.kafka;

import java.time.LocalDateTime;

public record SolicitacaoConcluidaEvent(
    Long documentoDiplomaId,
    String status,
    LocalDateTime dataConclusao
) {
}
