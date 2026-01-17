package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.NivelDocente;
import br.com.tcc.graduacao.domain.model.SituacaoFuncional;
import java.time.LocalDate;

public record ProfessorGraduacaoResponse(
    Long id,
    Long pessoaId,
    Long cursoId,
    String cursoCodigo,
    String cursoNome,
    LocalDate dataIngresso,
    NivelDocente nivelDocente,
    SituacaoFuncional status
) {
}
