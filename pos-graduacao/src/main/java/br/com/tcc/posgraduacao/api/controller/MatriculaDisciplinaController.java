package br.com.tcc.posgraduacao.api.controller;

import br.com.tcc.posgraduacao.api.dto.MatriculaDisciplinaRequest;
import br.com.tcc.posgraduacao.api.dto.MatriculaDisciplinaResponse;
import br.com.tcc.posgraduacao.api.mapper.MatriculaDisciplinaMapper;
import br.com.tcc.posgraduacao.domain.service.MatriculaDisciplinaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/matriculas")
@Tag(name = "09 - Matriculas", description = "Operacoes de matricula em disciplinas")
public class MatriculaDisciplinaController {

  private static final Logger log = LoggerFactory.getLogger(MatriculaDisciplinaController.class);

  private final MatriculaDisciplinaService service;
  private final MatriculaDisciplinaMapper mapper;

  public MatriculaDisciplinaController(
      MatriculaDisciplinaService service,
      MatriculaDisciplinaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar matricula", description = "Cria uma matricula de aluno em oferta de disciplina.")
  public ResponseEntity<MatriculaDisciplinaResponse> criar(@Valid @RequestBody MatriculaDisciplinaRequest request,
      UriComponentsBuilder uriBuilder) {
    return service.criar(request)
        .map(matricula -> {
          log.info("Matricula criada id={} alunoId={} ofertaDisciplinaId={}", matricula.getId(), request.alunoId(), request.ofertaDisciplinaId());
          URI location = uriBuilder.path("/matriculas/{id}").buildAndExpand(matricula.getId()).toUri();
          return ResponseEntity.created(location).body(mapper.toResponse(matricula));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar matricula alunoId={} ofertaDisciplinaId={}", request.alunoId(), request.ofertaDisciplinaId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar matriculas", description = "Lista matriculas, com filtros opcionais por aluno ou oferta de disciplina.")
  public List<MatriculaDisciplinaResponse> listar(@RequestParam(required = false) Long alunoId,
      @RequestParam(required = false) Long ofertaDisciplinaId) {
    var matriculas = service.listar(alunoId, ofertaDisciplinaId);
    log.info("Listando matriculas total={} alunoId={} ofertaDisciplinaId={}", matriculas.size(), alunoId, ofertaDisciplinaId);
    return matriculas.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar matricula", description = "Busca uma matricula pelo identificador.")
  public ResponseEntity<MatriculaDisciplinaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(matricula -> {
          log.info("Matricula encontrada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(matricula));
        })
        .orElseGet(() -> {
          log.warn("Matricula nao encontrada id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar matricula", description = "Atualiza os dados de uma matricula.")
  public ResponseEntity<MatriculaDisciplinaResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody MatriculaDisciplinaRequest request) {
    return service.atualizar(id, request)
        .map(matricula -> {
          log.info("Matricula atualizada id={} alunoId={} ofertaDisciplinaId={}", id, request.alunoId(), request.ofertaDisciplinaId());
          return ResponseEntity.ok(mapper.toResponse(matricula));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar matricula id={} alunoId={} ofertaDisciplinaId={}", id, request.alunoId(), request.ofertaDisciplinaId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover matricula", description = "Remove uma matricula pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover matricula id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Matricula removida id={}", id);
    return ResponseEntity.noContent().build();
  }

}
