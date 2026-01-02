package com.tcc.graduacao.domain.service;

import com.tcc.graduacao.domain.model.AlunoGraduacao;
import com.tcc.graduacao.domain.model.CursoGraduacao;
import com.tcc.graduacao.domain.model.Pessoa;
import com.tcc.graduacao.domain.model.SituacaoAcademica;
import com.tcc.graduacao.domain.repository.AlunoGraduacaoRepository;
import com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import com.tcc.graduacao.domain.repository.PessoaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlunoGraduacaoService {

  private final AlunoGraduacaoRepository alunoRepository;
  private final CursoGraduacaoRepository cursoRepository;
  private final PessoaRepository pessoaRepository;

  public AlunoGraduacaoService(AlunoGraduacaoRepository alunoRepository, CursoGraduacaoRepository cursoRepository,
      PessoaRepository pessoaRepository) {
    this.alunoRepository = alunoRepository;
    this.cursoRepository = cursoRepository;
    this.pessoaRepository = pessoaRepository;
  }

  @Transactional
  public Optional<AlunoGraduacao> criar(Long pessoaId, Pessoa novaPessoa, Long cursoId, LocalDate dataIngresso, SituacaoAcademica status) {
    Optional<CursoGraduacao> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isEmpty()) {
      return Optional.empty();
    }

    Optional<Pessoa> pessoaOpt = obterOuCriarPessoa(pessoaId, novaPessoa);
    if (pessoaOpt.isEmpty()) {
      return Optional.empty();
    }

    AlunoGraduacao novo = new AlunoGraduacao(pessoaOpt.get(), cursoOpt.get(), dataIngresso, status);
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
  public Optional<AlunoGraduacao> atualizar(Long id, Long pessoaId, Long cursoId, LocalDate dataIngresso, SituacaoAcademica status) {
    Optional<CursoGraduacao> cursoOpt = cursoRepository.findById(cursoId);
    Optional<Pessoa> pessoaOpt = pessoaRepository.findById(pessoaId);
    if (cursoOpt.isEmpty() || pessoaOpt.isEmpty()) {
      return Optional.empty();
    }

    return alunoRepository.findById(id).map(aluno -> {
      aluno.setPessoa(pessoaOpt.get());
      aluno.setCurso(cursoOpt.get());
      aluno.setDataIngresso(dataIngresso);
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
