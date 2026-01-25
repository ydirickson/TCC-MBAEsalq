package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AlunoGraduacaoRequest(
    @NotNull Long pessoaId,
    @NotNull String turmaId,
    @NotNull LocalDate dataMatricula,
    LocalDate dataConclusao,
    @NotNull SituacaoAcademica status
) {

  @AssertTrue(message = "Informe dataConclusao quando status for CONCLUIDO.")
  public boolean isDataConclusaoValida() {
    return status != SituacaoAcademica.CONCLUIDO || dataConclusao != null;
  }
}
