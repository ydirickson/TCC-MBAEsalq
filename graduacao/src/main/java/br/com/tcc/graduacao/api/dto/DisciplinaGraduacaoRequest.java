package br.com.tcc.graduacao.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record DisciplinaGraduacaoRequest(
    @NotBlank(message = "Código da disciplina é obrigatório")
    @Pattern(regexp = "^[A-Z]{3}\\d{4}$", message = "Código deve conter 3 letras maiúsculas seguidas de 4 dígitos")
    String codigo,
    @NotBlank(message = "Nome da disciplina é obrigatório")
    String nome,
    @NotNull(message = "Carga horária é obrigatória")
    @Positive(message = "Carga horária deve ser um número positivo")
    Integer cargaHoraria
) {
}
