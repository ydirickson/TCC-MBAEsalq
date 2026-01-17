package br.com.tcc.assinatura.api.controller;

import br.com.tcc.assinatura.api.dto.ManifestoAssinaturaRequest;
import br.com.tcc.assinatura.api.dto.ManifestoAssinaturaResponse;
import br.com.tcc.assinatura.api.mapper.ManifestoAssinaturaMapper;
import br.com.tcc.assinatura.domain.service.ManifestoAssinaturaService;
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
@RequestMapping("/manifestos-assinatura")
@Tag(name = "07 - Manifestos de assinatura", description = "Operacoes de manifesto de assinatura")
public class ManifestoAssinaturaController {

  private static final Logger log = LoggerFactory.getLogger(ManifestoAssinaturaController.class);

  private final ManifestoAssinaturaService service;
  private final ManifestoAssinaturaMapper mapper;

  public ManifestoAssinaturaController(ManifestoAssinaturaService service, ManifestoAssinaturaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar manifesto", description = "Cria um manifesto de assinatura para uma solicitacao.")
  public ResponseEntity<ManifestoAssinaturaResponse> criar(@Valid @RequestBody ManifestoAssinaturaRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar manifesto: solicitacao nao encontrada id={}", request.solicitacaoId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Manifesto criado id={} solicitacaoId={}", salvo.getId(), request.solicitacaoId());
    URI location = uriBuilder.path("/manifestos-assinatura/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar manifestos", description = "Retorna a lista de manifestos cadastrados.")
  public List<ManifestoAssinaturaResponse> listar() {
    var manifestos = service.listar();
    log.info("Listando manifestos total={}", manifestos.size());
    return manifestos.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar manifesto", description = "Busca um manifesto de assinatura pelo identificador.")
  public ResponseEntity<ManifestoAssinaturaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(manifesto -> {
          log.info("Manifesto encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(manifesto));
        })
        .orElseGet(() -> {
          log.warn("Manifesto nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar manifesto", description = "Atualiza um manifesto de assinatura existente.")
  public ResponseEntity<ManifestoAssinaturaResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody ManifestoAssinaturaRequest request) {
    return service.atualizar(id, request)
        .map(manifesto -> {
          log.info("Manifesto atualizado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(manifesto));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar manifesto id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover manifesto", description = "Remove um manifesto de assinatura pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover manifesto: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Manifesto removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
