package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.domain.model.AlunoGraduacao;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import br.com.tcc.graduacao.domain.model.TurmaGraduacao;
import br.com.tcc.graduacao.domain.repository.AlunoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import br.com.tcc.graduacao.domain.repository.TurmaGraduacaoRepository;
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

  public AlunoGraduacaoService(AlunoGraduacaoRepository alunoRepository, PessoaRepository pessoaRepository,
      TurmaGraduacaoRepository turmaRepository) {
    this.alunoRepository = alunoRepository;
    this.pessoaRepository = pessoaRepository;
    this.turmaRepository = turmaRepository;
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

    AlunoGraduacao novo = new AlunoGraduacao(pessoaOpt.get(), turmaOpt.get(), dataMatricula, status);
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
      aluno.setPessoa(pessoaOpt.get());
      aluno.setTurma(turmaOpt.get());
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
}
