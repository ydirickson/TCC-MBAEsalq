package br.com.tcc.diplomas.api.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DocumentoDiplomaRequest(
    Long diplomaId,
    @NotNull(message = "Versao e obrigatoria")
    Integer versao,
    @NotNull(message = "Data de geracao e obrigatoria")
    LocalDate dataGeracao,
    String urlArquivo,
    String hashDocumento
) {
}
