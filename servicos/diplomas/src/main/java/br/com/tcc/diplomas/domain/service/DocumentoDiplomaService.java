package br.com.tcc.diplomas.domain.service;

import br.com.tcc.diplomas.api.dto.DocumentoDiplomaRequest;
import br.com.tcc.diplomas.api.mapper.DocumentoDiplomaMapper;
import br.com.tcc.diplomas.domain.model.DocumentoDiploma;
import br.com.tcc.diplomas.domain.repository.DiplomaRepository;
import br.com.tcc.diplomas.domain.repository.DocumentoDiplomaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentoDiplomaService {

  private final DocumentoDiplomaRepository repository;
  private final DiplomaRepository diplomaRepository;
  private final DocumentoDiplomaMapper mapper;

  public DocumentoDiplomaService(DocumentoDiplomaRepository repository, DiplomaRepository diplomaRepository,
      DocumentoDiplomaMapper mapper) {
    this.repository = repository;
    this.diplomaRepository = diplomaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<DocumentoDiploma> criar(DocumentoDiplomaRequest request) {
    if (request.diplomaId() == null) {
      return Optional.empty();
    }
    return diplomaRepository.findById(request.diplomaId())
        .map(diploma -> repository.save(mapper.toEntity(request, diploma)));
  }

  @Transactional
  public Optional<DocumentoDiploma> criar(Long diplomaId, DocumentoDiplomaRequest request) {
    return diplomaRepository.findById(diplomaId)
        .map(diploma -> repository.save(mapper.toEntity(request, diploma)));
  }

  public List<DocumentoDiploma> listar() {
    return repository.findAll();
  }

  public Optional<List<DocumentoDiploma>> listarPorDiplomaId(Long diplomaId) {
    if (!diplomaRepository.existsById(diplomaId)) {
      return Optional.empty();
    }
    return Optional.of(repository.findAllByDiplomaIdOrderByVersaoDesc(diplomaId));
  }

  public Optional<DocumentoDiploma> buscarPorId(Long id) {
    return repository.findById(id);
  }

  public Optional<DocumentoDiploma> buscarPorId(Long diplomaId, Long id) {
    return repository.findByIdAndDiplomaId(id, diplomaId);
  }

  @Transactional
  public Optional<DocumentoDiploma> atualizar(Long id, DocumentoDiplomaRequest request) {
    if (request.diplomaId() == null) {
      return Optional.empty();
    }
    var diplomaOpt = diplomaRepository.findById(request.diplomaId());
    if (diplomaOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(documento -> {
      mapper.updateEntityFromRequest(request, documento, diplomaOpt.get());
      return documento;
    });
  }

  @Transactional
  public Optional<DocumentoDiploma> atualizar(Long diplomaId, Long id, DocumentoDiplomaRequest request) {
    var diplomaOpt = diplomaRepository.findById(diplomaId);
    if (diplomaOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findByIdAndDiplomaId(id, diplomaId).map(documento -> {
      mapper.updateEntityFromRequest(request, documento, diplomaOpt.get());
      return documento;
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

  @Transactional
  public boolean remover(Long diplomaId, Long id) {
    if (!repository.existsByIdAndDiplomaId(id, diplomaId)) {
      return false;
    }
    repository.deleteById(id);
    return true;
  }
}
