package br.com.tcc.diplomas.api.controller;

import br.com.tcc.diplomas.api.dto.BaseEmissaoDiplomaRequest;
import br.com.tcc.diplomas.api.dto.BaseEmissaoDiplomaResponse;
import br.com.tcc.diplomas.api.mapper.BaseEmissaoDiplomaMapper;
import br.com.tcc.diplomas.domain.service.BaseEmissaoDiplomaService;
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
@RequestMapping("/bases-emissao")
@Tag(name = "04 - Base de emissao", description = "Operacoes da base de emissao de diploma")
public class BaseEmissaoDiplomaController {

  private static final Logger log = LoggerFactory.getLogger(BaseEmissaoDiplomaController.class);

  private final BaseEmissaoDiplomaService service;
  private final BaseEmissaoDiplomaMapper mapper;

  public BaseEmissaoDiplomaController(BaseEmissaoDiplomaService service, BaseEmissaoDiplomaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar base", description = "Cria manualmente uma base de emissao de diploma.")
  public ResponseEntity<BaseEmissaoDiplomaResponse> criar(@Valid @RequestBody BaseEmissaoDiplomaRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar base: requerimento nao encontrado id={}", request.requerimentoId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Base de emissao criada id={} requerimentoId={}", salvo.getId(), request.requerimentoId());
    URI location = uriBuilder.path("/bases-emissao/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar bases", description = "Retorna a lista de bases de emissao cadastradas.")
  public List<BaseEmissaoDiplomaResponse> listar() {
    var bases = service.listar();
    log.info("Listando bases de emissao total={}", bases.size());
    return bases.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar base", description = "Busca uma base de emissao pelo identificador.")
  public ResponseEntity<BaseEmissaoDiplomaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(base -> {
          log.info("Base de emissao encontrada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(base));
        })
        .orElseGet(() -> {
          log.warn("Base de emissao nao encontrada id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar base", description = "Atualiza uma base de emissao existente.")
  public ResponseEntity<BaseEmissaoDiplomaResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody BaseEmissaoDiplomaRequest request) {
    return service.atualizar(id, request)
        .map(base -> {
          log.info("Base de emissao atualizada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(base));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar base de emissao id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover base", description = "Remove uma base de emissao pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover base: nao encontrada id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Base de emissao removida id={}", id);
    return ResponseEntity.noContent().build();
  }
}
