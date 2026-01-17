package br.com.tcc.diplomas.domain.service;

import br.com.tcc.diplomas.api.dto.VinculoAcademicoRequest;
import br.com.tcc.diplomas.api.mapper.VinculoAcademicoMapper;
import br.com.tcc.diplomas.domain.model.VinculoAcademico;
import br.com.tcc.diplomas.domain.repository.PessoaRepository;
import br.com.tcc.diplomas.domain.repository.VinculoAcademicoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VinculoAcademicoService {

  private final VinculoAcademicoRepository repository;
  private final PessoaRepository pessoaRepository;
  private final VinculoAcademicoMapper mapper;

  public VinculoAcademicoService(VinculoAcademicoRepository repository, PessoaRepository pessoaRepository,
      VinculoAcademicoMapper mapper) {
    this.repository = repository;
    this.pessoaRepository = pessoaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<VinculoAcademico> criar(VinculoAcademicoRequest request) {
    return pessoaRepository.findById(request.pessoaId())
        .map(pessoa -> repository.save(mapper.toEntity(request, pessoa)));
  }

  public List<VinculoAcademico> listar() {
    return repository.findAll();
  }

  public Optional<VinculoAcademico> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<VinculoAcademico> atualizar(Long id, VinculoAcademicoRequest request) {
    var pessoaOpt = pessoaRepository.findById(request.pessoaId());
    if (pessoaOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(vinculo -> {
      mapper.updateEntityFromRequest(request, vinculo, pessoaOpt.get());
      return vinculo;
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
