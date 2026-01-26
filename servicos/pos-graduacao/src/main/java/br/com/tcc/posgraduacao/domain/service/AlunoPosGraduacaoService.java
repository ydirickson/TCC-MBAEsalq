package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.domain.model.AlunoPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import br.com.tcc.posgraduacao.domain.model.SituacaoAcademica;
import br.com.tcc.posgraduacao.domain.repository.AlunoPosGraduacaoRepository;
import br.com.tcc.posgraduacao.domain.repository.PessoaRepository;
import br.com.tcc.posgraduacao.domain.repository.ProfessorPosGraduacaoRepository;
import br.com.tcc.posgraduacao.domain.repository.ProgramaPosRepository;
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

  public AlunoPosGraduacaoService(
      AlunoPosGraduacaoRepository alunoRepository,
      PessoaRepository pessoaRepository,
      ProgramaPosRepository programaRepository,
      ProfessorPosGraduacaoRepository professorRepository) {
    this.alunoRepository = alunoRepository;
    this.pessoaRepository = pessoaRepository;
    this.programaRepository = programaRepository;
    this.professorRepository = professorRepository;
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

    AlunoPosGraduacao novo = new AlunoPosGraduacao(
        pessoaOpt.get(),
        programaOpt.get(),
        orientadorOpt.orElse(null),
        dataMatricula,
        status);
    novo.setDataConclusao(dataConclusao);
    return Optional.of(alunoRepository.save(novo));
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
      aluno.setPessoa(pessoaOpt.get());
      aluno.setPrograma(programaOpt.get());
      aluno.setOrientador(orientadorOpt.orElse(null));
      aluno.setDataMatricula(dataMatricula);
      aluno.setDataConclusao(dataConclusao);
      aluno.setStatus(status);
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
}
