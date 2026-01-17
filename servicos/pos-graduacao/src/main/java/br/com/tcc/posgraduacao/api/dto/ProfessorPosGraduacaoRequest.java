package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.NivelDocente;
import br.com.tcc.posgraduacao.domain.model.SituacaoFuncional;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProfessorPosGraduacaoRequest(
    @NotNull Long pessoaId,
    @NotNull Long programaId,
    @NotNull LocalDate dataIngresso,
    @NotNull NivelDocente nivelDocente,
    @NotNull SituacaoFuncional status
) {
}
