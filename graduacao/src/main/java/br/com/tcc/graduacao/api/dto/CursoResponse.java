package br.com.tcc.graduacao.api.dto;

public record CursoResponse(
    Long id,
    String codigo,
    String nome,
    Integer cargaHoraria
) {
}
