package br.com.tcc.assinatura.domain.service;

import br.com.tcc.assinatura.api.dto.UsuarioAssinanteRequest;
import br.com.tcc.assinatura.api.mapper.UsuarioAssinanteMapper;
import br.com.tcc.assinatura.domain.model.UsuarioAssinante;
import br.com.tcc.assinatura.domain.repository.PessoaRepository;
import br.com.tcc.assinatura.domain.repository.UsuarioAssinanteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioAssinanteService {

  private final UsuarioAssinanteRepository repository;
  private final PessoaRepository pessoaRepository;
  private final UsuarioAssinanteMapper mapper;

  public UsuarioAssinanteService(UsuarioAssinanteRepository repository, PessoaRepository pessoaRepository,
      UsuarioAssinanteMapper mapper) {
    this.repository = repository;
    this.pessoaRepository = pessoaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<UsuarioAssinante> criar(UsuarioAssinanteRequest request) {
    return pessoaRepository.findById(request.pessoaId())
        .map(pessoa -> repository.save(mapper.toEntity(request, pessoa)));
  }

  public List<UsuarioAssinante> listar() {
    return repository.findAll();
  }

  public Optional<UsuarioAssinante> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<UsuarioAssinante> atualizar(Long id, UsuarioAssinanteRequest request) {
    var pessoaOpt = pessoaRepository.findById(request.pessoaId());
    if (pessoaOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(usuario -> {
      mapper.updateEntityFromRequest(request, usuario, pessoaOpt.get());
      return usuario;
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
