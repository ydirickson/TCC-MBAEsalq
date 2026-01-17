package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.TipoDefesa;
import java.math.BigDecimal;

public record DefesaResponse(
    Long id,
    Long alunoId,
    TipoDefesa tipo,
    BigDecimal nota
) {
}
