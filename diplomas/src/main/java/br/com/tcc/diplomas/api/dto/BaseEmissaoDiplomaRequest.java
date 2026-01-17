package br.com.tcc.diplomas.api.dto;

import br.com.tcc.diplomas.domain.model.TipoCursoPrograma;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record BaseEmissaoDiplomaRequest(
    @NotNull(message = "Requerimento e obrigatorio")
    Long requerimentoId,
    @NotNull(message = "Pessoa e obrigatoria")
    Long pessoaId,
    @NotBlank(message = "Nome da pessoa e obrigatorio")
    String pessoaNome,
    String pessoaNomeSocial,
    @NotNull(message = "Data de nascimento e obrigatoria")
    LocalDate pessoaDataNascimento,
    @NotBlank(message = "Codigo do curso/programa e obrigatorio")
    String cursoCodigo,
    @NotBlank(message = "Nome do curso/programa e obrigatorio")
    String cursoNome,
    @NotNull(message = "Tipo do curso/programa e obrigatorio")
    TipoCursoPrograma cursoTipo,
    @NotNull(message = "Data de conclusao e obrigatoria")
    LocalDate dataConclusao,
    LocalDate dataColacaoGrau
) {
}
