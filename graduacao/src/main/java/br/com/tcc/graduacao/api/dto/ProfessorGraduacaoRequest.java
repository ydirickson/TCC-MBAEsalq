package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.NivelDocente;
import br.com.tcc.graduacao.domain.model.SituacaoFuncional;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProfessorGraduacaoRequest(
    @NotNull Long pessoaId,
    @NotNull Long cursoId,
    @NotNull LocalDate dataIngresso,
    @NotNull NivelDocente nivelDocente,
    @NotNull SituacaoFuncional status
) {
}
