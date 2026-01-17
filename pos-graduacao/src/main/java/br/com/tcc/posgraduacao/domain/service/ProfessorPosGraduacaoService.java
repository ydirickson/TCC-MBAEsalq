package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.domain.model.NivelDocente;
import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import br.com.tcc.posgraduacao.domain.model.SituacaoFuncional;
import br.com.tcc.posgraduacao.domain.repository.PessoaRepository;
import br.com.tcc.posgraduacao.domain.repository.ProfessorPosGraduacaoRepository;
import br.com.tcc.posgraduacao.domain.repository.ProgramaPosRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfessorPosGraduacaoService {

  private final ProfessorPosGraduacaoRepository professorRepository;
  private final PessoaRepository pessoaRepository;
  private final ProgramaPosRepository programaRepository;

  public ProfessorPosGraduacaoService(ProfessorPosGraduacaoRepository professorRepository,
      PessoaRepository pessoaRepository,
      ProgramaPosRepository programaRepository) {
    this.professorRepository = professorRepository;
    this.pessoaRepository = pessoaRepository;
    this.programaRepository = programaRepository;
  }

  @Transactional
  public Optional<ProfessorPosGraduacao> criar(
      Long pessoaId,
      Pessoa novaPessoa,
      Long programaId,
      LocalDate dataIngresso,
      NivelDocente nivelDocente,
      SituacaoFuncional status) {
    Optional<Pessoa> pessoaOpt = obterOuCriarPessoa(pessoaId, novaPessoa);
    Optional<ProgramaPos> programaOpt = programaRepository.findById(programaId);
    if (pessoaOpt.isEmpty() || programaOpt.isEmpty()) {
      return Optional.empty();
    }

    ProfessorPosGraduacao novo = new ProfessorPosGraduacao(
        pessoaOpt.get(),
        programaOpt.get(),
        dataIngresso,
        nivelDocente,
        status);
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

  public List<ProfessorPosGraduacao> listar() {
    return professorRepository.findAll();
  }

  public Optional<ProfessorPosGraduacao> buscarPorId(Long id) {
    return professorRepository.findById(id);
  }

  @Transactional
  public Optional<ProfessorPosGraduacao> atualizar(
      Long id,
      Long pessoaId,
      Long programaId,
      LocalDate dataIngresso,
      NivelDocente nivelDocente,
      SituacaoFuncional status) {
    Optional<Pessoa> pessoaOpt = pessoaRepository.findById(pessoaId);
    Optional<ProgramaPos> programaOpt = programaRepository.findById(programaId);
    if (pessoaOpt.isEmpty() || programaOpt.isEmpty()) {
      return Optional.empty();
    }

    return professorRepository.findById(id).map(professor -> {
      professor.setPessoa(pessoaOpt.get());
      professor.setPrograma(programaOpt.get());
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
