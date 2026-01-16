package br.com.tcc.graduacao.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AvaliacaoAlunoRequest(
    @NotNull Long avaliacaoId,
    @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal nota
) {
}
