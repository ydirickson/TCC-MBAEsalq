package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.api.dto.DocumentoOficialPosRequest;
import br.com.tcc.posgraduacao.api.mapper.DocumentoOficialPosMapper;
import br.com.tcc.posgraduacao.domain.model.DocumentoOficialPos;
import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.repository.DocumentoOficialPosRepository;
import br.com.tcc.posgraduacao.domain.repository.PessoaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentoOficialPosService {

  private final DocumentoOficialPosRepository repository;
  private final PessoaRepository pessoaRepository;
  private final DocumentoOficialPosMapper mapper;

  public DocumentoOficialPosService(DocumentoOficialPosRepository repository, PessoaRepository pessoaRepository,
      DocumentoOficialPosMapper mapper) {
    this.repository = repository;
    this.pessoaRepository = pessoaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<DocumentoOficialPos> criar(DocumentoOficialPosRequest request) {
    Pessoa pessoa = obterPessoa(request.pessoaId());
    if (request.pessoaId() != null && pessoa == null) {
      return Optional.empty();
    }
    DocumentoOficialPos documento = mapper.toEntity(request, pessoa);
    return Optional.of(repository.save(documento));
  }

  public List<DocumentoOficialPos> listar() {
    return repository.findAll();
  }

  public Optional<DocumentoOficialPos> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<DocumentoOficialPos> atualizar(Long id, DocumentoOficialPosRequest request) {
    Pessoa pessoa = obterPessoa(request.pessoaId());
    if (request.pessoaId() != null && pessoa == null) {
      return Optional.empty();
    }
    return repository.findById(id).map(documento -> {
      mapper.updateEntityFromRequest(request, documento, pessoa);
      return documento;
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

  private Pessoa obterPessoa(Long pessoaId) {
    if (pessoaId == null) {
      return null;
    }
    return pessoaRepository.findById(pessoaId).orElse(null);
  }
}
