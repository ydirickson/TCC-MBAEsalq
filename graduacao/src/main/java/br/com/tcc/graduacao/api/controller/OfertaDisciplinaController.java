package br.com.tcc.graduacao.api.controller;

import br.com.tcc.graduacao.api.dto.OfertaDisciplinaRequest;
import br.com.tcc.graduacao.api.dto.OfertaDisciplinaResponse;
import br.com.tcc.graduacao.api.mapper.OfertaDisciplinaMapper;
import br.com.tcc.graduacao.domain.service.OfertaDisciplinaService;
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
@RequestMapping("/ofertas-disciplinas")
@Tag(name = "10 - Ofertas de Disciplina", description = "Operacoes de cadastro de ofertas de disciplina")
public class OfertaDisciplinaController {

  private static final Logger log = LoggerFactory.getLogger(OfertaDisciplinaController.class);

  private final OfertaDisciplinaService service;
  private final OfertaDisciplinaMapper mapper;

  public OfertaDisciplinaController(OfertaDisciplinaService service, OfertaDisciplinaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar oferta de disciplina", description = "Cria uma oferta de disciplina vinculada a disciplina e professor.")
  public ResponseEntity<OfertaDisciplinaResponse> criar(
      @Valid @RequestBody OfertaDisciplinaRequest request,
      UriComponentsBuilder uriBuilder) {
    return service.criar(request)
        .map(oferta -> {
          log.info("Oferta criada id={} disciplinaId={} professorId={}",
              oferta.getId(), request.disciplinaId(), request.professorId());
          URI location = uriBuilder.path("/ofertas-disciplinas/{id}")
              .buildAndExpand(oferta.getId())
              .toUri();
          return ResponseEntity.created(location).body(mapper.toResponse(oferta));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar oferta: disciplina ou professor nao encontrados disciplinaId={} professorId={}",
              request.disciplinaId(), request.professorId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar ofertas de disciplina", description = "Lista ofertas com filtros opcionais por disciplina ou professor.")
  public List<OfertaDisciplinaResponse> listar(
      @RequestParam(required = false) Long disciplinaId,
      @RequestParam(required = false) Long professorId) {
    var ofertas = service.listar(disciplinaId, professorId);
    log.info("Listando ofertas total={} disciplinaId={} professorId={}", ofertas.size(), disciplinaId, professorId);
    return ofertas.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar oferta de disciplina", description = "Busca uma oferta pelo identificador.")
  public ResponseEntity<OfertaDisciplinaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(oferta -> {
          log.info("Oferta encontrada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(oferta));
        })
        .orElseGet(() -> {
          log.warn("Oferta nao encontrada id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar oferta de disciplina", description = "Atualiza os dados de uma oferta de disciplina.")
  public ResponseEntity<OfertaDisciplinaResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody OfertaDisciplinaRequest request) {
    return service.atualizar(id, request)
        .map(oferta -> {
          log.info("Oferta atualizada id={} disciplinaId={} professorId={}",
              id, request.disciplinaId(), request.professorId());
          return ResponseEntity.ok(mapper.toResponse(oferta));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar oferta id={} disciplinaId={} professorId={}",
              id, request.disciplinaId(), request.professorId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover oferta de disciplina", description = "Remove uma oferta pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover oferta id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Oferta removida id={}", id);
    return ResponseEntity.noContent().build();
  }
}
