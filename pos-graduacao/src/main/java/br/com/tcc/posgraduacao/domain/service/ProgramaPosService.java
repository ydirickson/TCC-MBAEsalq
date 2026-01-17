package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.api.dto.ProgramaPosRequest;
import br.com.tcc.posgraduacao.api.mapper.ProgramaPosMapper;
import br.com.tcc.posgraduacao.domain.model.ProgramaPos;
import br.com.tcc.posgraduacao.domain.repository.ProgramaPosRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgramaPosService {

  private final ProgramaPosRepository repository;
  private final ProgramaPosMapper mapper;

  public ProgramaPosService(ProgramaPosRepository repository, ProgramaPosMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional
  public ProgramaPos criarPrograma(ProgramaPosRequest request) {
    return repository.save(mapper.toEntity(request));
  }

  public List<ProgramaPos> listar() {
    return repository.findAll();
  }

  public Optional<ProgramaPos> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<ProgramaPos> atualizar(Long id, ProgramaPosRequest request) {
    return repository.findById(id).map(programa -> {
      mapper.updateEntityFromRequest(request, programa);
      return programa;
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
