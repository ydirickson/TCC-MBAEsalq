package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.api.dto.DisciplinaPosRequest;
import br.com.tcc.posgraduacao.api.mapper.DisciplinaPosMapper;
import br.com.tcc.posgraduacao.domain.model.DisciplinaPos;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import br.com.tcc.posgraduacao.domain.repository.DisciplinaPosRepository;
import br.com.tcc.posgraduacao.domain.repository.ProgramaPosRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisciplinaPosService {

  private final DisciplinaPosRepository repository;
  private final ProgramaPosRepository programaRepository;
  private final DisciplinaPosMapper mapper;

  public DisciplinaPosService(
      DisciplinaPosRepository repository,
      ProgramaPosRepository programaRepository,
      DisciplinaPosMapper mapper) {
    this.repository = repository;
    this.programaRepository = programaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<DisciplinaPos> criar(Long programaId, DisciplinaPosRequest request) {
    Optional<ProgramaPos> programaOpt = programaRepository.findById(programaId);
    if (programaOpt.isEmpty()) {
      return Optional.empty();
    }
    DisciplinaPos disciplina = mapper.toEntity(programaOpt.get(), request);
    return Optional.of(repository.save(disciplina));
  }

  public List<DisciplinaPos> listar(Long programaId) {
    return repository.findByProgramaId(programaId);
  }

  public Optional<DisciplinaPos> buscarPorId(Long programaId, Long id) {
    return repository.findByIdAndProgramaId(id, programaId);
  }

  @Transactional
  public Optional<DisciplinaPos> atualizar(Long programaId, Long id, DisciplinaPosRequest request) {
    Optional<ProgramaPos> programaOpt = programaRepository.findById(programaId);
    if (programaOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findByIdAndProgramaId(id, programaId).map(disciplina -> {
      disciplina.setPrograma(programaOpt.get());
      mapper.updateEntityFromRequest(request, disciplina);
      return disciplina;
    });
  }

  @Transactional
  public boolean remover(Long programaId, Long id) {
    if (repository.findByIdAndProgramaId(id, programaId).isEmpty()) {
      return false;
    }
    repository.deleteById(id);
    return true;
  }
}
