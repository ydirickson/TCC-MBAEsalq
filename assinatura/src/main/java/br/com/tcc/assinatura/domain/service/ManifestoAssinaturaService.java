package br.com.tcc.assinatura.domain.service;

import br.com.tcc.assinatura.api.dto.ManifestoAssinaturaRequest;
import br.com.tcc.assinatura.api.mapper.ManifestoAssinaturaMapper;
import br.com.tcc.assinatura.domain.model.ManifestoAssinatura;
import br.com.tcc.assinatura.domain.repository.ManifestoAssinaturaRepository;
import br.com.tcc.assinatura.domain.repository.SolicitacaoAssinaturaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManifestoAssinaturaService {

  private final ManifestoAssinaturaRepository repository;
  private final SolicitacaoAssinaturaRepository solicitacaoRepository;
  private final ManifestoAssinaturaMapper mapper;

  public ManifestoAssinaturaService(ManifestoAssinaturaRepository repository,
      SolicitacaoAssinaturaRepository solicitacaoRepository, ManifestoAssinaturaMapper mapper) {
    this.repository = repository;
    this.solicitacaoRepository = solicitacaoRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<ManifestoAssinatura> criar(ManifestoAssinaturaRequest request) {
    return solicitacaoRepository.findById(request.solicitacaoId())
        .map(solicitacao -> repository.save(mapper.toEntity(request, solicitacao)));
  }

  public List<ManifestoAssinatura> listar() {
    return repository.findAll();
  }

  public Optional<ManifestoAssinatura> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<ManifestoAssinatura> atualizar(Long id, ManifestoAssinaturaRequest request) {
    var solicitacaoOpt = solicitacaoRepository.findById(request.solicitacaoId());
    if (solicitacaoOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(manifesto -> {
      mapper.updateEntityFromRequest(request, manifesto, solicitacaoOpt.get());
      return manifesto;
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
