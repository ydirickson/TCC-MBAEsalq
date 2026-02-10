package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.domain.model.CursoProgramaReferencia;
import br.com.tcc.posgraduacao.domain.model.NivelDocente;
import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import br.com.tcc.posgraduacao.domain.model.SituacaoAcademica;
import br.com.tcc.posgraduacao.domain.model.SituacaoFuncional;
import br.com.tcc.posgraduacao.domain.model.TipoCursoPrograma;
import br.com.tcc.posgraduacao.domain.model.TipoVinculo;
import br.com.tcc.posgraduacao.domain.model.VinculoAcademico;
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
public class ProfessorPosGraduacaoService {

  private final ProfessorPosGraduacaoRepository professorRepository;
  private final PessoaRepository pessoaRepository;
  private final ProgramaPosRepository programaRepository;
  private final VinculoAcademicoRepository vinculoRepository;

  public ProfessorPosGraduacaoService(ProfessorPosGraduacaoRepository professorRepository,
      PessoaRepository pessoaRepository,
      ProgramaPosRepository programaRepository,
      VinculoAcademicoRepository vinculoRepository) {
    this.professorRepository = professorRepository;
    this.pessoaRepository = pessoaRepository;
    this.programaRepository = programaRepository;
    this.vinculoRepository = vinculoRepository;
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

    Pessoa pessoa = pessoaOpt.get();
    ProgramaPos programa = programaOpt.get();
    
    ProfessorPosGraduacao novo = new ProfessorPosGraduacao(
        pessoa,
        programa,
        dataIngresso,
        nivelDocente,
        status);
    ProfessorPosGraduacao professorSalvo = professorRepository.save(novo);
    
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
      Pessoa pessoa = pessoaOpt.get();
      ProgramaPos programa = programaOpt.get();
      
      professor.setPessoa(pessoa);
      professor.setPrograma(programa);
      professor.setDataIngresso(dataIngresso);
      professor.setNivelDocente(nivelDocente);
      professor.setStatus(status);
      
      // Atualizar VínculoAcademico correspondente
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
  
  private TipoCursoPrograma determinarTipoCursoPorPrograma(String codigoPrograma) {
    // Lógica baseada em bd/c1/05_vinculo_academico_sync.sql
    return switch (codigoPrograma) {
      case "PPGCC" -> TipoCursoPrograma.MESTRADO;
      case "PPGAG" -> TipoCursoPrograma.DOUTORADO;
      default -> TipoCursoPrograma.ESPECIALIZACAO;
    };
  }
}
