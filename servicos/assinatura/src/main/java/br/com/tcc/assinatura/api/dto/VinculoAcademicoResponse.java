package br.com.tcc.assinatura.api.dto;

import br.com.tcc.assinatura.domain.model.SituacaoAcademica;
import br.com.tcc.assinatura.domain.model.TipoVinculo;
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
