package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.api.dto.OfertaDisciplinaRequest;
import br.com.tcc.graduacao.api.mapper.OfertaDisciplinaMapper;
import br.com.tcc.graduacao.domain.model.DisciplinaGraduacao;
import br.com.tcc.graduacao.domain.model.OfertaDisciplina;
import br.com.tcc.graduacao.domain.model.ProfessorGraduacao;
import br.com.tcc.graduacao.domain.repository.DisciplinaRepository;
import br.com.tcc.graduacao.domain.repository.OfertaDisciplinaRepository;
import br.com.tcc.graduacao.domain.repository.ProfessorGraduacaoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfertaDisciplinaService {

  private final OfertaDisciplinaRepository repository;
  private final DisciplinaRepository disciplinaRepository;
  private final ProfessorGraduacaoRepository professorRepository;
  private final OfertaDisciplinaMapper mapper;

  public OfertaDisciplinaService(
      OfertaDisciplinaRepository repository,
      DisciplinaRepository disciplinaRepository,
      ProfessorGraduacaoRepository professorRepository,
      OfertaDisciplinaMapper mapper) {
    this.repository = repository;
    this.disciplinaRepository = disciplinaRepository;
    this.professorRepository = professorRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<OfertaDisciplina> criar(OfertaDisciplinaRequest request) {
    Optional<DisciplinaGraduacao> disciplinaOpt = disciplinaRepository.findById(request.disciplinaId());
    Optional<ProfessorGraduacao> professorOpt = professorRepository.findById(request.professorId());
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
    Optional<DisciplinaGraduacao> disciplinaOpt = disciplinaRepository.findById(request.disciplinaId());
    Optional<ProfessorGraduacao> professorOpt = professorRepository.findById(request.professorId());
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
