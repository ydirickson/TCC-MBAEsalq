package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.CursoProgramaReferencia;
import br.com.tcc.graduacao.domain.model.NivelDocente;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.ProfessorGraduacao;
import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import br.com.tcc.graduacao.domain.model.SituacaoFuncional;
import br.com.tcc.graduacao.domain.model.TipoCursoPrograma;
import br.com.tcc.graduacao.domain.model.TipoVinculo;
import br.com.tcc.graduacao.domain.model.VinculoAcademico;
import br.com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import br.com.tcc.graduacao.domain.repository.ProfessorGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.VinculoAcademicoRepository;
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
  private final VinculoAcademicoRepository vinculoRepository;

  public ProfessorGraduacaoService(ProfessorGraduacaoRepository professorRepository,
      PessoaRepository pessoaRepository,
      CursoGraduacaoRepository cursoRepository,
      VinculoAcademicoRepository vinculoRepository) {
    this.professorRepository = professorRepository;
    this.pessoaRepository = pessoaRepository;
    this.cursoRepository = cursoRepository;
    this.vinculoRepository = vinculoRepository;
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

    Pessoa pessoa = pessoaOpt.get();
    CursoGraduacao curso = cursoOpt.get();
    
    ProfessorGraduacao novo = new ProfessorGraduacao(pessoa, curso, dataIngresso, nivelDocente, status);
    ProfessorGraduacao professorSalvo = professorRepository.save(novo);
    
    // Criar VínculoAcademico correspondente (sem triggers, criação explícita)
    CursoProgramaReferencia cursoRef = new CursoProgramaReferencia(
        curso.getId(),
        curso.getCodigo(),
        curso.getNome(),
        TipoCursoPrograma.GRADUACAO
    );
    
    VinculoAcademico vinculo = new VinculoAcademico(
        pessoa,
        cursoRef,
        TipoVinculo.PROFESSOR,
        dataIngresso,
        SituacaoAcademica.valueOf(status.name())
    );
    vinculoRepository.save(vinculo);
    
    return Optional.of(professorSalvo);
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
      Pessoa pessoa = pessoaOpt.get();
      CursoGraduacao curso = cursoOpt.get();
      
      professor.setPessoa(pessoa);
      professor.setCurso(curso);
      professor.setDataIngresso(dataIngresso);
      professor.setNivelDocente(nivelDocente);
      professor.setStatus(status);
      
      // Atualizar VínculoAcademico correspondente
      CursoProgramaReferencia cursoRef = new CursoProgramaReferencia(
          curso.getId(),
          curso.getCodigo(),
          curso.getNome(),
          TipoCursoPrograma.GRADUACAO
      );
      
      VinculoAcademico vinculo = new VinculoAcademico(
          pessoa,
          cursoRef,
          TipoVinculo.PROFESSOR,
          dataIngresso,
          SituacaoAcademica.valueOf(status.name())
      );
      vinculoRepository.save(vinculo);
      
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
