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
@RequestMapping("/documentos-diploma")
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
  public ResponseEntity<DocumentoDiplomaResponse> criar(@Valid @RequestBody DocumentoDiplomaRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar documento: diploma nao encontrado id={}", request.diplomaId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Documento criado id={} diplomaId={}", salvo.getId(), request.diplomaId());
    URI location = uriBuilder.path("/documentos-diploma/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar documentos", description = "Retorna a lista de documentos de diploma cadastrados.")
  public List<DocumentoDiplomaResponse> listar() {
    var documentos = service.listar();
    log.info("Listando documentos de diploma total={}", documentos.size());
    return documentos.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar documento", description = "Busca um documento de diploma pelo identificador.")
  public ResponseEntity<DocumentoDiplomaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
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
  public ResponseEntity<DocumentoDiplomaResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody DocumentoDiplomaRequest request) {
    return service.atualizar(id, request)
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
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover documento: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Documento removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
