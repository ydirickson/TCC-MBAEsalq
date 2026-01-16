package br.com.tcc.graduacao.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.tcc.graduacao.domain.model.AlunoGraduacao;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import br.com.tcc.graduacao.domain.model.TurmaGraduacao;
import br.com.tcc.graduacao.domain.repository.AlunoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.DisciplinaRepository;
import br.com.tcc.graduacao.domain.repository.MatriculaDisciplinaRepository;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import br.com.tcc.graduacao.domain.repository.TurmaGraduacaoRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AlunoGraduacaoServiceTest {

  @Autowired
  private AlunoGraduacaoService service;

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
  void criarDeveUtilizarPessoaExistenteQuandoInformada() {
    Pessoa pessoa = pessoaRepository.save(new Pessoa("Joao", LocalDate.of(1990, 1, 1), "Joao Silva"));
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ENG", "Engenharia", 3600));
    TurmaGraduacao turma = new TurmaGraduacao(curso, 2024, 1, TurmaGraduacao.StatusTurma.ATIVA);
    turma.setId("T-ENG-2024-1");
    turma = turmaRepository.save(turma);

    var alunoOpt = service.criar(pessoa.getId(), null, turma.getId(), LocalDate.of(2024, 3, 10), SituacaoAcademica.ATIVO);

    assertThat(alunoOpt).isPresent();
    AlunoGraduacao aluno = alunoOpt.get();
    assertThat(aluno.getPessoaId()).isEqualTo(pessoa.getId());
    assertThat(aluno.getTurma().getId()).isEqualTo(turma.getId());
  }

  @Test
  void criarDevePersistirNovaPessoaQuandoNaoInformadoId() {
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ADM", "Administracao", 3200));
    TurmaGraduacao turma = new TurmaGraduacao(curso, 2024, 1, TurmaGraduacao.StatusTurma.ATIVA);
    turma.setId("T-ADM-2024-1");
    turma = turmaRepository.save(turma);
    Pessoa novaPessoa = new Pessoa("Ana", LocalDate.of(1995, 6, 15), null);

    var alunoOpt = service.criar(null, novaPessoa, turma.getId(), LocalDate.of(2024, 3, 10), SituacaoAcademica.ATIVO);

    assertThat(alunoOpt).isPresent();
    assertThat(pessoaRepository.count()).isEqualTo(1);
    assertThat(alunoOpt.get().getPessoa().getNome()).isEqualTo("Ana");
  }

  @Test
  void criarDeveRetornarVazioQuandoTurmaNaoExiste() {
    Pessoa novaPessoa = new Pessoa("Ana", LocalDate.of(1995, 6, 15), null);

    var alunoOpt = service.criar(null, novaPessoa, "TURMA-INEXISTENTE", LocalDate.of(2024, 3, 10), SituacaoAcademica.ATIVO);

    assertThat(alunoOpt).isEmpty();
  }

  @Test
  void atualizarDeveAlterarDadosDoAluno() {
    Pessoa pessoa = pessoaRepository.save(new Pessoa("Joao", LocalDate.of(1990, 1, 1), null));
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ENG", "Engenharia", 3600));
    TurmaGraduacao turma = new TurmaGraduacao(curso, 2020, 1, TurmaGraduacao.StatusTurma.ATIVA);
    turma.setId("T-ENG-2020-1");
    turma = turmaRepository.save(turma);
    AlunoGraduacao aluno = alunoRepository.save(new AlunoGraduacao(pessoa, turma, LocalDate.of(2020, 1, 1), SituacaoAcademica.TRANCADO));

    Pessoa novaPessoa = pessoaRepository.save(new Pessoa("Maria", LocalDate.of(1992, 2, 2), null));
    CursoGraduacao novoCurso = cursoRepository.save(new CursoGraduacao("DIR", "Direito", 3000));
    TurmaGraduacao novaTurma = new TurmaGraduacao(novoCurso, 2021, 1, TurmaGraduacao.StatusTurma.ATIVA);
    novaTurma.setId("T-DIR-2021-1");
    novaTurma = turmaRepository.save(novaTurma);

    var alunoAtualizado = service.atualizar(aluno.getId(), novaPessoa.getId(), novaTurma.getId(), LocalDate.of(2021, 5, 5), SituacaoAcademica.ATIVO);

    assertThat(alunoAtualizado).isPresent();
    assertThat(alunoAtualizado.get().getPessoaId()).isEqualTo(novaPessoa.getId());
    assertThat(alunoAtualizado.get().getTurma().getId()).isEqualTo(novaTurma.getId());
    assertThat(alunoAtualizado.get().getStatus()).isEqualTo(SituacaoAcademica.ATIVO);
  }

  @Test
  void removerDeveExcluirAlunoExistente() {
    Pessoa pessoa = pessoaRepository.save(new Pessoa("Joao", LocalDate.of(1990, 1, 1), null));
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ENG", "Engenharia", 3600));
    TurmaGraduacao turma = new TurmaGraduacao(curso, 2020, 1, TurmaGraduacao.StatusTurma.ATIVA);
    turma.setId("T-ENG-2020-1");
    turma = turmaRepository.save(turma);
    AlunoGraduacao aluno = alunoRepository.save(new AlunoGraduacao(pessoa, turma, LocalDate.of(2020, 1, 1), SituacaoAcademica.ATIVO));

    boolean removido = service.remover(aluno.getId());

    assertThat(removido).isTrue();
    assertThat(alunoRepository.findById(aluno.getId())).isEmpty();
  }
}
