package br.com.tcc.assinatura.api.controller;

import br.com.tcc.assinatura.api.dto.AssinaturaRequest;
import br.com.tcc.assinatura.api.dto.AssinaturaResponse;
import br.com.tcc.assinatura.api.mapper.AssinaturaMapper;
import br.com.tcc.assinatura.domain.service.AssinaturaService;
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
@RequestMapping("/assinaturas")
@Tag(name = "06 - Assinaturas", description = "Operacoes de assinaturas realizadas")
public class AssinaturaController {

  private static final Logger log = LoggerFactory.getLogger(AssinaturaController.class);

  private final AssinaturaService service;
  private final AssinaturaMapper mapper;

  public AssinaturaController(AssinaturaService service, AssinaturaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar assinatura", description = "Registra uma assinatura em uma solicitacao existente.")
  public ResponseEntity<AssinaturaResponse> criar(@Valid @RequestBody AssinaturaRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar assinatura: solicitacao/usuario nao encontrados solicitacaoId={} usuarioId={}",
          request.solicitacaoId(), request.usuarioAssinanteId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Assinatura criada id={} solicitacaoId={}", salvo.getId(), request.solicitacaoId());
    URI location = uriBuilder.path("/assinaturas/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar assinaturas", description = "Retorna a lista de assinaturas cadastradas.")
  public List<AssinaturaResponse> listar() {
    var assinaturas = service.listar();
    log.info("Listando assinaturas total={}", assinaturas.size());
    return assinaturas.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar assinatura", description = "Busca uma assinatura pelo identificador.")
  public ResponseEntity<AssinaturaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(assinatura -> {
          log.info("Assinatura encontrada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(assinatura));
        })
        .orElseGet(() -> {
          log.warn("Assinatura nao encontrada id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar assinatura", description = "Atualiza uma assinatura existente.")
  public ResponseEntity<AssinaturaResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody AssinaturaRequest request) {
    return service.atualizar(id, request)
        .map(assinatura -> {
          log.info("Assinatura atualizada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(assinatura));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar assinatura id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover assinatura", description = "Remove uma assinatura pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover assinatura: nao encontrada id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Assinatura removida id={}", id);
    return ResponseEntity.noContent().build();
  }
}
