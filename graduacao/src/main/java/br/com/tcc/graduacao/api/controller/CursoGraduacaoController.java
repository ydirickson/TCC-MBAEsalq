package br.com.tcc.graduacao.api.controller;

import br.com.tcc.graduacao.api.dto.CursoGraduacaoRequest;
import br.com.tcc.graduacao.api.dto.CursoGraduacaoResponse;
import br.com.tcc.graduacao.api.mapper.CursoGraduacaoMapper;
import br.com.tcc.graduacao.domain.service.CursoGraduacaoService;
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
@RequestMapping("/cursos")
@Tag(name = "01 - Cursos", description = "Operações de cadastro e manutenção de cursos de graduação")
public class CursoGraduacaoController {

  private static final Logger log = LoggerFactory.getLogger(CursoGraduacaoController.class);

  private final CursoGraduacaoService service;
  private final CursoGraduacaoMapper mapper;

  public CursoGraduacaoController(CursoGraduacaoService service, CursoGraduacaoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar curso", description = "Cria um novo curso de graduação.")
  public ResponseEntity<CursoGraduacaoResponse> criar(@Valid @RequestBody CursoGraduacaoRequest request, UriComponentsBuilder uriBuilder) {
    var salvo = service.criarCurso(request);
    log.info("Curso criado id={} codigo={} nome={}", salvo.getId(), request.codigo(), request.nome());
    URI location = uriBuilder.path("/cursos/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar cursos", description = "Retorna a lista de cursos cadastrados.")
  public List<CursoGraduacaoResponse> listar() {
    var cursos = service.listar();
    log.info("Listando cursos total={}", cursos.size());
    return cursos.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar curso", description = "Busca um curso pelo identificador.")
  public ResponseEntity<CursoGraduacaoResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(curso -> {
          log.info("Curso encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(curso));
        })
        .orElseGet(() -> {
          log.warn("Curso nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar curso", description = "Atualiza os dados de um curso existente.")
  public ResponseEntity<CursoGraduacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody CursoGraduacaoRequest request) {
    return service.atualizar(id, request)
        .map(curso -> {
          log.info("Curso atualizado id={} novoCodigo={} novoNome={}", id, request.codigo(), request.nome());
          return ResponseEntity.ok(mapper.toResponse(curso));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar curso: nao encontrado id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover curso", description = "Remove um curso pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover curso: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Curso removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
