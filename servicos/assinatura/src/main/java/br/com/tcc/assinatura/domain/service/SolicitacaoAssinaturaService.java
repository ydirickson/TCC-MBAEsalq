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
    return documentoAssinavelRepository.findById(request.documentoAssinavelId())
        .map(documento -> repository.save(mapper.toEntity(request, documento)));
  }

  public List<SolicitacaoAssinatura> listar() {
    return repository.findAll();
  }

  public Optional<SolicitacaoAssinatura> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<SolicitacaoAssinatura> atualizar(Long id, SolicitacaoAssinaturaRequest request) {
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
  public boolean remover(Long id) {
    if (!repository.existsById(id)) {
      return false;
    }
    repository.deleteById(id);
    return true;
  }
}
