package br.com.tcc.assinatura.domain.service;

import br.com.tcc.assinatura.api.dto.AssinaturaRequest;
import br.com.tcc.assinatura.api.mapper.AssinaturaMapper;
import br.com.tcc.assinatura.domain.model.Assinatura;
import br.com.tcc.assinatura.domain.repository.AssinaturaRepository;
import br.com.tcc.assinatura.domain.repository.SolicitacaoAssinaturaRepository;
import br.com.tcc.assinatura.domain.repository.UsuarioAssinanteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssinaturaService {

  private final AssinaturaRepository repository;
  private final SolicitacaoAssinaturaRepository solicitacaoRepository;
  private final UsuarioAssinanteRepository usuarioAssinanteRepository;
  private final AssinaturaMapper mapper;

  public AssinaturaService(AssinaturaRepository repository, SolicitacaoAssinaturaRepository solicitacaoRepository,
      UsuarioAssinanteRepository usuarioAssinanteRepository, AssinaturaMapper mapper) {
    this.repository = repository;
    this.solicitacaoRepository = solicitacaoRepository;
    this.usuarioAssinanteRepository = usuarioAssinanteRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<Assinatura> criar(AssinaturaRequest request) {
    var solicitacaoOpt = solicitacaoRepository.findById(request.solicitacaoId());
    var usuarioOpt = usuarioAssinanteRepository.findById(request.usuarioAssinanteId());
    if (solicitacaoOpt.isEmpty() || usuarioOpt.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(repository.save(mapper.toEntity(request, solicitacaoOpt.get(), usuarioOpt.get())));
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
}
