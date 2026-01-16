package br.com.tcc.graduacao.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CursoGraduacaoRequest(
    @NotBlank(message = "Código do curso é obrigatório")
    @Pattern(regexp = "^[A-Z]{3,5}$", message = "Código do curso deve conter entre 3 e 5 letras maiúsculas")
    String codigo,
    @NotBlank(message = "Nome do curso é obrigatório")
    String nome,
    @NotNull(message = "Carga horária é obrigatória")
    @Positive(message = "Carga horária deve ser um número positivo")
    Integer cargaHoraria
) {
}
