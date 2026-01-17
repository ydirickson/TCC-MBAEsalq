package br.com.tcc.diplomas.api.dto;

import br.com.tcc.diplomas.domain.model.SituacaoAcademica;
import br.com.tcc.diplomas.domain.model.TipoVinculo;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record VinculoAcademicoRequest(
    @NotNull(message = "Pessoa e obrigatoria")
    Long pessoaId,
    @NotNull(message = "Tipo de vinculo e obrigatorio")
    TipoVinculo tipoVinculo,
    @NotNull(message = "Data de ingresso e obrigatoria")
    LocalDate dataIngresso,
    LocalDate dataConclusao,
    @NotNull(message = "Situacao academica e obrigatoria")
    SituacaoAcademica situacao
) {
}
