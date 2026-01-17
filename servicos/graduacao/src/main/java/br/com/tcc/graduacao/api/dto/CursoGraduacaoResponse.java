package br.com.tcc.graduacao.api.dto;

public record CursoGraduacaoResponse(
    Long id,
    String codigo,
    String nome,
    Integer cargaHoraria
) {
}
