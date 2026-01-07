package br.com.tcc.graduacao.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.tcc.graduacao.domain.model.AlunoGraduacao;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AlunoGraduacaoRepositoryTest {

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
  void deveSalvarERecuperarAluno() {
    Pessoa pessoa = pessoaRepository.save(new Pessoa("Aluno Teste", LocalDate.of(1990, 1, 1), null));
    CursoGraduacao curso = cursoRepository.save(new CursoGraduacao("ENG", "Engenharia", 3600));

    AlunoGraduacao salvo = alunoRepository.save(new AlunoGraduacao(pessoa, curso, LocalDate.of(2024, 1, 1), SituacaoAcademica.ATIVO));

    assertThat(salvo.getId()).isNotNull();

    var encontrado = alunoRepository.findById(salvo.getId());
    assertThat(encontrado).isPresent();
    assertThat(encontrado.get().getPessoa().getNome()).isEqualTo("Aluno Teste");
    assertThat(encontrado.get().getCurso().getCodigo()).isEqualTo("ENG");
    assertThat(encontrado.get().getStatus()).isEqualTo(SituacaoAcademica.ATIVO);
  }
}
