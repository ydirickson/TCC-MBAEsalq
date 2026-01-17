package br.com.tcc.graduacao.api.dto;

import jakarta.validation.constraints.NotNull;

public record OfertaDisciplinaRequest(
    @NotNull Long disciplinaId,
    @NotNull Long professorId,
    @NotNull Integer ano,
    @NotNull Integer semestre
) {
}
