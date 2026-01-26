package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.api.dto.DocumentoOficialGraduacaoRequest;
import br.com.tcc.graduacao.api.mapper.DocumentoOficialGraduacaoMapper;
import br.com.tcc.graduacao.domain.model.DocumentoOficialGraduacao;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.repository.DocumentoOficialGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentoOficialGraduacaoService {

  private final DocumentoOficialGraduacaoRepository repository;
  private final PessoaRepository pessoaRepository;
  private final DocumentoOficialGraduacaoMapper mapper;

  public DocumentoOficialGraduacaoService(DocumentoOficialGraduacaoRepository repository,
      PessoaRepository pessoaRepository, DocumentoOficialGraduacaoMapper mapper) {
    this.repository = repository;
    this.pessoaRepository = pessoaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<DocumentoOficialGraduacao> criar(DocumentoOficialGraduacaoRequest request) {
    Pessoa pessoa = obterPessoa(request.pessoaId());
    if (request.pessoaId() != null && pessoa == null) {
      return Optional.empty();
    }
    DocumentoOficialGraduacao documento = mapper.toEntity(request, pessoa);
    return Optional.of(repository.save(documento));
  }

  public List<DocumentoOficialGraduacao> listar() {
    return repository.findAll();
  }

  public Optional<DocumentoOficialGraduacao> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<DocumentoOficialGraduacao> atualizar(Long id, DocumentoOficialGraduacaoRequest request) {
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
