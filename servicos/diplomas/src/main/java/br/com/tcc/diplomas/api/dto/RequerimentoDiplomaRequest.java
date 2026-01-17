package br.com.tcc.diplomas.api.dto;

import br.com.tcc.diplomas.domain.model.TipoCursoPrograma;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RequerimentoDiplomaRequest(
    @NotNull(message = "Pessoa e obrigatoria")
    Long pessoaId,
    @NotNull(message = "Vinculo academico e obrigatorio")
    Long vinculoId,
    @NotBlank(message = "Codigo do curso/programa e obrigatorio")
    String cursoCodigo,
    @NotBlank(message = "Nome do curso/programa e obrigatorio")
    String cursoNome,
    @NotNull(message = "Tipo do curso/programa e obrigatorio")
    TipoCursoPrograma cursoTipo,
    @NotNull(message = "Data de conclusao e obrigatoria")
    LocalDate dataConclusao,
    LocalDate dataColacaoGrau,
    @NotNull(message = "Data de solicitacao e obrigatoria")
    LocalDate dataSolicitacao
) {
}
