package br.com.tcc.diplomas.api.dto;

import br.com.tcc.diplomas.domain.model.SituacaoAcademica;
import br.com.tcc.diplomas.domain.model.TipoVinculo;
import java.time.LocalDate;

public record VinculoAcademicoResponse(
    Long id,
    Long pessoaId,
    TipoVinculo tipoVinculo,
    LocalDate dataIngresso,
    LocalDate dataConclusao,
    SituacaoAcademica situacao
) {
}
