package br.com.tcc.assinatura.domain.service;

import br.com.tcc.assinatura.api.dto.SolicitacaoAssinaturaRequest;
import br.com.tcc.assinatura.api.mapper.SolicitacaoAssinaturaMapper;
import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import br.com.tcc.assinatura.domain.repository.DocumentoAssinavelRepository;
import br.com.tcc.assinatura.domain.repository.SolicitacaoAssinaturaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitacaoAssinaturaService {

  private final SolicitacaoAssinaturaRepository repository;
  private final DocumentoAssinavelRepository documentoAssinavelRepository;
  private final SolicitacaoAssinaturaMapper mapper;

  public SolicitacaoAssinaturaService(SolicitacaoAssinaturaRepository repository,
      DocumentoAssinavelRepository documentoAssinavelRepository, SolicitacaoAssinaturaMapper mapper) {
    this.repository = repository;
    this.documentoAssinavelRepository = documentoAssinavelRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<SolicitacaoAssinatura> criar(SolicitacaoAssinaturaRequest request) {
    if (request.documentoAssinavelId() == null) {
      return Optional.empty();
    }
    return documentoAssinavelRepository.findById(request.documentoAssinavelId())
        .map(documento -> repository.save(mapper.toEntity(request, documento)));
  }

  @Transactional
  public Optional<SolicitacaoAssinatura> criar(Long documentoAssinavelId, SolicitacaoAssinaturaRequest request) {
    return documentoAssinavelRepository.findById(documentoAssinavelId)
        .map(documento -> repository.save(mapper.toEntity(request, documento)));
  }

  public List<SolicitacaoAssinatura> listar() {
    return repository.findAll();
  }

  public Optional<List<SolicitacaoAssinatura>> listarPorDocumentoAssinavelId(Long documentoAssinavelId) {
    if (!documentoAssinavelRepository.existsById(documentoAssinavelId)) {
      return Optional.empty();
    }
    return Optional.of(repository.findAllByDocumentoAssinavelIdOrderByDataSolicitacaoDesc(documentoAssinavelId));
  }

  public Optional<SolicitacaoAssinatura> buscarPorId(Long id) {
    return repository.findById(id);
  }

  public Optional<SolicitacaoAssinatura> buscarPorId(Long documentoAssinavelId, Long id) {
    return repository.findByIdAndDocumentoAssinavelId(id, documentoAssinavelId);
  }

  @Transactional
  public Optional<SolicitacaoAssinatura> atualizar(Long id, SolicitacaoAssinaturaRequest request) {
    if (request.documentoAssinavelId() == null) {
      return Optional.empty();
    }
    var documentoOpt = documentoAssinavelRepository.findById(request.documentoAssinavelId());
    if (documentoOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(solicitacao -> {
      mapper.updateEntityFromRequest(request, solicitacao, documentoOpt.get());
      return solicitacao;
    });
  }

  @Transactional
  public Optional<SolicitacaoAssinatura> atualizar(Long documentoAssinavelId, Long id,
      SolicitacaoAssinaturaRequest request) {
    var documentoOpt = documentoAssinavelRepository.findById(documentoAssinavelId);
    if (documentoOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findByIdAndDocumentoAssinavelId(id, documentoAssinavelId).map(solicitacao -> {
      mapper.updateEntityFromRequest(request, solicitacao, documentoOpt.get());
      return solicitacao;
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
  public boolean remover(Long documentoAssinavelId, Long id) {
    if (!repository.existsByIdAndDocumentoAssinavelId(id, documentoAssinavelId)) {
      return false;
    }
    repository.deleteById(id);
    return true;
  }
}
