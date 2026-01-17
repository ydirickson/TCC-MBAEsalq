package br.com.tcc.graduacao.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoOfertaDisciplinaRequest(
    @NotBlank String nome,
    @NotNull Short peso
) {
}
