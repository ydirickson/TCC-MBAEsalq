package br.com.tcc.graduacao.api.controller;

import br.com.tcc.graduacao.api.dto.TurmaGraduacaoRequest;
import br.com.tcc.graduacao.api.dto.TurmaGraduacaoResponse;
import br.com.tcc.graduacao.api.mapper.TurmaGraduacaoMapper;
import br.com.tcc.graduacao.domain.service.TurmaGraduacaoService;
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
@RequestMapping("/cursos/{cursoId}/turmas")
@Tag(name = "03 - Turmas de Graduação", description = "Operacoes de turmas vinculadas a cursos de graduacao")
public class TurmaGraduacaoController {

  private static final Logger log = LoggerFactory.getLogger(TurmaGraduacaoController.class);

  private final TurmaGraduacaoService service;
  private final TurmaGraduacaoMapper mapper;

  public TurmaGraduacaoController(TurmaGraduacaoService service, TurmaGraduacaoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar turma de graduação", description = "Cria uma turma vinculada a um curso de graduação.")
  public ResponseEntity<TurmaGraduacaoResponse> criar(
      @PathVariable Long cursoId,
      @Valid @RequestBody TurmaGraduacaoRequest request,
      UriComponentsBuilder uriBuilder) {
    return service.criar(cursoId, request)
        .map(turma -> {
          log.info("Turma criada id={} cursoId={}", turma.getId(), cursoId);
          URI location = uriBuilder.path("/cursos/{cursoId}/turmas/{id}")
              .buildAndExpand(cursoId, turma.getId())
              .toUri();
          return ResponseEntity.created(location).body(mapper.toResponse(turma));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar turma: curso nao encontrado cursoId={}", cursoId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar turmas de graduação", description = "Lista turmas vinculadas a um curso de graduação.")
  public List<TurmaGraduacaoResponse> listar(@PathVariable Long cursoId) {
    var turmas = service.listar(cursoId);
    log.info("Listando turmas total={} cursoId={}", turmas.size(), cursoId);
    return turmas.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{turmaId}")
  @Operation(summary = "Buscar turma de graduação", description = "Busca uma turma pelo identificador dentro do curso.")
  public ResponseEntity<TurmaGraduacaoResponse> buscarPorId(@PathVariable Long cursoId,
      @PathVariable String turmaId) {
    return service.buscarPorId(cursoId, turmaId)
        .map(turma -> {
          log.info("Turma encontrada id={} cursoId={}", turmaId, cursoId);
          return ResponseEntity.ok(mapper.toResponse(turma));
        })
        .orElseGet(() -> {
          log.warn("Turma nao encontrada id={} cursoId={}", turmaId, cursoId);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{turmaId}")
  @Operation(summary = "Atualizar turma de graduação", description = "Atualiza os dados de uma turma vinculada a um curso.")
  public ResponseEntity<TurmaGraduacaoResponse> atualizar(
      @PathVariable Long cursoId,
      @PathVariable String turmaId,
      @Valid @RequestBody TurmaGraduacaoRequest request) {
    return service.atualizar(cursoId, turmaId, request)
        .map(turma -> {
          log.info("Turma atualizada id={} cursoId={}", turmaId, cursoId);
          return ResponseEntity.ok(mapper.toResponse(turma));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar turma id={} cursoId={}", turmaId, cursoId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{turmaId}")
  @Operation(summary = "Remover turma de graduação", description = "Remove uma turma vinculada a um curso.")
  public ResponseEntity<Void> remover(@PathVariable Long cursoId, @PathVariable String turmaId) {
    boolean removido = service.remover(cursoId, turmaId);
    if (!removido) {
      log.warn("Falha ao remover turma id={} cursoId={}", turmaId, cursoId);
      return ResponseEntity.notFound().build();
    }
    log.info("Turma removida id={} cursoId={}", turmaId, cursoId);
    return ResponseEntity.noContent().build();
  }
}
