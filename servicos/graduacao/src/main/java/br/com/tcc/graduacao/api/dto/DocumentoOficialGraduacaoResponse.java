package br.com.tcc.graduacao.api.dto;

import java.time.LocalDate;

public record DocumentoOficialGraduacaoResponse(
    Long id,
    Long pessoaId,
    String tipoDocumento,
    LocalDate dataEmissao,
    Integer versao,
    String urlArquivo,
    String hashDocumento
) {
}
