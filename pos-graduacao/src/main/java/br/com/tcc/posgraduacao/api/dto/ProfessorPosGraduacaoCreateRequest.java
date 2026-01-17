package br.com.tcc.posgraduacao.api.dto;

import java.time.LocalDate;
import br.com.tcc.posgraduacao.domain.model.NivelDocente;
import br.com.tcc.posgraduacao.domain.model.SituacaoFuncional;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ProfessorPosGraduacaoCreateRequest(
    Long pessoaId,
    PessoaRequest novaPessoa,
    @NotNull Long programaId,
    @NotNull LocalDate dataIngresso,
    @NotNull NivelDocente nivelDocente,
    @NotNull SituacaoFuncional status
) {

  @AssertTrue(message = "Informe pessoaId ou novaPessoa, mas nao ambos.")
  public boolean isPessoaInformada() {
    return (pessoaId != null) ^ (novaPessoa != null);
  }
}
