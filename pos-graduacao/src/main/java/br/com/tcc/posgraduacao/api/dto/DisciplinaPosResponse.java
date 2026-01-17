package br.com.tcc.posgraduacao.api.dto;

public record DisciplinaPosResponse(
    Long id,
    Long programaId,
    String codigo,
    String nome,
    Integer cargaHoraria
) {
}
