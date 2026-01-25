package br.com.tcc.posgraduacao.api.controller;

import br.com.tcc.posgraduacao.api.dto.DocumentoOficialPosRequest;
import br.com.tcc.posgraduacao.api.dto.DocumentoOficialPosResponse;
import br.com.tcc.posgraduacao.api.mapper.DocumentoOficialPosMapper;
import br.com.tcc.posgraduacao.domain.service.DocumentoOficialPosService;
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
@RequestMapping("/documentos-oficiais")
@Tag(name = "12 - Documentos oficiais", description = "Operacoes de emissao e manutencao de documentos oficiais")
public class DocumentoOficialPosController {

  private static final Logger log = LoggerFactory.getLogger(DocumentoOficialPosController.class);

  private final DocumentoOficialPosService service;
  private final DocumentoOficialPosMapper mapper;

  public DocumentoOficialPosController(DocumentoOficialPosService service, DocumentoOficialPosMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar documento oficial", description = "Cria um documento oficial da pos-graduacao.")
  public ResponseEntity<DocumentoOficialPosResponse> criar(@Valid @RequestBody DocumentoOficialPosRequest request,
      UriComponentsBuilder uriBuilder) {
    return service.criar(request)
        .map(documento -> {
          URI location = uriBuilder.path("/documentos-oficiais/{id}").buildAndExpand(documento.getId()).toUri();
          log.info("Documento oficial criado id={} pessoaId={} tipo={} versao={}",
              documento.getId(), request.pessoaId(), request.tipoDocumento(), request.versao());
          return ResponseEntity.created(location).body(mapper.toResponse(documento));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar documento oficial: pessoa nao encontrada pessoaId={}", request.pessoaId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar documentos oficiais", description = "Retorna a lista de documentos oficiais.")
  public List<DocumentoOficialPosResponse> listar() {
    var documentos = service.listar();
    log.info("Listando documentos oficiais total={}", documentos.size());
    return documentos.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar documento oficial", description = "Busca um documento oficial pelo identificador.")
  public ResponseEntity<DocumentoOficialPosResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(documento -> {
          log.info("Documento oficial encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(documento));
        })
        .orElseGet(() -> {
          log.warn("Documento oficial nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar documento oficial", description = "Atualiza um documento oficial existente.")
  public ResponseEntity<DocumentoOficialPosResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody DocumentoOficialPosRequest request) {
    return service.atualizar(id, request)
        .map(documento -> {
          log.info("Documento oficial atualizado id={} pessoaId={} tipo={} versao={}",
              id, request.pessoaId(), request.tipoDocumento(), request.versao());
          return ResponseEntity.ok(mapper.toResponse(documento));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar documento oficial: nao encontrado id={} pessoaId={}", id, request.pessoaId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover documento oficial", description = "Remove um documento oficial pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover documento oficial: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Documento oficial removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
