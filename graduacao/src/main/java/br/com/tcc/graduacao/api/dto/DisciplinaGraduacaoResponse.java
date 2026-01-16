package br.com.tcc.graduacao.api.dto;

public record DisciplinaGraduacaoResponse(
    Long id,
    Long cursoId,
    String codigo,
    String nome,
    Integer cargaHoraria
) {
}
