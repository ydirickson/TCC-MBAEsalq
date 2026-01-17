package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.TurmaGraduacao.StatusTurma;
import jakarta.validation.constraints.NotNull;

public record TurmaGraduacaoRequest(
    @NotNull Integer ano,
    @NotNull Integer semestre,
    @NotNull StatusTurma status
) {
}
