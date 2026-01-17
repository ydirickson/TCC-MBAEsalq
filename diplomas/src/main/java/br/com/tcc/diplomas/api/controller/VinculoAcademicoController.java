package br.com.tcc.diplomas.api.controller;

import br.com.tcc.diplomas.api.dto.VinculoAcademicoRequest;
import br.com.tcc.diplomas.api.dto.VinculoAcademicoResponse;
import br.com.tcc.diplomas.api.mapper.VinculoAcademicoMapper;
import br.com.tcc.diplomas.domain.service.VinculoAcademicoService;
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
@RequestMapping("/vinculos")
@Tag(name = "02 - Vinculos academicos", description = "Operacoes de cadastro e manutencao de vinculos academicos")
public class VinculoAcademicoController {

  private static final Logger log = LoggerFactory.getLogger(VinculoAcademicoController.class);

  private final VinculoAcademicoService service;
  private final VinculoAcademicoMapper mapper;

  public VinculoAcademicoController(VinculoAcademicoService service, VinculoAcademicoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar vinculo", description = "Cria um vinculo academico para uma pessoa.")
  public ResponseEntity<VinculoAcademicoResponse> criar(@Valid @RequestBody VinculoAcademicoRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar vinculo: pessoa nao encontrada id={}", request.pessoaId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Vinculo criado id={} pessoaId={}", salvo.getId(), request.pessoaId());
    URI location = uriBuilder.path("/vinculos/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar vinculos", description = "Retorna a lista de vinculos academicos cadastrados.")
  public List<VinculoAcademicoResponse> listar() {
    var vinculos = service.listar();
    log.info("Listando vinculos total={}", vinculos.size());
    return vinculos.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar vinculo", description = "Busca um vinculo academico pelo identificador.")
  public ResponseEntity<VinculoAcademicoResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(vinculo -> {
          log.info("Vinculo encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(vinculo));
        })
        .orElseGet(() -> {
          log.warn("Vinculo nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar vinculo", description = "Atualiza um vinculo academico existente.")
  public ResponseEntity<VinculoAcademicoResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody VinculoAcademicoRequest request) {
    return service.atualizar(id, request)
        .map(vinculo -> {
          log.info("Vinculo atualizado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(vinculo));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar vinculo id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover vinculo", description = "Remove um vinculo academico pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover vinculo: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Vinculo removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
