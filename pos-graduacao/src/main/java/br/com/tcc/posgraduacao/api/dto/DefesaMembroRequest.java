package br.com.tcc.posgraduacao.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DefesaMembroRequest(
    @NotNull Long professorId,
    @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal nota,
    @NotNull Boolean presidente
) {
}
