package br.com.tcc.assinatura.domain.service;

import br.com.tcc.assinatura.api.dto.SolicitacaoAssinaturaRequest;
import br.com.tcc.assinatura.api.mapper.SolicitacaoAssinaturaMapper;
import br.com.tcc.assinatura.domain.model.Assinatura;
import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import br.com.tcc.assinatura.domain.model.StatusAssinatura;
import br.com.tcc.assinatura.domain.model.StatusSolicitacaoAssinatura;
import br.com.tcc.assinatura.domain.repository.AssinaturaRepository;
import br.com.tcc.assinatura.domain.repository.DocumentoAssinavelRepository;
import br.com.tcc.assinatura.domain.repository.SolicitacaoAssinaturaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitacaoAssinaturaService {

  private final SolicitacaoAssinaturaRepository repository;
  private final DocumentoAssinavelRepository documentoAssinavelRepository;
  private final AssinaturaRepository assinaturaRepository;
  private final SolicitacaoAssinaturaMapper mapper;

  public SolicitacaoAssinaturaService(SolicitacaoAssinaturaRepository repository,
      DocumentoAssinavelRepository documentoAssinavelRepository, AssinaturaRepository assinaturaRepository,
      SolicitacaoAssinaturaMapper mapper) {
    this.repository = repository;
    this.documentoAssinavelRepository = documentoAssinavelRepository;
    this.assinaturaRepository = assinaturaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<SolicitacaoAssinatura> criar(SolicitacaoAssinaturaRequest request) {
    if (request.documentoAssinavelId() == null) {
      return Optional.empty();
    }
    if (existeSolicitacaoAtivaOuConcluida(request.documentoAssinavelId())) {
      return Optional.empty();
    }
    return documentoAssinavelRepository.findById(request.documentoAssinavelId())
        .map(documento -> {
          SolicitacaoAssinatura solicitacao = repository.save(mapper.toEntity(request, documento));
          criarAssinaturaPendenteSeNecessario(solicitacao);
          return solicitacao;
        });
  }

  @Transactional
  public Optional<SolicitacaoAssinatura> criar(Long documentoAssinavelId, SolicitacaoAssinaturaRequest request) {
    if (existeSolicitacaoAtivaOuConcluida(documentoAssinavelId)) {
      return Optional.empty();
    }
    return documentoAssinavelRepository.findById(documentoAssinavelId)
        .map(documento -> {
          SolicitacaoAssinatura solicitacao = repository.save(mapper.toEntity(request, documento));
          criarAssinaturaPendenteSeNecessario(solicitacao);
          return solicitacao;
        });
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
    return repository.findById(id).map(this::cancelarSolicitacao).orElse(false);
  }

  @Transactional
  public boolean remover(Long documentoAssinavelId, Long id) {
    return repository.findByIdAndDocumentoAssinavelId(id, documentoAssinavelId)
        .map(this::cancelarSolicitacao)
        .orElse(false);
  }

  private boolean existeSolicitacaoAtivaOuConcluida(Long documentoAssinavelId) {
    return repository.existsByDocumentoAssinavelIdAndStatusIn(
        documentoAssinavelId,
        List.of(StatusSolicitacaoAssinatura.PENDENTE, StatusSolicitacaoAssinatura.PARCIAL,
            StatusSolicitacaoAssinatura.CONCLUIDA));
  }

  private void criarAssinaturaPendenteSeNecessario(SolicitacaoAssinatura solicitacao) {
    if (solicitacao == null || solicitacao.getId() == null) {
      return;
    }
    if (assinaturaRepository.existsBySolicitacaoId(solicitacao.getId())) {
      return;
    }
    Assinatura assinatura = new Assinatura();
    assinatura.setSolicitacao(solicitacao);
    assinatura.setStatus(StatusAssinatura.PENDENTE);
    assinaturaRepository.save(assinatura);
  }

  private boolean cancelarSolicitacao(SolicitacaoAssinatura solicitacao) {
    if (solicitacao == null) {
      return false;
    }
    if (solicitacao.getStatus() == StatusSolicitacaoAssinatura.CONCLUIDA) {
      return false;
    }
    if (solicitacao.getStatus() == StatusSolicitacaoAssinatura.CANCELADA) {
      return true;
    }
    if (solicitacao.getStatus() == StatusSolicitacaoAssinatura.REJEITADA) {
      return true;
    }
    solicitacao.setStatus(StatusSolicitacaoAssinatura.CANCELADA);
    solicitacao.setDataConclusao(LocalDateTime.now());

    var assinaturas = assinaturaRepository.findAllBySolicitacaoId(solicitacao.getId());
    for (Assinatura assinatura : assinaturas) {
      if (assinatura.getStatus() == StatusAssinatura.PENDENTE) {
        assinatura.setStatus(StatusAssinatura.REJEITADA);
        assinatura.setMotivoRecusa("Solicitacao cancelada");
        assinatura.setDataAssinatura(LocalDateTime.now());
      }
    }
    return true;
  }
}
