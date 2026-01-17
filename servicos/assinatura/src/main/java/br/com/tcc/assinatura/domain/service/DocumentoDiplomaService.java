package br.com.tcc.assinatura.domain.service;

import br.com.tcc.assinatura.api.dto.DocumentoDiplomaRequest;
import br.com.tcc.assinatura.api.mapper.DocumentoDiplomaMapper;
import br.com.tcc.assinatura.domain.model.DocumentoDiploma;
import br.com.tcc.assinatura.domain.repository.DocumentoDiplomaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentoDiplomaService {

  private final DocumentoDiplomaRepository repository;
  private final DocumentoDiplomaMapper mapper;

  public DocumentoDiplomaService(DocumentoDiplomaRepository repository, DocumentoDiplomaMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional
  public DocumentoDiploma criar(DocumentoDiplomaRequest request) {
    return repository.save(mapper.toEntity(request));
  }

  public List<DocumentoDiploma> listar() {
    return repository.findAll();
  }

  public Optional<DocumentoDiploma> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<DocumentoDiploma> atualizar(Long id, DocumentoDiplomaRequest request) {
    return repository.findById(id).map(documento -> {
      mapper.updateEntityFromRequest(request, documento);
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
}
