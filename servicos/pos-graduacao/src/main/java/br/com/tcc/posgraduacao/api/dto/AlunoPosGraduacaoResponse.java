package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.SituacaoAcademica;
import java.time.LocalDate;

public record AlunoPosGraduacaoResponse(
    Long id,
    Long pessoaId,
    Long programaId,
    String programaCodigo,
    String programaNome,
    Long orientadorId,
    LocalDate dataMatricula,
    SituacaoAcademica status
) {
}
