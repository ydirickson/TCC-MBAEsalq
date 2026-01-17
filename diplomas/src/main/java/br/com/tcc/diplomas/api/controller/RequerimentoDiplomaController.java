package br.com.tcc.diplomas.api.controller;

import br.com.tcc.diplomas.api.dto.RequerimentoDiplomaRequest;
import br.com.tcc.diplomas.api.dto.RequerimentoDiplomaResponse;
import br.com.tcc.diplomas.api.mapper.RequerimentoDiplomaMapper;
import br.com.tcc.diplomas.domain.service.RequerimentoDiplomaService;
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
@RequestMapping("/requerimentos")
@Tag(name = "03 - Requerimentos", description = "Operacoes de requerimento de diploma")
public class RequerimentoDiplomaController {

  private static final Logger log = LoggerFactory.getLogger(RequerimentoDiplomaController.class);

  private final RequerimentoDiplomaService service;
  private final RequerimentoDiplomaMapper mapper;

  public RequerimentoDiplomaController(RequerimentoDiplomaService service, RequerimentoDiplomaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar requerimento", description = "Registra um requerimento de diploma e cria a base de emissao.")
  public ResponseEntity<RequerimentoDiplomaResponse> criar(@Valid @RequestBody RequerimentoDiplomaRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvo = service.criar(request);
    log.info("Requerimento criado id={} pessoaId={} vinculoId={}", salvo.getId(), request.pessoaId(), request.vinculoId());
    URI location = uriBuilder.path("/requerimentos/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar requerimentos", description = "Retorna a lista de requerimentos registrados.")
  public List<RequerimentoDiplomaResponse> listar() {
    var requerimentos = service.listar();
    log.info("Listando requerimentos total={}", requerimentos.size());
    return requerimentos.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar requerimento", description = "Busca um requerimento pelo identificador.")
  public ResponseEntity<RequerimentoDiplomaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(requerimento -> {
          log.info("Requerimento encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(requerimento));
        })
        .orElseGet(() -> {
          log.warn("Requerimento nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar requerimento", description = "Atualiza um requerimento existente.")
  public ResponseEntity<RequerimentoDiplomaResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody RequerimentoDiplomaRequest request) {
    return service.atualizar(id, request)
        .map(requerimento -> {
          log.info("Requerimento atualizado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(requerimento));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar requerimento id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover requerimento", description = "Remove um requerimento pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover requerimento: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Requerimento removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
