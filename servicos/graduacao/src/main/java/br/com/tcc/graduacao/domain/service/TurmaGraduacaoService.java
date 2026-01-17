package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.api.dto.TurmaGraduacaoRequest;
import br.com.tcc.graduacao.api.mapper.TurmaGraduacaoMapper;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.TurmaGraduacao;
import br.com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.TurmaGraduacaoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TurmaGraduacaoService {

  private final TurmaGraduacaoRepository repository;
  private final CursoGraduacaoRepository cursoRepository;
  private final TurmaGraduacaoMapper mapper;

  public TurmaGraduacaoService(
      TurmaGraduacaoRepository repository,
      CursoGraduacaoRepository cursoRepository,
      TurmaGraduacaoMapper mapper) {
    this.repository = repository;
    this.cursoRepository = cursoRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<TurmaGraduacao> criar(Long cursoId, TurmaGraduacaoRequest request) {
    Optional<CursoGraduacao> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isEmpty()) {
      return Optional.empty();
    }
    String codigoCurso = cursoOpt.get().getCodigo();
    if (codigoCurso == null || codigoCurso.isBlank()) {
      return Optional.empty();
    }
    String codigoTurma = String.format("%d%02d%s", request.ano(), request.semestre(), codigoCurso);
    TurmaGraduacao turma = mapper.toEntity(cursoOpt.get(), request);
    turma.setId(codigoTurma);
    return Optional.of(repository.save(turma));
  }

  public List<TurmaGraduacao> listar(Long cursoId) {
    return repository.findByCursoId(cursoId);
  }

  public Optional<TurmaGraduacao> buscarPorId(Long cursoId, String id) {
    return repository.findByIdAndCursoId(id, cursoId);
  }

  @Transactional
  public Optional<TurmaGraduacao> atualizar(Long cursoId, String id, TurmaGraduacaoRequest request) {
    Optional<CursoGraduacao> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findByIdAndCursoId(id, cursoId).map(turma -> {
      turma.setCurso(cursoOpt.get());
      mapper.updateEntityFromRequest(request, turma);
      return turma;
    });
  }

  @Transactional
  public boolean remover(Long cursoId, String id) {
    if (repository.findByIdAndCursoId(id, cursoId).isEmpty()) {
      return false;
    }
    repository.deleteById(id);
    return true;
  }
}
