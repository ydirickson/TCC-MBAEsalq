package br.com.tcc.graduacao.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record DocumentoOficialGraduacaoRequest(
    Long pessoaId,
    @NotBlank(message = "Tipo do documento e obrigatorio")
    String tipoDocumento,
    @NotNull(message = "Data de emissao e obrigatoria")
    LocalDate dataEmissao,
    @NotNull(message = "Versao e obrigatoria")
    @Positive(message = "Versao deve ser um numero positivo")
    Integer versao,
    String urlArquivo,
    String hashDocumento
) {
}
