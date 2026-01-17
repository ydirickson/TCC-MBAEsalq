package br.com.tcc.graduacao.api.controller;

import br.com.tcc.graduacao.api.dto.DisciplinaGraduacaoRequest;
import br.com.tcc.graduacao.api.dto.DisciplinaGraduacaoResponse;
import br.com.tcc.graduacao.api.mapper.DisciplinaGraduacaoMapper;
import br.com.tcc.graduacao.domain.service.DisciplinaGraduacaoService;
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
@RequestMapping("/cursos/{cursoId}/disciplinas")
@Tag(name = "02 - Disciplinas de Graduação", description = "Operações de disciplinas vinculadas a cursos")
public class DisciplinaGraduacaoController {

  private static final Logger log = LoggerFactory.getLogger(DisciplinaGraduacaoController.class);

  private final DisciplinaGraduacaoService service;
  private final DisciplinaGraduacaoMapper mapper;

  public DisciplinaGraduacaoController(DisciplinaGraduacaoService service, DisciplinaGraduacaoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar disciplina de graduação", description = "Cria uma disciplina de graduação vinculada a um curso de graduação.")
  public ResponseEntity<DisciplinaGraduacaoResponse> criar(@PathVariable Long cursoId,
      @Valid @RequestBody DisciplinaGraduacaoRequest request,
      UriComponentsBuilder uriBuilder) {
    return service.criar(cursoId, request)
        .map(disciplina -> {
          log.info("Disciplina de graduação criada id={} cursoId={}", disciplina.getId(), cursoId);
          URI location = uriBuilder.path("/cursos/{cursoId}/disciplinas/{id}")
              .buildAndExpand(cursoId, disciplina.getId())
              .toUri();
          return ResponseEntity.created(location).body(mapper.toResponse(disciplina));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar disciplina de graduação: curso nao encontrado cursoId={}", cursoId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar disciplinas de graduação", description = "Lista disciplinas de graduação vinculadas a um curso de graduação.")
  public List<DisciplinaGraduacaoResponse> listar(@PathVariable Long cursoId) {
    var disciplinas = service.listar(cursoId);
    log.info("Listando disciplinas de graduação total={} cursoId={}", disciplinas.size(), cursoId);
    return disciplinas.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{disciplinaId}")
  @Operation(summary = "Buscar disciplina de graduação", description = "Busca uma disciplina de graduação pelo identificador dentro do curso de graduação.")
  public ResponseEntity<DisciplinaGraduacaoResponse> buscarPorId(@PathVariable Long cursoId,
      @PathVariable Long disciplinaId) {
    return service.buscarPorId(cursoId, disciplinaId)
        .map(disciplina -> {
          log.info("Disciplina encontrada id={} cursoId={}", disciplinaId, cursoId);
          return ResponseEntity.ok(mapper.toResponse(disciplina));
        })
        .orElseGet(() -> {
          log.warn("Disciplina nao encontrada id={} cursoId={}", disciplinaId, cursoId);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{disciplinaId}")
  @Operation(summary = "Atualizar disciplina", description = "Atualiza os dados de uma disciplina vinculada a um curso.")
  public ResponseEntity<DisciplinaGraduacaoResponse> atualizar(@PathVariable Long cursoId, @PathVariable Long disciplinaId,
      @Valid @RequestBody DisciplinaGraduacaoRequest request) {
    return service.atualizar(cursoId, disciplinaId, request)
        .map(disciplina -> {
          log.info("Disciplina atualizada id={} cursoId={}", disciplinaId, cursoId);
          return ResponseEntity.ok(mapper.toResponse(disciplina));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar disciplina id={} cursoId={}", disciplinaId, cursoId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{disciplinaId}")
  @Operation(summary = "Remover disciplina", description = "Remove uma disciplina vinculada a um curso.")
  public ResponseEntity<Void> remover(@PathVariable Long cursoId, @PathVariable Long disciplinaId) {
    boolean removido = service.remover(cursoId, disciplinaId);
    if (!removido) {
      log.warn("Falha ao remover disciplina id={} cursoId={}", disciplinaId, cursoId);
      return ResponseEntity.notFound().build();
    }
    log.info("Disciplina removida id={} cursoId={}", disciplinaId, cursoId);
    return ResponseEntity.noContent().build();
  }
}
