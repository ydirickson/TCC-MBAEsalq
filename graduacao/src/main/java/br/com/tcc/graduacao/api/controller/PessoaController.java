package br.com.tcc.graduacao.api.controller;

import br.com.tcc.graduacao.api.dto.ContatoRequest;
import br.com.tcc.graduacao.api.dto.ContatoResponse;
import br.com.tcc.graduacao.api.dto.DocumentoIdentificacaoRequest;
import br.com.tcc.graduacao.api.dto.DocumentoIdentificacaoResponse;
import br.com.tcc.graduacao.api.dto.EnderecoRequest;
import br.com.tcc.graduacao.api.dto.EnderecoResponse;
import br.com.tcc.graduacao.api.dto.PessoaRequest;
import br.com.tcc.graduacao.api.dto.PessoaResponse;
import br.com.tcc.graduacao.api.mapper.ContatoMapper;
import br.com.tcc.graduacao.api.mapper.DocumentoIdentificacaoMapper;
import br.com.tcc.graduacao.api.mapper.EnderecoMapper;
import br.com.tcc.graduacao.api.mapper.PessoaMapper;
import br.com.tcc.graduacao.domain.service.PessoaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/pessoas")
@Tag(name = "10 - Pessoas", description = "Operacoes de cadastro e manutenção de pessoas")
public class PessoaController {

  private static final Logger log = LoggerFactory.getLogger(PessoaController.class);

  private final PessoaService service;
  private final PessoaMapper mapper;
  private final EnderecoMapper enderecoMapper;
  private final ContatoMapper contatoMapper;
  private final DocumentoIdentificacaoMapper documentoMapper;

  public PessoaController(PessoaService service, PessoaMapper mapper, EnderecoMapper enderecoMapper,
      ContatoMapper contatoMapper, DocumentoIdentificacaoMapper documentoMapper) {
    this.service = service;
    this.mapper = mapper;
    this.enderecoMapper = enderecoMapper;
    this.contatoMapper = contatoMapper;
    this.documentoMapper = documentoMapper;
  }

