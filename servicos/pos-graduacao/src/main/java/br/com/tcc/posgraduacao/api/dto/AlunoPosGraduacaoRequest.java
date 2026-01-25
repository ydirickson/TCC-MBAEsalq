package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.SituacaoAcademica;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AlunoPosGraduacaoRequest(
    @NotNull Long pessoaId,
    @NotNull Long programaId,
    Long orientadorId,
    @NotNull LocalDate dataMatricula,
    LocalDate dataConclusao,
    @NotNull SituacaoAcademica status
) {

  @AssertTrue(message = "Informe dataConclusao quando status for CONCLUIDO.")
  public boolean isDataConclusaoValida() {
    return status != SituacaoAcademica.CONCLUIDO || dataConclusao != null;
  }
}
