package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.domain.model.AlunoPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.CursoProgramaReferencia;
import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import br.com.tcc.posgraduacao.domain.model.SituacaoAcademica;
import br.com.tcc.posgraduacao.domain.model.TipoCursoPrograma;
import br.com.tcc.posgraduacao.domain.model.TipoVinculo;
import br.com.tcc.posgraduacao.domain.model.VinculoAcademico;
import br.com.tcc.posgraduacao.domain.repository.AlunoPosGraduacaoRepository;
import br.com.tcc.posgraduacao.domain.repository.PessoaRepository;
import br.com.tcc.posgraduacao.domain.repository.ProfessorPosGraduacaoRepository;
import br.com.tcc.posgraduacao.domain.repository.ProgramaPosRepository;
import br.com.tcc.posgraduacao.domain.repository.VinculoAcademicoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlunoPosGraduacaoService {

  private final AlunoPosGraduacaoRepository alunoRepository;
  private final PessoaRepository pessoaRepository;
  private final ProgramaPosRepository programaRepository;
  private final ProfessorPosGraduacaoRepository professorRepository;
  private final VinculoAcademicoRepository vinculoRepository;

  public AlunoPosGraduacaoService(
      AlunoPosGraduacaoRepository alunoRepository,
      PessoaRepository pessoaRepository,
      ProgramaPosRepository programaRepository,
      ProfessorPosGraduacaoRepository professorRepository,
      VinculoAcademicoRepository vinculoRepository) {
    this.alunoRepository = alunoRepository;
    this.pessoaRepository = pessoaRepository;
    this.programaRepository = programaRepository;
    this.professorRepository = professorRepository;
    this.vinculoRepository = vinculoRepository;
  }

  @Transactional
  public Optional<AlunoPosGraduacao> criar(
      Long pessoaId,
      Pessoa novaPessoa,
      Long programaId,
      Long orientadorId,
      LocalDate dataMatricula,
      LocalDate dataConclusao,
      SituacaoAcademica status) {
    Optional<Pessoa> pessoaOpt = obterOuCriarPessoa(pessoaId, novaPessoa);
    Optional<ProgramaPos> programaOpt = programaRepository.findById(programaId);
    Optional<ProfessorPosGraduacao> orientadorOpt = obterOrientador(orientadorId);
    if (pessoaOpt.isEmpty() || programaOpt.isEmpty() || (orientadorId != null && orientadorOpt.isEmpty())) {
      return Optional.empty();
    }

    Pessoa pessoa = pessoaOpt.get();
    ProgramaPos programa = programaOpt.get();
    
    AlunoPosGraduacao novo = new AlunoPosGraduacao(
        pessoa,
        programa,
        orientadorOpt.orElse(null),
        dataMatricula,
        status);
    novo.setDataConclusao(dataConclusao);
    AlunoPosGraduacao alunoSalvo = alunoRepository.save(novo);
    
    // Criar VínculoAcademico correspondente (sem triggers, criação explícita)
    TipoCursoPrograma tipoCurso = determinarTipoCursoPorPrograma(programa.getCodigo());
    CursoProgramaReferencia programaRef = new CursoProgramaReferencia(
        programa.getId(),
        programa.getCodigo(),
        programa.getNome(),
        tipoCurso
    );
    
    VinculoAcademico vinculo = new VinculoAcademico(
        pessoa,
        programaRef,
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

  public List<AlunoPosGraduacao> listar() {
    return alunoRepository.findAll();
  }

  public Optional<AlunoPosGraduacao> buscarPorId(Long id) {
    return alunoRepository.findById(id);
  }

  @Transactional
  public Optional<AlunoPosGraduacao> atualizar(
      Long id,
      Long pessoaId,
      Long programaId,
      Long orientadorId,
      LocalDate dataMatricula,
      LocalDate dataConclusao,
      SituacaoAcademica status) {
    Optional<Pessoa> pessoaOpt = pessoaRepository.findById(pessoaId);
    Optional<ProgramaPos> programaOpt = programaRepository.findById(programaId);
    Optional<ProfessorPosGraduacao> orientadorOpt = obterOrientador(orientadorId);
    if (pessoaOpt.isEmpty() || programaOpt.isEmpty() || (orientadorId != null && orientadorOpt.isEmpty())) {
      return Optional.empty();
    }

    return alunoRepository.findById(id).map(aluno -> {
      Pessoa pessoa = pessoaOpt.get();
      ProgramaPos programa = programaOpt.get();
      
      aluno.setPessoa(pessoa);
      aluno.setPrograma(programa);
      aluno.setOrientador(orientadorOpt.orElse(null));
      aluno.setDataMatricula(dataMatricula);
      aluno.setDataConclusao(dataConclusao);
      aluno.setStatus(status);
      
      // Atualizar VínculoAcademico correspondente
      TipoCursoPrograma tipoCurso = determinarTipoCursoPorPrograma(programa.getCodigo());
      CursoProgramaReferencia programaRef = new CursoProgramaReferencia(
          programa.getId(),
          programa.getCodigo(),
          programa.getNome(),
          tipoCurso
      );

      VinculoAcademico vinculo = vinculoRepository
        .findByPessoaAndCurso_IdAndTipoVinculo(pessoa, programa.getId(), TipoVinculo.ALUNO)
        .map(v -> {
            v.setCurso(programaRef);
            v.setDataIngresso(dataMatricula);
            v.setSituacao(status);
            return v;
        })
        .orElseGet(() -> new VinculoAcademico(
          pessoa,
          programaRef,
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

  private Optional<ProfessorPosGraduacao> obterOrientador(Long orientadorId) {
    if (orientadorId == null) {
      return Optional.empty();
    }
    return professorRepository.findById(orientadorId);
  }
  
  private TipoCursoPrograma determinarTipoCursoPorPrograma(String codigoPrograma) {
    // Lógica baseada em bd/c1/05_vinculo_academico_sync.sql
    return switch (codigoPrograma) {
      case "PPGCC" -> TipoCursoPrograma.MESTRADO;
      case "PPGAG" -> TipoCursoPrograma.DOUTORADO;
      default -> TipoCursoPrograma.ESPECIALIZACAO;
    };
  }
}
