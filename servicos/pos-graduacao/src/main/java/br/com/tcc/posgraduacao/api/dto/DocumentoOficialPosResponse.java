package br.com.tcc.posgraduacao.api.dto;

import java.time.LocalDate;

public record DocumentoOficialPosResponse(
    Long id,
    Long pessoaId,
    String tipoDocumento,
    LocalDate dataEmissao,
    Integer versao,
    String urlArquivo,
    String hashDocumento
) {
}
