package br.com.tcc.posgraduacao.api.dto;

public record ProgramaPosResponse(
    Long id,
    String codigo,
    String nome,
    Integer cargaHoraria
) {
}
