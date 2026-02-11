package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.domain.model.AlunoGraduacao;
import br.com.tcc.graduacao.domain.model.CursoProgramaReferencia;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import br.com.tcc.graduacao.domain.model.TipoCursoPrograma;
import br.com.tcc.graduacao.domain.model.TipoVinculo;
import br.com.tcc.graduacao.domain.model.TurmaGraduacao;
import br.com.tcc.graduacao.domain.model.VinculoAcademico;
import br.com.tcc.graduacao.domain.repository.AlunoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import br.com.tcc.graduacao.domain.repository.TurmaGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.VinculoAcademicoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlunoGraduacaoService {

  private final AlunoGraduacaoRepository alunoRepository;
  private final PessoaRepository pessoaRepository;
  private final TurmaGraduacaoRepository turmaRepository;
  private final VinculoAcademicoRepository vinculoRepository;

  public AlunoGraduacaoService(AlunoGraduacaoRepository alunoRepository, PessoaRepository pessoaRepository,
      TurmaGraduacaoRepository turmaRepository, VinculoAcademicoRepository vinculoRepository) {
    this.alunoRepository = alunoRepository;
    this.pessoaRepository = pessoaRepository;
    this.turmaRepository = turmaRepository;
    this.vinculoRepository = vinculoRepository;
  }

  @Transactional
  public Optional<AlunoGraduacao> criar(
      Long pessoaId,
      Pessoa novaPessoa,
      String turmaId,
      LocalDate dataMatricula,
      LocalDate dataConclusao,
      SituacaoAcademica status) {
    Optional<Pessoa> pessoaOpt = obterOuCriarPessoa(pessoaId, novaPessoa);
    Optional<TurmaGraduacao> turmaOpt = turmaRepository.findById(turmaId);
    if (pessoaOpt.isEmpty() || turmaOpt.isEmpty()) {
      return Optional.empty();
    }

    Pessoa pessoa = pessoaOpt.get();
    TurmaGraduacao turma = turmaOpt.get();
    
    AlunoGraduacao novo = new AlunoGraduacao(pessoa, turma, dataMatricula, status);
    novo.setDataConclusao(dataConclusao);
    AlunoGraduacao alunoSalvo = alunoRepository.save(novo);
    
    // Criar VínculoAcademico correspondente (c2a2: sem triggers, criação explícita)
    CursoProgramaReferencia cursoRef = new CursoProgramaReferencia(
        turma.getCurso().getId(),
        turma.getCurso().getCodigo(),
        turma.getCurso().getNome(),
        TipoCursoPrograma.GRADUACAO
    );
    
    VinculoAcademico vinculo = new VinculoAcademico(
        pessoa,
        cursoRef,
        TipoVinculo.ALUNO,
        dataMatricula,
        status
    );
    vinculo.setDataConclusao(dataConclusao);
    vinculoRepository.save(vinculo);
    
    return Optional.of(alunoSalvo);
  }

  private Optional<Pessoa> obterOuCriarPessoa(Long pessoaId, Pessoa novaPessoa) {
    if (pessoaId != null) {
      return pessoaRepository.findById(pessoaId);
    }
    if (novaPessoa != null) {
      return Optional.of(pessoaRepository.save(novaPessoa));
    }
    return Optional.empty();
  }

  public List<AlunoGraduacao> listar() {
    return alunoRepository.findAll();
  }

  public Optional<AlunoGraduacao> buscarPorId(Long id) {
    return alunoRepository.findById(id);
  }

  @Transactional
  public Optional<AlunoGraduacao> atualizar(
      Long id,
      Long pessoaId,
      String turmaId,
      LocalDate dataMatricula,
      LocalDate dataConclusao,
      SituacaoAcademica status) {
    Optional<Pessoa> pessoaOpt = pessoaRepository.findById(pessoaId);
    Optional<TurmaGraduacao> turmaOpt = turmaRepository.findById(turmaId);
    if (pessoaOpt.isEmpty() || turmaOpt.isEmpty()) {
      return Optional.empty();
    }

    return alunoRepository.findById(id).map(aluno -> {
      Pessoa pessoa = pessoaOpt.get();
      TurmaGraduacao turma = turmaOpt.get();
      
      aluno.setPessoa(pessoa);
      aluno.setTurma(turma);
      aluno.setDataMatricula(dataMatricula);
      aluno.setDataConclusao(dataConclusao);
      aluno.setStatus(status);
      
      // Atualizar VínculoAcademico correspondente
      CursoProgramaReferencia cursoRef = new CursoProgramaReferencia(
          turma.getCurso().getId(),
          turma.getCurso().getCodigo(),
          turma.getCurso().getNome(),
          TipoCursoPrograma.GRADUACAO
      );

      VinculoAcademico vinculo = vinculoRepository
        .findByPessoaAndCurso_IdAndTipoVinculo(pessoa, turma.getCurso().getId(), TipoVinculo.ALUNO)
        .map(v -> {
            v.setCurso(cursoRef); // Atualiza dados do curso caso tenham mudado
            v.setDataIngresso(dataMatricula);
            v.setSituacao(status);
            return v;
        })
        .orElseGet(() -> new VinculoAcademico(
            pessoa,
            cursoRef,
            TipoVinculo.ALUNO,
            dataMatricula,
            status
        ));

      vinculo.setDataConclusao(dataConclusao);
      vinculoRepository.save(vinculo);
      
      return aluno;
    });
  }

  @Transactional
  public boolean remover(Long id) {
    if (!alunoRepository.existsById(id)) {
      return false;
    }
    alunoRepository.deleteById(id);
    return true;
  }
}
