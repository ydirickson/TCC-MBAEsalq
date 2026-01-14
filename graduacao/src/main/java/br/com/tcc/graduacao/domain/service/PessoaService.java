package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.api.dto.ContatoRequest;
import br.com.tcc.graduacao.api.dto.DocumentoIdentificacaoRequest;
import br.com.tcc.graduacao.api.dto.EnderecoRequest;
import br.com.tcc.graduacao.api.dto.PessoaRequest;
import br.com.tcc.graduacao.api.mapper.ContatoMapper;
import br.com.tcc.graduacao.api.mapper.DocumentoIdentificacaoMapper;
import br.com.tcc.graduacao.api.mapper.EnderecoMapper;
import br.com.tcc.graduacao.api.mapper.PessoaMapper;
import br.com.tcc.graduacao.domain.model.Contato;
import br.com.tcc.graduacao.domain.model.DocumentoIdentificacao;
import br.com.tcc.graduacao.domain.model.Endereco;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.repository.ContatoRepository;
import br.com.tcc.graduacao.domain.repository.DocumentoIdentificacaoRepository;
import br.com.tcc.graduacao.domain.repository.EnderecoRepository;
import br.com.tcc.graduacao.domain.repository.PessoaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PessoaService {

  private final PessoaRepository pessoaRepository;
  private final PessoaMapper pessoaMapper;
  private final EnderecoRepository enderecoRepository;
  private final EnderecoMapper enderecoMapper;
  private final ContatoRepository contatoRepository;
  private final ContatoMapper contatoMapper;
  private final DocumentoIdentificacaoRepository documentoRepository;
  private final DocumentoIdentificacaoMapper documentoMapper;

  public PessoaService(PessoaRepository pessoaRepository, PessoaMapper pessoaMapper, EnderecoRepository enderecoRepository,
      EnderecoMapper enderecoMapper, ContatoRepository contatoRepository, ContatoMapper contatoMapper,
      DocumentoIdentificacaoRepository documentoRepository, DocumentoIdentificacaoMapper documentoMapper) {
    this.pessoaRepository = pessoaRepository;
    this.pessoaMapper = pessoaMapper;
    this.enderecoRepository = enderecoRepository;
    this.enderecoMapper = enderecoMapper;
    this.contatoRepository = contatoRepository;
    this.contatoMapper = contatoMapper;
    this.documentoRepository = documentoRepository;
    this.documentoMapper = documentoMapper;
  }

  @Transactional
  public Pessoa criar(PessoaRequest request) {
    return pessoaRepository.save(pessoaMapper.toEntity(request));
  }

  public List<Pessoa> listar() {
    return pessoaRepository.findAll();
  }

  public Optional<Pessoa> buscarPorId(Long id) {
    return pessoaRepository.findById(id);
  }

  @Transactional
  public Optional<Pessoa> atualizar(Long id, PessoaRequest request) {
    return pessoaRepository.findById(id).map(pessoa -> {
      pessoaMapper.updateEntityFromRequest(request, pessoa);
      return pessoa;
    });
  }

  @Transactional
  public boolean remover(Long id) {
    if (!pessoaRepository.existsById(id)) {
      return false;
    }
    pessoaRepository.deleteById(id);
    return true;
  }

  @Transactional
  public Optional<Endereco> criarEndereco(Long pessoaId, EnderecoRequest request) {
    Optional<Pessoa> pessoaOpt = pessoaRepository.findById(pessoaId);
    if (pessoaOpt.isEmpty()) {
      return Optional.empty();
    }
    Endereco endereco = enderecoMapper.toEntity(pessoaOpt.get(), request);
    return Optional.of(enderecoRepository.save(endereco));
  }

  public Optional<List<Endereco>> listarEnderecos(Long pessoaId) {
    if (!pessoaRepository.existsById(pessoaId)) {
      return Optional.empty();
    }
    return Optional.of(enderecoRepository.findByPessoaId(pessoaId));
  }

  @Transactional
  public Optional<Endereco> atualizarEndereco(Long pessoaId, Long enderecoId, EnderecoRequest request) {
    if (!pessoaRepository.existsById(pessoaId)) {
      return Optional.empty();
    }
    return enderecoRepository.findById(enderecoId)
        .filter(endereco -> endereco.getPessoa() != null && endereco.getPessoa().getId().equals(pessoaId))
        .map(endereco -> {
          enderecoMapper.updateEntityFromRequest(request, endereco);
          return endereco;
        });
  }

  @Transactional
  public boolean removerEndereco(Long pessoaId, Long enderecoId) {
    if (!pessoaRepository.existsById(pessoaId)) {
      return false;
    }
    Optional<Endereco> enderecoOpt = enderecoRepository.findById(enderecoId)
        .filter(endereco -> endereco.getPessoa() != null && endereco.getPessoa().getId().equals(pessoaId));
    if (enderecoOpt.isEmpty()) {
      return false;
    }
    enderecoRepository.delete(enderecoOpt.get());
    return true;
  }

  @Transactional
  public Optional<Contato> criarContato(Long pessoaId, ContatoRequest request) {
    Optional<Pessoa> pessoaOpt = pessoaRepository.findById(pessoaId);
    if (pessoaOpt.isEmpty()) {
      return Optional.empty();
    }
    Contato contato = contatoMapper.toEntity(pessoaOpt.get(), request);
    return Optional.of(contatoRepository.save(contato));
  }

  public Optional<List<Contato>> listarContatos(Long pessoaId) {
    if (!pessoaRepository.existsById(pessoaId)) {
      return Optional.empty();
    }
    return Optional.of(contatoRepository.findByPessoaId(pessoaId));
  }

  @Transactional
  public Optional<Contato> atualizarContato(Long pessoaId, Long contatoId, ContatoRequest request) {
    if (!pessoaRepository.existsById(pessoaId)) {
      return Optional.empty();
    }
    return contatoRepository.findById(contatoId)
        .filter(contato -> contato.getPessoa() != null && contato.getPessoa().getId().equals(pessoaId))
        .map(contato -> {
          contatoMapper.updateEntityFromRequest(request, contato);
          return contato;
        });
  }

  @Transactional
  public boolean removerContato(Long pessoaId, Long contatoId) {
    if (!pessoaRepository.existsById(pessoaId)) {
      return false;
    }
    Optional<Contato> contatoOpt = contatoRepository.findById(contatoId)
        .filter(contato -> contato.getPessoa() != null && contato.getPessoa().getId().equals(pessoaId));
    if (contatoOpt.isEmpty()) {
      return false;
    }
    contatoRepository.delete(contatoOpt.get());
    return true;
  }

  @Transactional
  public Optional<DocumentoIdentificacao> criarOuAtualizarDocumento(Long pessoaId, DocumentoIdentificacaoRequest request) {
    Optional<Pessoa> pessoaOpt = pessoaRepository.findById(pessoaId);
    if (pessoaOpt.isEmpty()) {
      return Optional.empty();
    }
    Pessoa pessoa = pessoaOpt.get();
    Optional<DocumentoIdentificacao> existente = documentoRepository.findByPessoaId(pessoaId);
    if (existente.isPresent()) {
      documentoMapper.updateEntityFromRequest(request, existente.get());
      return existente;
    }
    DocumentoIdentificacao documento = documentoMapper.toEntity(pessoa, request);
    pessoa.setDocumentoIdentificacao(documento);
    return Optional.of(documentoRepository.save(documento));
  }

  public Optional<DocumentoIdentificacao> buscarDocumentoPorPessoaId(Long pessoaId) {
    return documentoRepository.findByPessoaId(pessoaId);
  }

  @Transactional
  public boolean removerDocumento(Long pessoaId) {
    Optional<DocumentoIdentificacao> existente = documentoRepository.findByPessoaId(pessoaId);
    if (existente.isEmpty()) {
      return false;
    }
    documentoRepository.delete(existente.get());
    return true;
  }
}
