package br.com.tcc.graduacao.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.tcc.graduacao.domain.model.AlunoGraduacao;
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

  @BeforeEach
  void limparDados() {
    alunoRepository.deleteAll();
    pessoaRepository.deleteAll();
    cursoRepository.deleteAll();
  }

  @Test
  void criarDeveUtilizarPessoaExistenteQuandoInformada() {
    Pessoa pessoa = pessoaRepository.save(new Pessoa("Joao", LocalDate.of(1990, 1, 1), "Joao Silva"));
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ENG", "Engenharia", 3600));

    var alunoOpt = service.criar(pessoa.getId(), null, curso.getId(), LocalDate.of(2024, 3, 10), SituacaoAcademica.ATIVO);

    assertThat(alunoOpt).isPresent();
    AlunoGraduacao aluno = alunoOpt.get();
    assertThat(aluno.getPessoaId()).isEqualTo(pessoa.getId());
    assertThat(aluno.getCurso().getId()).isEqualTo(curso.getId());
  }

  @Test
  void criarDevePersistirNovaPessoaQuandoNaoInformadoId() {
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ADM", "Administracao", 3200));
    Pessoa novaPessoa = new Pessoa("Ana", LocalDate.of(1995, 6, 15), null);

    var alunoOpt = service.criar(null, novaPessoa, curso.getId(), LocalDate.of(2024, 3, 10), SituacaoAcademica.ATIVO);

    assertThat(alunoOpt).isPresent();
    assertThat(pessoaRepository.count()).isEqualTo(1);
    assertThat(alunoOpt.get().getPessoa().getNome()).isEqualTo("Ana");
  }

  @Test
  void criarDeveRetornarVazioQuandoCursoNaoExiste() {
    Pessoa novaPessoa = new Pessoa("Ana", LocalDate.of(1995, 6, 15), null);

    var alunoOpt = service.criar(null, novaPessoa, 999L, LocalDate.of(2024, 3, 10), SituacaoAcademica.ATIVO);

    assertThat(alunoOpt).isEmpty();
  }

  @Test
  void atualizarDeveAlterarDadosDoAluno() {
    Pessoa pessoa = pessoaRepository.save(new Pessoa("Joao", LocalDate.of(1990, 1, 1), null));
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ENG", "Engenharia", 3600));
    AlunoGraduacao aluno = alunoRepository.save(new AlunoGraduacao(pessoa, curso, LocalDate.of(2020, 1, 1), SituacaoAcademica.TRANCADO));

    Pessoa novaPessoa = pessoaRepository.save(new Pessoa("Maria", LocalDate.of(1992, 2, 2), null));
    CursoGraduacao novoCurso = cursoRepository.save(new CursoGraduacao("DIR", "Direito", 3000));

    var alunoAtualizado = service.atualizar(aluno.getId(), novaPessoa.getId(), novoCurso.getId(), LocalDate.of(2021, 5, 5), SituacaoAcademica.ATIVO);

    assertThat(alunoAtualizado).isPresent();
    assertThat(alunoAtualizado.get().getPessoaId()).isEqualTo(novaPessoa.getId());
    assertThat(alunoAtualizado.get().getCurso().getId()).isEqualTo(novoCurso.getId());
    assertThat(alunoAtualizado.get().getStatus()).isEqualTo(SituacaoAcademica.ATIVO);
  }

  @Test
  void removerDeveExcluirAlunoExistente() {
    Pessoa pessoa = pessoaRepository.save(new Pessoa("Joao", LocalDate.of(1990, 1, 1), null));
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ENG", "Engenharia", 3600));
    AlunoGraduacao aluno = alunoRepository.save(new AlunoGraduacao(pessoa, curso, LocalDate.of(2020, 1, 1), SituacaoAcademica.ATIVO));

    boolean removido = service.remover(aluno.getId());

    assertThat(removido).isTrue();
    assertThat(alunoRepository.findById(aluno.getId())).isEmpty();
  }
}
