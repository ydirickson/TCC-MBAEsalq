package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.TipoDefesa;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DefesaRequest(
    @NotNull Long alunoId,
    @NotNull TipoDefesa tipo,
    @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal nota
) {
}
