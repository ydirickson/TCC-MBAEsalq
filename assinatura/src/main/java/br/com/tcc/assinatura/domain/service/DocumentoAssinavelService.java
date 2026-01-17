package br.com.tcc.assinatura.domain.service;

import br.com.tcc.assinatura.api.dto.DocumentoAssinavelRequest;
import br.com.tcc.assinatura.api.mapper.DocumentoAssinavelMapper;
import br.com.tcc.assinatura.domain.model.DocumentoAssinavel;
import br.com.tcc.assinatura.domain.repository.DocumentoAssinavelRepository;
import br.com.tcc.assinatura.domain.repository.DocumentoDiplomaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentoAssinavelService {

  private final DocumentoAssinavelRepository repository;
  private final DocumentoDiplomaRepository documentoDiplomaRepository;
  private final DocumentoAssinavelMapper mapper;

  public DocumentoAssinavelService(DocumentoAssinavelRepository repository,
      DocumentoDiplomaRepository documentoDiplomaRepository, DocumentoAssinavelMapper mapper) {
    this.repository = repository;
    this.documentoDiplomaRepository = documentoDiplomaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<DocumentoAssinavel> criar(DocumentoAssinavelRequest request) {
    return documentoDiplomaRepository.findById(request.documentoDiplomaId())
        .map(documento -> repository.save(mapper.toEntity(request, documento)));
  }

  public List<DocumentoAssinavel> listar() {
    return repository.findAll();
  }

  public Optional<DocumentoAssinavel> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<DocumentoAssinavel> atualizar(Long id, DocumentoAssinavelRequest request) {
    var documentoOpt = documentoDiplomaRepository.findById(request.documentoDiplomaId());
    if (documentoOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(documentoAssinavel -> {
      mapper.updateEntityFromRequest(request, documentoAssinavel, documentoOpt.get());
      return documentoAssinavel;
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
