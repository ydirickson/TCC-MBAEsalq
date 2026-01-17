package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.NivelDocente;
import br.com.tcc.posgraduacao.domain.model.SituacaoFuncional;
import java.time.LocalDate;

public record ProfessorPosGraduacaoResponse(
    Long id,
    Long pessoaId,
    Long programaId,
    String programaCodigo,
    String programaNome,
    LocalDate dataIngresso,
    NivelDocente nivelDocente,
    SituacaoFuncional status
) {
}
