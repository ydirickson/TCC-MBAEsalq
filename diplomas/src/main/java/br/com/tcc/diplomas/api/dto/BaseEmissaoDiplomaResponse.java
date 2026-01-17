package br.com.tcc.diplomas.api.dto;

import br.com.tcc.diplomas.domain.model.TipoCursoPrograma;
import java.time.LocalDate;

public record BaseEmissaoDiplomaResponse(
    Long id,
    Long requerimentoId,
    Long pessoaId,
    String pessoaNome,
    String pessoaNomeSocial,
    LocalDate pessoaDataNascimento,
    String cursoCodigo,
    String cursoNome,
    TipoCursoPrograma cursoTipo,
    LocalDate dataConclusao,
    LocalDate dataColacaoGrau
) {
}
