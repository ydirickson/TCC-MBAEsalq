package br.com.tcc.assinatura.api.controller;

import br.com.tcc.assinatura.api.dto.DocumentoAssinavelRequest;
import br.com.tcc.assinatura.api.dto.DocumentoAssinavelResponse;
import br.com.tcc.assinatura.api.mapper.DocumentoAssinavelMapper;
import br.com.tcc.assinatura.domain.service.DocumentoAssinavelService;
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
@RequestMapping("/documentos-assinaveis")
@Tag(name = "04 - Documentos assinaveis", description = "Operacoes de documentos disponiveis para assinatura")
public class DocumentoAssinavelController {

  private static final Logger log = LoggerFactory.getLogger(DocumentoAssinavelController.class);

  private final DocumentoAssinavelService service;
  private final DocumentoAssinavelMapper mapper;

  public DocumentoAssinavelController(DocumentoAssinavelService service, DocumentoAssinavelMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar documento assinavel", description = "Cria um documento assinavel a partir de um documento de diploma.")
  public ResponseEntity<DocumentoAssinavelResponse> criar(@Valid @RequestBody DocumentoAssinavelRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar documento assinavel: documento diploma nao encontrado id={}", request.documentoDiplomaId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Documento assinavel criado id={} documentoDiplomaId={}", salvo.getId(), request.documentoDiplomaId());
    URI location = uriBuilder.path("/documentos-assinaveis/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar documentos assinaveis", description = "Retorna a lista de documentos assinaveis cadastrados.")
  public List<DocumentoAssinavelResponse> listar() {
    var documentos = service.listar();
    log.info("Listando documentos assinaveis total={}", documentos.size());
    return documentos.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar documento assinavel", description = "Busca um documento assinavel pelo identificador.")
  public ResponseEntity<DocumentoAssinavelResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(documento -> {
          log.info("Documento assinavel encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(documento));
        })
        .orElseGet(() -> {
          log.warn("Documento assinavel nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar documento assinavel", description = "Atualiza um documento assinavel existente.")
  public ResponseEntity<DocumentoAssinavelResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody DocumentoAssinavelRequest request) {
    return service.atualizar(id, request)
        .map(documento -> {
          log.info("Documento assinavel atualizado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(documento));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar documento assinavel id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover documento assinavel", description = "Remove um documento assinavel pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover documento assinavel: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Documento assinavel removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
