package br.com.tcc.graduacao.api.dto;

public record AvaliacaoOfertaDisciplinaResponse(
    Long id,
    Long ofertaDisciplinaId,
    String nome,
    Short peso
) {
}
