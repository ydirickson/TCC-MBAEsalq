package br.com.tcc.posgraduacao.api.controller;

import br.com.tcc.posgraduacao.api.dto.ProgramaPosRequest;
import br.com.tcc.posgraduacao.api.dto.ProgramaPosResponse;
import br.com.tcc.posgraduacao.api.mapper.ProgramaPosMapper;
import br.com.tcc.posgraduacao.domain.service.ProgramaPosService;
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
@RequestMapping("/programas")
@Tag(name = "01 - Programas", description = "Operacoes de cadastro e manutencao de programas de pos-graduacao")
public class ProgramaPosController {

  private static final Logger log = LoggerFactory.getLogger(ProgramaPosController.class);

  private final ProgramaPosService service;
  private final ProgramaPosMapper mapper;

  public ProgramaPosController(ProgramaPosService service, ProgramaPosMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar programa", description = "Cria um novo programa de pos-graduacao.")
  public ResponseEntity<ProgramaPosResponse> criar(@Valid @RequestBody ProgramaPosRequest request, UriComponentsBuilder uriBuilder) {
    var salvo = service.criarPrograma(request);
    log.info("Programa criado id={} codigo={} nome={}", salvo.getId(), request.codigo(), request.nome());
    URI location = uriBuilder.path("/programas/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar programas", description = "Retorna a lista de programas cadastrados.")
  public List<ProgramaPosResponse> listar() {
    var programas = service.listar();
    log.info("Listando programas total={}", programas.size());
    return programas.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar programa", description = "Busca um programa pelo identificador.")
  public ResponseEntity<ProgramaPosResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(programa -> {
          log.info("Programa encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(programa));
        })
        .orElseGet(() -> {
          log.warn("Programa nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar programa", description = "Atualiza os dados de um programa existente.")
  public ResponseEntity<ProgramaPosResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ProgramaPosRequest request) {
    return service.atualizar(id, request)
        .map(programa -> {
          log.info("Programa atualizado id={} novoCodigo={} novoNome={}", id, request.codigo(), request.nome());
          return ResponseEntity.ok(mapper.toResponse(programa));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar programa: nao encontrado id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover programa", description = "Remove um programa pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover programa: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Programa removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
