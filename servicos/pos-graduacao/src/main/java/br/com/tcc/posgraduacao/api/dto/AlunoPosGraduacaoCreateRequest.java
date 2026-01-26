package br.com.tcc.posgraduacao.api.dto;

import java.time.LocalDate;
import br.com.tcc.posgraduacao.domain.model.SituacaoAcademica;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record AlunoPosGraduacaoCreateRequest(
    Long pessoaId,
    PessoaRequest novaPessoa,
    @NotNull Long programaId,
    Long orientadorId,
    @NotNull LocalDate dataMatricula,
    LocalDate dataConclusao,
    @NotNull SituacaoAcademica status
) {

  @AssertTrue(message = "Informe pessoaId ou novaPessoa, mas nao ambos.")
  public boolean isPessoaInformada() {
    return (pessoaId != null) ^ (novaPessoa != null);
  }

  @AssertTrue(message = "Informe dataConclusao quando status for CONCLUIDO.")
  public boolean isDataConclusaoValida() {
    return status != SituacaoAcademica.CONCLUIDO || dataConclusao != null;
  }
}
