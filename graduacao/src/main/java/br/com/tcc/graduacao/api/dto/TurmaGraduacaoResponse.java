package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.TurmaGraduacao.StatusTurma;

public record TurmaGraduacaoResponse(
    String id,
    Long cursoId,
    Integer ano,
    Integer semestre,
    StatusTurma status
) {
}
