package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.api.dto.DisciplinaGraduacaoRequest;
import br.com.tcc.graduacao.api.mapper.DisciplinaGraduacaoMapper;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.DisciplinaGraduacao;
import br.com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.DisciplinaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisciplinaGraduacaoService {

  private final DisciplinaRepository repository;
  private final CursoGraduacaoRepository cursoRepository;
  private final DisciplinaGraduacaoMapper mapper;

  public DisciplinaGraduacaoService(DisciplinaRepository repository, CursoGraduacaoRepository cursoRepository,
      DisciplinaGraduacaoMapper mapper) {
    this.repository = repository;
    this.cursoRepository = cursoRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<DisciplinaGraduacao> criar(Long cursoId, DisciplinaGraduacaoRequest request) {
    Optional<CursoGraduacao> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isEmpty()) {
      return Optional.empty();
    }
    DisciplinaGraduacao disciplina = mapper.toEntity(cursoOpt.get(), request);
    return Optional.of(repository.save(disciplina));
  }

  public List<DisciplinaGraduacao> listar(Long cursoId) {
    return repository.findByCursoId(cursoId);
  }

  public Optional<DisciplinaGraduacao> buscarPorId(Long cursoId, Long id) {
    return repository.findByIdAndCursoId(id, cursoId);
  }

  @Transactional
  public Optional<DisciplinaGraduacao> atualizar(Long cursoId, Long id, DisciplinaGraduacaoRequest request) {
    Optional<CursoGraduacao> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findByIdAndCursoId(id, cursoId).map(disciplina -> {
      disciplina.setCurso(cursoOpt.get());
      mapper.updateEntityFromRequest(request, disciplina);
      return disciplina;
    });
  }

  @Transactional
  public boolean remover(Long cursoId, Long id) {
    if (repository.findByIdAndCursoId(id, cursoId).isEmpty()) {
      return false;
    }
    repository.deleteById(id);
    return true;
  }
}
