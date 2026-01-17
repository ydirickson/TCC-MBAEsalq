package br.com.tcc.assinatura.domain.service;

import br.com.tcc.assinatura.api.dto.PessoaRequest;
import br.com.tcc.assinatura.api.mapper.PessoaMapper;
import br.com.tcc.assinatura.domain.model.Pessoa;
import br.com.tcc.assinatura.domain.repository.PessoaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessoaService {

  private final PessoaRepository repository;
  private final PessoaMapper mapper;

  public PessoaService(PessoaRepository repository, PessoaMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional
  public Pessoa criar(PessoaRequest request) {
    return repository.save(mapper.toEntity(request));
  }

  public List<Pessoa> listar() {
    return repository.findAll();
  }

  public Optional<Pessoa> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<Pessoa> atualizar(Long id, PessoaRequest request) {
    return repository.findById(id).map(pessoa -> {
      mapper.updateEntityFromRequest(request, pessoa);
      return pessoa;
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
