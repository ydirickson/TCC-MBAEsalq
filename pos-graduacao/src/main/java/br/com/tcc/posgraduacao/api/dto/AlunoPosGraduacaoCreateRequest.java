package br.com.tcc.posgraduacao.api.dto;

import br.com.tcc.posgraduacao.domain.model.SituacaoAcademica;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AlunoPosGraduacaoCreateRequest(
    Long pessoaId,
    PessoaRequest novaPessoa,
    @NotNull Long programaId,
    Long orientadorId,
    @NotNull LocalDate dataMatricula,
    @NotNull SituacaoAcademica status
) {

  @AssertTrue(message = "Informe pessoaId ou novaPessoa, mas nao ambos.")
  public boolean isPessoaInformada() {
    return (pessoaId != null) ^ (novaPessoa != null);
  }
}