  @PostMapping
  @Operation(summary = "Criar pessoa", description = "Cria uma nova pessoa.")
  public ResponseEntity<PessoaResponse> criar(@Valid @RequestBody PessoaRequest request, UriComponentsBuilder uriBuilder) {
    var salva = service.criar(request);
    log.info("Pessoa criada id={} nome={}", salva.getId(), request.nome());
    URI location = uriBuilder.path("/pessoas/{id}").buildAndExpand(salva.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salva));
  }

  @GetMapping
  @Operation(summary = "Listar pessoas", description = "Retorna a lista de pessoas cadastradas.")
  public List<PessoaResponse> listar() {
    var pessoas = service.listar();
    log.info("Listando pessoas total={}", pessoas.size());
    return pessoas.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar pessoa", description = "Busca uma pessoa pelo identificador.")
  public ResponseEntity<PessoaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(pessoa -> {
          log.info("Pessoa encontrada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(pessoa));
        })
        .orElseGet(() -> {
          log.warn("Pessoa nao encontrada id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar pessoa", description = "Atualiza os dados de uma pessoa existente.")
  public ResponseEntity<PessoaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody PessoaRequest request) {
    return service.atualizar(id, request)
        .map(pessoa -> {
          log.info("Pessoa atualizada id={} nome={}", id, request.nome());
          return ResponseEntity.ok(mapper.toResponse(pessoa));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar pessoa: nao encontrada id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover pessoa", description = "Remove uma pessoa pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover pessoa: nao encontrada id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Pessoa removida id={}", id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{pessoaId}/enderecos")
  @Operation(summary = "Criar endereco", description = "Cria um endereco para a pessoa.")
  public ResponseEntity<EnderecoResponse> criarEndereco(@PathVariable Long pessoaId,
      @Valid @RequestBody EnderecoRequest request, UriComponentsBuilder uriBuilder) {
    return service.criarEndereco(pessoaId, request)
        .map(endereco -> {
          log.info("Endereco criado id={} pessoaId={}", endereco.getId(), pessoaId);
          URI location = uriBuilder.path("/pessoas/{pessoaId}/enderecos/{id}")
              .buildAndExpand(pessoaId, endereco.getId()).toUri();
          return ResponseEntity.created(location).body(enderecoMapper.toResponse(endereco));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar endereco: pessoa nao encontrada pessoaId={}", pessoaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping("/{pessoaId}/enderecos")
  @Operation(summary = "Listar enderecos", description = "Lista os enderecos cadastrados da pessoa.")
  public ResponseEntity<List<EnderecoResponse>> listarEnderecos(@PathVariable Long pessoaId) {
    return service.listarEnderecos(pessoaId)
        .map(enderecos -> {
          log.info("Listando enderecos pessoaId={} total={}", pessoaId, enderecos.size());
          List<EnderecoResponse> response = enderecos.stream()
              .map(enderecoMapper::toResponse)
              .collect(Collectors.toList());
          return ResponseEntity.ok(response);
        })
        .orElseGet(() -> {
          log.warn("Pessoa nao encontrada pessoaId={}", pessoaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @PutMapping("/{pessoaId}/enderecos/{enderecoId}")
  @Operation(summary = "Atualizar endereco", description = "Atualiza um endereco da pessoa.")
  public ResponseEntity<EnderecoResponse> atualizarEndereco(@PathVariable Long pessoaId, @PathVariable Long enderecoId,
      @Valid @RequestBody EnderecoRequest request) {
    return service.atualizarEndereco(pessoaId, enderecoId, request)
        .map(endereco -> {
          log.info("Endereco atualizado id={} pessoaId={}", enderecoId, pessoaId);
          return ResponseEntity.ok(enderecoMapper.toResponse(endereco));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar endereco id={} pessoaId={}", enderecoId, pessoaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{pessoaId}/enderecos/{enderecoId}")
  @Operation(summary = "Remover endereco", description = "Remove um endereco da pessoa.")
  public ResponseEntity<Void> removerEndereco(@PathVariable Long pessoaId, @PathVariable Long enderecoId) {
    boolean removido = service.removerEndereco(pessoaId, enderecoId);
    if (!removido) {
      log.warn("Falha ao remover endereco id={} pessoaId={}", enderecoId, pessoaId);
      return ResponseEntity.notFound().build();
    }
    log.info("Endereco removido id={} pessoaId={}", enderecoId, pessoaId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{pessoaId}/contatos")
  @Operation(summary = "Criar contato", description = "Cria um contato para a pessoa.")
  public ResponseEntity<ContatoResponse> criarContato(@PathVariable Long pessoaId,
      @Valid @RequestBody ContatoRequest request, UriComponentsBuilder uriBuilder) {
    return service.criarContato(pessoaId, request)
        .map(contato -> {
          log.info("Contato criado id={} pessoaId={}", contato.getId(), pessoaId);
          URI location = uriBuilder.path("/pessoas/{pessoaId}/contatos/{id}")
              .buildAndExpand(pessoaId, contato.getId()).toUri();
          return ResponseEntity.created(location).body(contatoMapper.toResponse(contato));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar contato: pessoa nao encontrada pessoaId={}", pessoaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping("/{pessoaId}/contatos")
  @Operation(summary = "Listar contatos", description = "Lista os contatos cadastrados da pessoa.")
  public ResponseEntity<List<ContatoResponse>> listarContatos(@PathVariable Long pessoaId) {
    return service.listarContatos(pessoaId)
        .map(contatos -> {
          log.info("Listando contatos pessoaId={} total={}", pessoaId, contatos.size());
          List<ContatoResponse> response = contatos.stream()
              .map(contatoMapper::toResponse)
              .collect(Collectors.toList());
          return ResponseEntity.ok(response);
        })
        .orElseGet(() -> {
          log.warn("Pessoa nao encontrada pessoaId={}", pessoaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @PutMapping("/{pessoaId}/contatos/{contatoId}")
  @Operation(summary = "Atualizar contato", description = "Atualiza um contato da pessoa.")
  public ResponseEntity<ContatoResponse> atualizarContato(@PathVariable Long pessoaId, @PathVariable Long contatoId,
      @Valid @RequestBody ContatoRequest request) {
    return service.atualizarContato(pessoaId, contatoId, request)
        .map(contato -> {
          log.info("Contato atualizado id={} pessoaId={}", contatoId, pessoaId);
          return ResponseEntity.ok(contatoMapper.toResponse(contato));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar contato id={} pessoaId={}", contatoId, pessoaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{pessoaId}/contatos/{contatoId}")
  @Operation(summary = "Remover contato", description = "Remove um contato da pessoa.")
  public ResponseEntity<Void> removerContato(@PathVariable Long pessoaId, @PathVariable Long contatoId) {
    boolean removido = service.removerContato(pessoaId, contatoId);
    if (!removido) {
      log.warn("Falha ao remover contato id={} pessoaId={}", contatoId, pessoaId);
      return ResponseEntity.notFound().build();
    }
    log.info("Contato removido id={} pessoaId={}", contatoId, pessoaId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{pessoaId}/documento")
  @Operation(summary = "Criar ou atualizar documento", description = "Cria ou atualiza o documento de identificacao da pessoa.")
  public ResponseEntity<DocumentoIdentificacaoResponse> criarOuAtualizarDocumento(@PathVariable Long pessoaId,
      @Valid @RequestBody DocumentoIdentificacaoRequest request) {
    return service.criarOuAtualizarDocumento(pessoaId, request)
        .map(documento -> {
          log.info("Documento identificado pessoaId={} tipo={}", pessoaId, request.tipo());
          return ResponseEntity.ok(documentoMapper.toResponse(documento));
        })
        .orElseGet(() -> {
          log.warn("Falha ao salvar documento: pessoa nao encontrada pessoaId={}", pessoaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping("/{pessoaId}/documento")
  @Operation(summary = "Buscar documento", description = "Busca o documento de identificacao da pessoa.")
  public ResponseEntity<DocumentoIdentificacaoResponse> buscarDocumento(@PathVariable Long pessoaId) {
    return service.buscarDocumentoPorPessoaId(pessoaId)
        .map(documento -> {
          log.info("Documento encontrado pessoaId={}", pessoaId);
          return ResponseEntity.ok(documentoMapper.toResponse(documento));
        })
        .orElseGet(() -> {
          log.warn("Documento nao encontrado pessoaId={}", pessoaId);
          return ResponseEntity.notFound().build();
        });
  }

  @DeleteMapping("/{pessoaId}/documento")
  @Operation(summary = "Remover documento", description = "Remove o documento de identificacao da pessoa.")
  public ResponseEntity<Void> removerDocumento(@PathVariable Long pessoaId) {
    boolean removido = service.removerDocumento(pessoaId);
    if (!removido) {
      log.warn("Falha ao remover documento: nao encontrado pessoaId={}", pessoaId);
      return ResponseEntity.notFound().build();
    }
    log.info("Documento removido pessoaId={}", pessoaId);
    return ResponseEntity.noContent().build();
  }
}
