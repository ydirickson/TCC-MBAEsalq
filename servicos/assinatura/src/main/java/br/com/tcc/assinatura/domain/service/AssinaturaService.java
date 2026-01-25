package br.com.tcc.assinatura.domain.service;

import br.com.tcc.assinatura.api.dto.AssinaturaRequest;
import br.com.tcc.assinatura.api.mapper.AssinaturaMapper;
import br.com.tcc.assinatura.domain.model.Assinatura;
import br.com.tcc.assinatura.domain.model.ManifestoAssinatura;
import br.com.tcc.assinatura.domain.model.StatusAssinatura;
import br.com.tcc.assinatura.domain.model.StatusSolicitacaoAssinatura;
import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import br.com.tcc.assinatura.domain.repository.AssinaturaRepository;
import br.com.tcc.assinatura.domain.repository.ManifestoAssinaturaRepository;
import br.com.tcc.assinatura.domain.repository.SolicitacaoAssinaturaRepository;
import br.com.tcc.assinatura.domain.repository.UsuarioAssinanteRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssinaturaService {

  private final AssinaturaRepository repository;
  private final SolicitacaoAssinaturaRepository solicitacaoRepository;
  private final UsuarioAssinanteRepository usuarioAssinanteRepository;
  private final ManifestoAssinaturaRepository manifestoRepository;
  private final AssinaturaMapper mapper;

  public AssinaturaService(AssinaturaRepository repository, SolicitacaoAssinaturaRepository solicitacaoRepository,
      UsuarioAssinanteRepository usuarioAssinanteRepository, ManifestoAssinaturaRepository manifestoRepository,
      AssinaturaMapper mapper) {
    this.repository = repository;
    this.solicitacaoRepository = solicitacaoRepository;
    this.usuarioAssinanteRepository = usuarioAssinanteRepository;
    this.manifestoRepository = manifestoRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<Assinatura> criar(AssinaturaRequest request) {
    var solicitacaoOpt = solicitacaoRepository.findById(request.solicitacaoId());
    var usuarioOpt = usuarioAssinanteRepository.findById(request.usuarioAssinanteId());
    if (solicitacaoOpt.isEmpty() || usuarioOpt.isEmpty()) {
      return Optional.empty();
    }
    Assinatura assinatura = repository.save(mapper.toEntity(request, solicitacaoOpt.get(), usuarioOpt.get()));
    aplicarResultadoAssinatura(assinatura, request.dataAssinatura());
    return Optional.of(assinatura);
  }

  public List<Assinatura> listar() {
    return repository.findAll();
  }

  public Optional<Assinatura> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<Assinatura> atualizar(Long id, AssinaturaRequest request) {
    var solicitacaoOpt = solicitacaoRepository.findById(request.solicitacaoId());
    var usuarioOpt = usuarioAssinanteRepository.findById(request.usuarioAssinanteId());
    if (solicitacaoOpt.isEmpty() || usuarioOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(assinatura -> {
      mapper.updateEntityFromRequest(request, assinatura, solicitacaoOpt.get(), usuarioOpt.get());
      aplicarResultadoAssinatura(assinatura, request.dataAssinatura());
      return assinatura;
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

  private void aplicarResultadoAssinatura(Assinatura assinatura, LocalDateTime dataConclusao) {
    if (assinatura == null || assinatura.getSolicitacao() == null) {
      return;
    }
    var solicitacao = assinatura.getSolicitacao();
    if (assinatura.getStatus() == StatusAssinatura.ASSINADA) {
      solicitacao.setStatus(StatusSolicitacaoAssinatura.CONCLUIDA);
      solicitacao.setDataConclusao(dataConclusao != null ? dataConclusao : LocalDateTime.now());
      criarManifestoSeNecessario(solicitacao);
    } else if (assinatura.getStatus() == StatusAssinatura.REJEITADA) {
      solicitacao.setStatus(StatusSolicitacaoAssinatura.REJEITADA);
      solicitacao.setDataConclusao(dataConclusao != null ? dataConclusao : LocalDateTime.now());
    }
  }

  private void criarManifestoSeNecessario(SolicitacaoAssinatura solicitacao) {
    if (solicitacao == null || solicitacao.getId() == null) {
      return;
    }
    if (manifestoRepository.existsBySolicitacaoId(solicitacao.getId())) {
      return;
    }
    ManifestoAssinatura manifesto = new ManifestoAssinatura();
    manifesto.setSolicitacao(solicitacao);
    manifesto.setAuditoria("Assinatura concluida");
    manifesto.setCarimboTempo(LocalDateTime.now());
    manifesto.setHashFinal("hash-final-" + solicitacao.getId());
    manifestoRepository.save(manifesto);
  }
}
