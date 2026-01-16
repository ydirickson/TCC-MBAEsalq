package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.MatriculaDisciplina.StatusMatricula;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MatriculaDisciplinaRequest(
    @NotNull Long alunoId,
    @NotNull Long ofertaDisciplinaId,
    @NotNull LocalDate dataMatricula,
    @NotNull StatusMatricula status,
    @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal nota
) {
}
