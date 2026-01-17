package br.com.tcc.posgraduacao.api.dto;

import java.math.BigDecimal;

public record DefesaMembroResponse(
    Long id,
    Long professorId,
    BigDecimal nota,
    boolean presidente
) {
}
