package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.MatriculaDisciplina.StatusMatricula;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MatriculaDisciplinaResponse(
    Long id,
    Long alunoId,
    Long ofertaDisciplinaId,
    LocalDate dataMatricula,
    StatusMatricula status,
    BigDecimal nota
) {
}
