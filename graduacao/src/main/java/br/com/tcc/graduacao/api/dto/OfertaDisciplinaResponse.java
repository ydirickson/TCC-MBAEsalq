package br.com.tcc.graduacao.api.dto;

public record OfertaDisciplinaResponse(
    Long id,
    Long disciplinaId,
    Long professorId,
    Integer ano,
    Integer semestre
) {
}
