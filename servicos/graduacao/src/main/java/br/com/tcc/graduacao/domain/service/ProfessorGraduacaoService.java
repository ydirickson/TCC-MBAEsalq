package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.NivelDocente;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.ProfessorGraduacao;
import br.com.tcc.graduacao.domain.model.SituacaoFuncional;
import br.com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import br.com.tcc.graduacao.domain.repository.ProfessorGraduacaoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessorGraduacaoService {

  private final ProfessorGraduacaoRepository professorRepository;
  private final PessoaRepository pessoaRepository;
  private final CursoGraduacaoRepository cursoRepository;

  public ProfessorGraduacaoService(ProfessorGraduacaoRepository professorRepository,
      PessoaRepository pessoaRepository,
      CursoGraduacaoRepository cursoRepository) {
    this.professorRepository = professorRepository;
    this.pessoaRepository = pessoaRepository;
    this.cursoRepository = cursoRepository;
  }

  @Transactional
  public Optional<ProfessorGraduacao> criar(
      Long pessoaId,
      Pessoa novaPessoa,
      Long cursoId,
      LocalDate dataIngresso,
      NivelDocente nivelDocente,
      SituacaoFuncional status) {
    Optional<Pessoa> pessoaOpt = obterOuCriarPessoa(pessoaId, novaPessoa);
    Optional<CursoGraduacao> cursoOpt = cursoRepository.findById(cursoId);
    if (pessoaOpt.isEmpty() || cursoOpt.isEmpty()) {
      return Optional.empty();
    }

    ProfessorGraduacao novo = new ProfessorGraduacao(pessoaOpt.get(), cursoOpt.get(), dataIngresso, nivelDocente, status);
    return Optional.of(professorRepository.save(novo));
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

  public List<ProfessorGraduacao> listar() {
    return professorRepository.findAll();
  }

  public Optional<ProfessorGraduacao> buscarPorId(Long id) {
    return professorRepository.findById(id);
  }

  @Transactional
  public Optional<ProfessorGraduacao> atualizar(
      Long id,
      Long pessoaId,
      Long cursoId,
      LocalDate dataIngresso,
      NivelDocente nivelDocente,
      SituacaoFuncional status) {
    Optional<Pessoa> pessoaOpt = pessoaRepository.findById(pessoaId);
    Optional<CursoGraduacao> cursoOpt = cursoRepository.findById(cursoId);
    if (pessoaOpt.isEmpty() || cursoOpt.isEmpty()) {
      return Optional.empty();
    }

    return professorRepository.findById(id).map(professor -> {
      professor.setPessoa(pessoaOpt.get());
      professor.setCurso(cursoOpt.get());
      professor.setDataIngresso(dataIngresso);
      professor.setNivelDocente(nivelDocente);
      professor.setStatus(status);
      return professor;
    });
  }

  @Transactional
  public boolean remover(Long id) {
    if (!professorRepository.existsById(id)) {
      return false;
    }
    professorRepository.deleteById(id);
    return true;
  }
}
