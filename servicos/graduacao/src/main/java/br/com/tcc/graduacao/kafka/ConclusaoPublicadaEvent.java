package br.com.tcc.graduacao.kafka;

import java.time.LocalDate;

public record ConclusaoPublicadaEvent(Long pessoaId, Long vinculoAcademicoId, String cursoTipo, LocalDate dataConclusao) {
}
