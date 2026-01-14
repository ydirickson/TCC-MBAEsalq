package br.com.tcc.graduacao.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import br.com.tcc.graduacao.config.JsonSeedLoader;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.DisciplinaRepository;

@SpringBootTest
@ActiveProfiles("test")
class DisciplinaGraduacaoControllerIT {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private CursoGraduacaoRepository cursoRepository;

  @Autowired
  private DisciplinaRepository disciplinaRepository;

  @Autowired
  private JsonSeedLoader seedLoader;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() throws IOException {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    disciplinaRepository.deleteAll();
    cursoRepository.deleteAll();
    seedLoader.seed();
  }

  @Test
  void deveTerAoMenosVinteDisciplinasPorCurso() {
    List<CursoGraduacao> cursos = cursoRepository.findAll();
    assertThat(cursos).isNotEmpty();
    for (CursoGraduacao curso : cursos) {
      assertThat(disciplinaRepository.findByCursoId(curso.getId()).size())
          .isGreaterThanOrEqualTo(20);
    }
  }

  @Test
  void deveListarDisciplinasParaCurso() throws Exception {
    CursoGraduacao curso = cursoRepository.findByCodigo("ADM").orElseThrow();

    mockMvc.perform(get("/cursos/{cursoId}/disciplinas", curso.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(20))
        .andExpect(jsonPath("$[0].cursoId").value(curso.getId()));
  }
}
