package br.com.tcc.diplomas.api.controller;

import br.com.tcc.diplomas.api.dto.DiplomaRequest;
import br.com.tcc.diplomas.api.dto.DiplomaResponse;
import br.com.tcc.diplomas.api.mapper.DiplomaMapper;
import br.com.tcc.diplomas.domain.service.DiplomaService;
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
@RequestMapping("/diplomas")
@Tag(name = "05 - Diplomas", description = "Operacoes de emissao e manutencao de diplomas")
public class DiplomaController {

  private static final Logger log = LoggerFactory.getLogger(DiplomaController.class);

  private final DiplomaService service;
  private final DiplomaMapper mapper;

  public DiplomaController(DiplomaService service, DiplomaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar diploma", description = "Cria um diploma a partir de um requerimento.")
  public ResponseEntity<DiplomaResponse> criar(@Valid @RequestBody DiplomaRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar diploma: requerimento ou base nao encontrados id={}", request.requerimentoId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Diploma criado id={} requerimentoId={}", salvo.getId(), request.requerimentoId());
    URI location = uriBuilder.path("/diplomas/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar diplomas", description = "Retorna a lista de diplomas cadastrados.")
  public List<DiplomaResponse> listar() {
    var diplomas = service.listar();
    log.info("Listando diplomas total={}", diplomas.size());
    return diplomas.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar diploma", description = "Busca um diploma pelo identificador.")
  public ResponseEntity<DiplomaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(diploma -> {
          log.info("Diploma encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(diploma));
        })
        .orElseGet(() -> {
          log.warn("Diploma nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar diploma", description = "Atualiza um diploma existente.")
  public ResponseEntity<DiplomaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody DiplomaRequest request) {
    return service.atualizar(id, request)
        .map(diploma -> {
          log.info("Diploma atualizado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(diploma));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar diploma id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover diploma", description = "Remove um diploma pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover diploma: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Diploma removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
