package br.com.tcc.graduacao.api.dto;

import java.time.LocalDate;
import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record AlunoGraduacaoCreateRequest(
    Long pessoaId,
    PessoaRequest novaPessoa,
    @NotNull String turmaId,
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
