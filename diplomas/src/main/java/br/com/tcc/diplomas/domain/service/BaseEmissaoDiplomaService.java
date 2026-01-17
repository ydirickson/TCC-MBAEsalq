package br.com.tcc.diplomas.domain.service;

import br.com.tcc.diplomas.api.dto.BaseEmissaoDiplomaRequest;
import br.com.tcc.diplomas.api.mapper.BaseEmissaoDiplomaMapper;
import br.com.tcc.diplomas.domain.model.BaseEmissaoDiploma;
import br.com.tcc.diplomas.domain.repository.BaseEmissaoDiplomaRepository;
import br.com.tcc.diplomas.domain.repository.RequerimentoDiplomaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BaseEmissaoDiplomaService {

  private final BaseEmissaoDiplomaRepository repository;
  private final RequerimentoDiplomaRepository requerimentoRepository;
  private final BaseEmissaoDiplomaMapper mapper;

  public BaseEmissaoDiplomaService(BaseEmissaoDiplomaRepository repository,
      RequerimentoDiplomaRepository requerimentoRepository, BaseEmissaoDiplomaMapper mapper) {
    this.repository = repository;
    this.requerimentoRepository = requerimentoRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<BaseEmissaoDiploma> criar(BaseEmissaoDiplomaRequest request) {
    return requerimentoRepository.findById(request.requerimentoId())
        .map(requerimento -> repository.save(mapper.toEntity(request, requerimento)));
  }

  public List<BaseEmissaoDiploma> listar() {
    return repository.findAll();
  }

  public Optional<BaseEmissaoDiploma> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<BaseEmissaoDiploma> atualizar(Long id, BaseEmissaoDiplomaRequest request) {
    var requerimentoOpt = requerimentoRepository.findById(request.requerimentoId());
    if (requerimentoOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(base -> {
      mapper.updateEntityFromRequest(request, base, requerimentoOpt.get());
      return base;
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
