package com.tcc.graduacao.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.tcc.graduacao.domain.model.AlunoGraduacao;
import com.tcc.graduacao.domain.model.CursoGraduacao;
import com.tcc.graduacao.domain.model.Pessoa;
import com.tcc.graduacao.domain.model.SituacaoAcademica;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AlunoGraduacaoMapperTest {

  private final AlunoGraduacaoMapper mapper = new AlunoGraduacaoMapper();

  @Test
  void deveMapearEntidadeParaResponse() {
    Pessoa pessoa = new Pessoa("Maria", LocalDate.of(1990, 5, 1), "Maria Silva");
    pessoa.setId(10L);
    CursoGraduacao curso = new CursoGraduacao("ENG001", "Engenharia", 3600);
    curso.setId(20L);

    AlunoGraduacao entity = new AlunoGraduacao(pessoa, curso, LocalDate.of(2020, 2, 15), SituacaoAcademica.ATIVO);
    entity.setId(30L);

    var response = mapper.toResponse(entity);

    assertThat(response.id()).isEqualTo(30L);
    assertThat(response.pessoaId()).isEqualTo(10L);
    assertThat(response.cursoId()).isEqualTo(20L);
    assertThat(response.cursoCodigo()).isEqualTo("ENG001");
    assertThat(response.cursoNome()).isEqualTo("Engenharia");
    assertThat(response.dataIngresso()).isEqualTo(LocalDate.of(2020, 2, 15));
    assertThat(response.status()).isEqualTo(SituacaoAcademica.ATIVO);
  }

  @Test
  void deveRetornarNuloQuandoEntidadeNula() {
    assertThat(mapper.toResponse(null)).isNull();
  }
}
