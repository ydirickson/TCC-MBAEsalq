package com.tcc.graduacao.api.dto;

import com.tcc.graduacao.domain.model.SituacaoAcademica;
import java.time.LocalDate;

public record AlunoGraduacaoResponse(
    Long id,
    Long pessoaId,
    Long cursoId,
    String cursoCodigo,
    String cursoNome,
    LocalDate dataIngresso,
    SituacaoAcademica status
) {
}
