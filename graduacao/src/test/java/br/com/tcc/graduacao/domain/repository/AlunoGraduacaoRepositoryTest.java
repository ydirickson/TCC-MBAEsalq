package br.com.tcc.graduacao.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import br.com.tcc.graduacao.domain.model.AlunoGraduacao;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import br.com.tcc.graduacao.domain.model.TurmaGraduacao;

@SpringBootTest
@ActiveProfiles("test")
class AlunoGraduacaoRepositoryTest {

  @Autowired
  private AlunoGraduacaoRepository alunoRepository;

  @Autowired
  private PessoaRepository pessoaRepository;

  @Autowired
  private CursoGraduacaoRepository cursoRepository;

  @Autowired
  private TurmaGraduacaoRepository turmaRepository;

  @Autowired
  private DisciplinaRepository disciplinaRepository;

  @Autowired
  private MatriculaDisciplinaRepository matriculaDisciplinaRepository;

  @BeforeEach
  void limparDados() {
    matriculaDisciplinaRepository.deleteAll();
    disciplinaRepository.deleteAll();
    alunoRepository.deleteAll();
    pessoaRepository.deleteAll();
    turmaRepository.deleteAll();
    cursoRepository.deleteAll();
  }

  @Test
  void deveSalvarERecuperarAluno() {
    Pessoa pessoa = pessoaRepository.save(new Pessoa("Aluno Teste", LocalDate.of(1990, 1, 1), null));
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ENG", "Engenharia", 3600));
    TurmaGraduacao turma = new TurmaGraduacao(curso, 2024, 1, TurmaGraduacao.StatusTurma.ATIVA);
    turma.setId("T-ENG-2024-1");
    turma = turmaRepository.save(turma);

    AlunoGraduacao salvo = alunoRepository.save(new AlunoGraduacao(pessoa, turma, LocalDate.of(2024, 1, 1), SituacaoAcademica.ATIVO));

    assertThat(salvo.getId()).isNotNull();

    var encontrado = alunoRepository.findById(salvo.getId());
    assertThat(encontrado).isPresent();
    assertThat(encontrado.get().getPessoa().getNome()).isEqualTo("Aluno Teste");
    assertThat(encontrado.get().getCurso().getCodigo()).isEqualTo("ENG");
    assertThat(encontrado.get().getTurma().getId()).isEqualTo("T-ENG-2024-1");
    assertThat(encontrado.get().getStatus()).isEqualTo(SituacaoAcademica.ATIVO);
  }
}
