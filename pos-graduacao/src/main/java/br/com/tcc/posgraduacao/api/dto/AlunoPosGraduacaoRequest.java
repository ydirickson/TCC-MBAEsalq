package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.SituacaoAcademica;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AlunoPosGraduacaoRequest(
    @NotNull Long pessoaId,
    @NotNull Long programaId,
    Long orientadorId,
    @NotNull LocalDate dataMatricula,
    @NotNull SituacaoAcademica status
) {
}
