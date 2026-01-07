package br.com.tcc.graduacao.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import br.com.tcc.graduacao.domain.repository.AlunoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class AlunoGraduacaoControllerIT {

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private CursoGraduacaoRepository cursoRepository;

  @Autowired
  private PessoaRepository pessoaRepository;

  @Autowired
  private AlunoGraduacaoRepository alunoRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    alunoRepository.deleteAll();
    pessoaRepository.deleteAll();
    cursoRepository.deleteAll();
  }

  @Test
  void deveCriarAlunoComNovaPessoaUsandoH2() throws Exception {
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("DIR", "Direito", 3200));

    mockMvc.perform(post("/alunos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "novaPessoa": {
                    "nome": "Fulano Teste",
                    "dataNascimento": "1990-12-01",
                    "nomeSocial": "Fulano"
                  },
                  "cursoId": %d,
                  "dataIngresso": "2024-02-01",
                  "status": "ATIVO"
                }
                """.formatted(curso.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.pessoaId").exists())
        .andExpect(jsonPath("$.cursoId").value(curso.getId()));

    assertThat(alunoRepository.count()).isEqualTo(1);
    var aluno = alunoRepository.findAll().get(0);
    assertThat(aluno.getPessoa().getNome()).isEqualTo("Fulano Teste");
    assertThat(aluno.getCurso().getId()).isEqualTo(curso.getId());
    assertThat(aluno.getStatus()).isEqualTo(SituacaoAcademica.ATIVO);
  }

  @Test
  void deveCriarAlunoUsandoPessoaExistente() throws Exception {
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ADM", "Administracao", 3000));
    Pessoa pessoa = pessoaRepository.save(new Pessoa("Pessoa Existente", LocalDate.of(1985, 5, 5), null));

    mockMvc.perform(post("/alunos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "pessoaId": %d,
                  "cursoId": %d,
                  "dataIngresso": "2024-02-01",
                  "status": "ATIVO"
                }
                """.formatted(pessoa.getId(), curso.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.pessoaId").value(pessoa.getId()))
        .andExpect(jsonPath("$.cursoId").value(curso.getId()));

    assertThat(alunoRepository.count()).isEqualTo(1);
    var aluno = alunoRepository.findAll().get(0);
    assertThat(aluno.getPessoa().getId()).isEqualTo(pessoa.getId());
  }
}
