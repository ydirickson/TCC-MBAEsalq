package br.com.tcc.graduacao.api.dto;

import br.com.tcc.graduacao.domain.model.NivelDocente;
import br.com.tcc.graduacao.domain.model.SituacaoFuncional;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProfessorGraduacaoCreateRequest(
    Long pessoaId,
    PessoaRequest novaPessoa,
    @NotNull Long cursoId,
    @NotNull LocalDate dataIngresso,
    @NotNull NivelDocente nivelDocente,
    @NotNull SituacaoFuncional status
) {

  @AssertTrue(message = "Informe pessoaId ou novaPessoa, mas nao ambos.")
  public boolean isPessoaInformada() {
    return (pessoaId != null) ^ (novaPessoa != null);
  }
}
