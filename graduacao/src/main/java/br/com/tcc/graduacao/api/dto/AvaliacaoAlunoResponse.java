package br.com.tcc.graduacao.api.dto;

import java.math.BigDecimal;

public record AvaliacaoAlunoResponse(
    Long id,
    Long matriculaId,
    Long avaliacaoId,
    BigDecimal nota
) {
}
