package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.api.dto.OfertaDisciplinaRequest;
import br.com.tcc.posgraduacao.api.mapper.OfertaDisciplinaMapper;
import br.com.tcc.posgraduacao.domain.model.DisciplinaPos;
import br.com.tcc.posgraduacao.domain.model.OfertaDisciplina;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;
import br.com.tcc.posgraduacao.domain.repository.DisciplinaPosRepository;
import br.com.tcc.posgraduacao.domain.repository.OfertaDisciplinaRepository;
import br.com.tcc.posgraduacao.domain.repository.ProfessorPosGraduacaoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfertaDisciplinaService {

  private final OfertaDisciplinaRepository repository;
  private final DisciplinaPosRepository disciplinaRepository;
  private final ProfessorPosGraduacaoRepository professorRepository;
  private final OfertaDisciplinaMapper mapper;

  public OfertaDisciplinaService(
      OfertaDisciplinaRepository repository,
      DisciplinaPosRepository disciplinaRepository,
      ProfessorPosGraduacaoRepository professorRepository,
      OfertaDisciplinaMapper mapper) {
    this.repository = repository;
    this.disciplinaRepository = disciplinaRepository;
    this.professorRepository = professorRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<OfertaDisciplina> criar(OfertaDisciplinaRequest request) {
    Optional<DisciplinaPos> disciplinaOpt = disciplinaRepository.findById(request.disciplinaId());
    Optional<ProfessorPosGraduacao> professorOpt = professorRepository.findById(request.professorId());
    if (disciplinaOpt.isEmpty() || professorOpt.isEmpty()) {
      return Optional.empty();
    }
    OfertaDisciplina oferta = mapper.toEntity(disciplinaOpt.get(), professorOpt.get(), request);
    return Optional.of(repository.save(oferta));
  }

  public List<OfertaDisciplina> listar(Long disciplinaId, Long professorId) {
    if (disciplinaId != null) {
      return repository.findByDisciplinaId(disciplinaId);
    }
    if (professorId != null) {
      return repository.findByProfessorId(professorId);
    }
    return repository.findAll();
  }

  public Optional<OfertaDisciplina> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<OfertaDisciplina> atualizar(Long id, OfertaDisciplinaRequest request) {
    Optional<DisciplinaPos> disciplinaOpt = disciplinaRepository.findById(request.disciplinaId());
    Optional<ProfessorPosGraduacao> professorOpt = professorRepository.findById(request.professorId());
    if (disciplinaOpt.isEmpty() || professorOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(oferta -> {
      mapper.updateEntityFromRequest(request, disciplinaOpt.get(), professorOpt.get(), oferta);
      return oferta;
    });
  }

  @Transactional
  public boolean remover(Long id) {
    if (!repository.existsById(id)) {
      return false;
    }
    repository.deleteById(id);
    return true;
  }
}
