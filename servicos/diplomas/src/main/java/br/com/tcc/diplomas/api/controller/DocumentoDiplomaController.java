package br.com.tcc.diplomas.api.controller;

import br.com.tcc.diplomas.api.dto.DocumentoDiplomaRequest;
import br.com.tcc.diplomas.api.dto.DocumentoDiplomaResponse;
import br.com.tcc.diplomas.api.mapper.DocumentoDiplomaMapper;
import br.com.tcc.diplomas.domain.service.DocumentoDiplomaService;
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
@RequestMapping("/diplomas/{diplomaId}/documentos")
@Tag(name = "06 - Documentos de diploma", description = "Operacoes de documentos PDF emitidos")
public class DocumentoDiplomaController {

  private static final Logger log = LoggerFactory.getLogger(DocumentoDiplomaController.class);

  private final DocumentoDiplomaService service;
  private final DocumentoDiplomaMapper mapper;

  public DocumentoDiplomaController(DocumentoDiplomaService service, DocumentoDiplomaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar documento", description = "Cria um documento de diploma associado a um diploma.")
  public ResponseEntity<DocumentoDiplomaResponse> criar(@PathVariable Long diplomaId,
      @Valid @RequestBody DocumentoDiplomaRequest request, UriComponentsBuilder uriBuilder) {
    if (request.diplomaId() != null && !request.diplomaId().equals(diplomaId)) {
      log.warn("Falha ao criar documento: diplomaId divergente path={} body={}", diplomaId, request.diplomaId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    DocumentoDiplomaRequest ajustado = new DocumentoDiplomaRequest(
        diplomaId,
        request.versao(),
        request.dataGeracao(),
        request.urlArquivo(),
        request.hashDocumento());
    var salvoOpt = service.criar(diplomaId, ajustado);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar documento: diploma nao encontrado id={}", diplomaId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    var salvo = salvoOpt.get();
    log.info("Documento criado id={} diplomaId={}", salvo.getId(), diplomaId);
    URI location = uriBuilder.path("/diplomas/{diplomaId}/documentos/{id}")
        .buildAndExpand(diplomaId, salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar documentos", description = "Retorna a lista de documentos de um diploma.")
  public ResponseEntity<List<DocumentoDiplomaResponse>> listar(@PathVariable Long diplomaId) {
    return service.listarPorDiplomaId(diplomaId)
        .map(documentos -> {
          log.info("Listando documentos do diploma id={} total={}", diplomaId, documentos.size());
          var resposta = documentos.stream().map(mapper::toResponse).collect(Collectors.toList());
          return ResponseEntity.ok(resposta);
        })
        .orElseGet(() -> {
          log.warn("Diploma nao encontrado id={}", diplomaId);
          return ResponseEntity.notFound().build();
        });
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar documento", description = "Busca um documento de diploma pelo identificador.")
  public ResponseEntity<DocumentoDiplomaResponse> buscarPorId(@PathVariable Long diplomaId, @PathVariable Long id) {
    return service.buscarPorId(diplomaId, id)
        .map(documento -> {
          log.info("Documento encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(documento));
        })
        .orElseGet(() -> {
          log.warn("Documento nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar documento", description = "Atualiza um documento de diploma existente.")
  public ResponseEntity<DocumentoDiplomaResponse> atualizar(@PathVariable Long diplomaId, @PathVariable Long id,
      @Valid @RequestBody DocumentoDiplomaRequest request) {
    if (request.diplomaId() != null && !request.diplomaId().equals(diplomaId)) {
      log.warn("Falha ao atualizar documento: diplomaId divergente path={} body={}", diplomaId, request.diplomaId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    DocumentoDiplomaRequest ajustado = new DocumentoDiplomaRequest(
        diplomaId,
        request.versao(),
        request.dataGeracao(),
        request.urlArquivo(),
        request.hashDocumento());
    return service.atualizar(diplomaId, id, ajustado)
        .map(documento -> {
          log.info("Documento atualizado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(documento));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar documento id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover documento", description = "Remove um documento de diploma pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long diplomaId, @PathVariable Long id) {
    boolean removido = service.remover(diplomaId, id);
    if (!removido) {
      log.warn("Falha ao remover documento: nao encontrado id={} diplomaId={}", id, diplomaId);
      return ResponseEntity.notFound().build();
    }
    log.info("Documento removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
