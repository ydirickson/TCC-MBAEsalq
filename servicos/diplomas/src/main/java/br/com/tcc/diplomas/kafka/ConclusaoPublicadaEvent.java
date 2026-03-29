package br.com.tcc.diplomas.kafka;

import java.time.LocalDate;

public record ConclusaoPublicadaEvent(Long pessoaId, Long vinculoAcademicoId, String cursoTipo, LocalDate dataConclusao) {
}
