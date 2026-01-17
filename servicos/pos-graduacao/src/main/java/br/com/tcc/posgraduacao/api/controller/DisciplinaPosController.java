package br.com.tcc.posgraduacao.api.controller;

import br.com.tcc.posgraduacao.api.dto.DisciplinaPosRequest;
import br.com.tcc.posgraduacao.api.dto.DisciplinaPosResponse;
import br.com.tcc.posgraduacao.api.mapper.DisciplinaPosMapper;
import br.com.tcc.posgraduacao.domain.service.DisciplinaPosService;
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
@RequestMapping("/programas/{programaId}/disciplinas")
@Tag(name = "02 - Disciplinas de Pos", description = "Operacoes de disciplinas vinculadas a programas")
public class DisciplinaPosController {

  private static final Logger log = LoggerFactory.getLogger(DisciplinaPosController.class);

  private final DisciplinaPosService service;
  private final DisciplinaPosMapper mapper;

  public DisciplinaPosController(DisciplinaPosService service, DisciplinaPosMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar disciplina de pos", description = "Cria uma disciplina vinculada a um programa de pos.")
  public ResponseEntity<DisciplinaPosResponse> criar(@PathVariable Long programaId,
      @Valid @RequestBody DisciplinaPosRequest request,
      UriComponentsBuilder uriBuilder) {
    return service.criar(programaId, request)
        .map(disciplina -> {
          log.info("Disciplina criada id={} programaId={}", disciplina.getId(), programaId);
          URI location = uriBuilder.path("/programas/{programaId}/disciplinas/{id}")
              .buildAndExpand(programaId, disciplina.getId())
              .toUri();
          return ResponseEntity.created(location).body(mapper.toResponse(disciplina));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar disciplina: programa nao encontrado programaId={}", programaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar disciplinas", description = "Lista disciplinas vinculadas a um programa.")
  public List<DisciplinaPosResponse> listar(@PathVariable Long programaId) {
    var disciplinas = service.listar(programaId);
    log.info("Listando disciplinas total={} programaId={}", disciplinas.size(), programaId);
    return disciplinas.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{disciplinaId}")
  @Operation(summary = "Buscar disciplina", description = "Busca uma disciplina pelo identificador dentro do programa.")
  public ResponseEntity<DisciplinaPosResponse> buscarPorId(@PathVariable Long programaId,
      @PathVariable Long disciplinaId) {
    return service.buscarPorId(programaId, disciplinaId)
        .map(disciplina -> {
          log.info("Disciplina encontrada id={} programaId={}", disciplinaId, programaId);
          return ResponseEntity.ok(mapper.toResponse(disciplina));
        })
        .orElseGet(() -> {
          log.warn("Disciplina nao encontrada id={} programaId={}", disciplinaId, programaId);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{disciplinaId}")
  @Operation(summary = "Atualizar disciplina", description = "Atualiza os dados de uma disciplina vinculada a um programa.")
  public ResponseEntity<DisciplinaPosResponse> atualizar(@PathVariable Long programaId, @PathVariable Long disciplinaId,
      @Valid @RequestBody DisciplinaPosRequest request) {
    return service.atualizar(programaId, disciplinaId, request)
        .map(disciplina -> {
          log.info("Disciplina atualizada id={} programaId={}", disciplinaId, programaId);
          return ResponseEntity.ok(mapper.toResponse(disciplina));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar disciplina id={} programaId={}", disciplinaId, programaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{disciplinaId}")
  @Operation(summary = "Remover disciplina", description = "Remove uma disciplina vinculada a um programa.")
  public ResponseEntity<Void> remover(@PathVariable Long programaId, @PathVariable Long disciplinaId) {
    boolean removido = service.remover(programaId, disciplinaId);
    if (!removido) {
      log.warn("Falha ao remover disciplina id={} programaId={}", disciplinaId, programaId);
      return ResponseEntity.notFound().build();
    }
    log.info("Disciplina removida id={} programaId={}", disciplinaId, programaId);
    return ResponseEntity.noContent().build();
  }
}
