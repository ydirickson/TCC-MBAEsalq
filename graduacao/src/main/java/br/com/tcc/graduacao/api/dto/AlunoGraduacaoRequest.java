package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AlunoGraduacaoRequest(
    @NotNull Long pessoaId,
    @NotNull Long cursoId,
    @NotNull LocalDate dataIngresso,
    @NotNull SituacaoAcademica status
) {
}
