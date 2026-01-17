package br.com.tcc.posgraduacao.api.dto;

public record OfertaDisciplinaResponse(
    Long id,
    Long disciplinaId,
    Long professorId,
    Integer ano,
    Integer semestre
) {
}
