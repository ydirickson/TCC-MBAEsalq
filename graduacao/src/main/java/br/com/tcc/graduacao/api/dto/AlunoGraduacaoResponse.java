package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import java.time.LocalDate;

public record AlunoGraduacaoResponse(
    Long id,
    Long pessoaId,
    Long cursoId,
    String cursoCodigo,
    String cursoNome,
    String turmaId,
    LocalDate dataMatricula,
    SituacaoAcademica status
) {
}
