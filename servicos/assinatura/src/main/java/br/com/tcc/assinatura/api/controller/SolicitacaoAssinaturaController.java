package br.com.tcc.assinatura.api.controller;

import br.com.tcc.assinatura.api.dto.SolicitacaoAssinaturaRequest;
import br.com.tcc.assinatura.api.dto.SolicitacaoAssinaturaResponse;
import br.com.tcc.assinatura.api.mapper.SolicitacaoAssinaturaMapper;
import br.com.tcc.assinatura.domain.service.SolicitacaoAssinaturaService;
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
@RequestMapping("/solicitacoes-assinatura")
@Tag(name = "05 - Solicitacoes de assinatura", description = "Operacoes de solicitacoes de assinatura")
public class SolicitacaoAssinaturaController {

  private static final Logger log = LoggerFactory.getLogger(SolicitacaoAssinaturaController.class);

  private final SolicitacaoAssinaturaService service;
  private final SolicitacaoAssinaturaMapper mapper;

  public SolicitacaoAssinaturaController(SolicitacaoAssinaturaService service, SolicitacaoAssinaturaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar solicitacao", description = "Cria uma solicitacao de assinatura para um documento assinavel.")
  public ResponseEntity<SolicitacaoAssinaturaResponse> criar(@Valid @RequestBody SolicitacaoAssinaturaRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar solicitacao: documento assinavel nao encontrado id={}", request.documentoAssinavelId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Solicitacao criada id={} documentoAssinavelId={}", salvo.getId(), request.documentoAssinavelId());
    URI location = uriBuilder.path("/solicitacoes-assinatura/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar solicitacoes", description = "Retorna a lista de solicitacoes de assinatura cadastradas.")
  public List<SolicitacaoAssinaturaResponse> listar() {
    var solicitacoes = service.listar();
    log.info("Listando solicitacoes de assinatura total={}", solicitacoes.size());
    return solicitacoes.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar solicitacao", description = "Busca uma solicitacao de assinatura pelo identificador.")
  public ResponseEntity<SolicitacaoAssinaturaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(solicitacao -> {
          log.info("Solicitacao encontrada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(solicitacao));
        })
        .orElseGet(() -> {
          log.warn("Solicitacao nao encontrada id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar solicitacao", description = "Atualiza uma solicitacao de assinatura existente.")
  public ResponseEntity<SolicitacaoAssinaturaResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody SolicitacaoAssinaturaRequest request) {
    return service.atualizar(id, request)
        .map(solicitacao -> {
          log.info("Solicitacao atualizada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(solicitacao));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar solicitacao id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover solicitacao", description = "Remove uma solicitacao de assinatura pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover solicitacao: nao encontrada id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Solicitacao removida id={}", id);
    return ResponseEntity.noContent().build();
  }
}
