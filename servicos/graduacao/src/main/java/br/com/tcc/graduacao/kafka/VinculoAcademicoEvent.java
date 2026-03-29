package br.com.tcc.graduacao.kafka;

import java.time.LocalDate;

public record VinculoAcademicoEvent(
    Long id,
    Long pessoaId,
    Long cursoId,
    String cursoCodigo,
    String cursoNome,
    String cursoTipo,
    String tipoVinculo,
    LocalDate dataIngresso,
    LocalDate dataConclusao,
    String situacao) {
}
