package br.com.tcc.diplomas.domain.service;

import br.com.tcc.diplomas.api.dto.StatusEmissaoRequest;
import br.com.tcc.diplomas.api.mapper.StatusEmissaoMapper;
import br.com.tcc.diplomas.domain.model.StatusEmissao;
import br.com.tcc.diplomas.domain.repository.RequerimentoDiplomaRepository;
import br.com.tcc.diplomas.domain.repository.StatusEmissaoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatusEmissaoService {

  private final StatusEmissaoRepository repository;
  private final RequerimentoDiplomaRepository requerimentoRepository;
  private final StatusEmissaoMapper mapper;

  public StatusEmissaoService(StatusEmissaoRepository repository, RequerimentoDiplomaRepository requerimentoRepository,
      StatusEmissaoMapper mapper) {
    this.repository = repository;
    this.requerimentoRepository = requerimentoRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<StatusEmissao> criar(StatusEmissaoRequest request) {
    return requerimentoRepository.findById(request.requerimentoId())
        .map(requerimento -> repository.save(mapper.toEntity(request, requerimento)));
  }

  public List<StatusEmissao> listar() {
    return repository.findAll();
  }

  public Optional<StatusEmissao> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<StatusEmissao> atualizar(Long id, StatusEmissaoRequest request) {
    var requerimentoOpt = requerimentoRepository.findById(request.requerimentoId());
    if (requerimentoOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(status -> {
      mapper.updateEntityFromRequest(request, status, requerimentoOpt.get());
      return status;
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
