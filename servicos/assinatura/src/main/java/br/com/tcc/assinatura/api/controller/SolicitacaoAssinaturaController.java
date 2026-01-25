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
@RequestMapping("/documentos-assinaveis/{documentoAssinavelId}/solicitacoes-assinatura")
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
  public ResponseEntity<SolicitacaoAssinaturaResponse> criar(@PathVariable Long documentoAssinavelId,
      @Valid @RequestBody SolicitacaoAssinaturaRequest request, UriComponentsBuilder uriBuilder) {
    if (request.documentoAssinavelId() != null
        && !request.documentoAssinavelId().equals(documentoAssinavelId)) {
      log.warn("Falha ao criar solicitacao: documentoAssinavelId divergente path={} body={}",
          documentoAssinavelId, request.documentoAssinavelId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    SolicitacaoAssinaturaRequest ajustado = new SolicitacaoAssinaturaRequest(
        documentoAssinavelId,
        request.status(),
        request.dataSolicitacao(),
        request.dataConclusao());
    var salvoOpt = service.criar(documentoAssinavelId, ajustado);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar solicitacao: documento assinavel nao encontrado id={}", documentoAssinavelId);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Solicitacao criada id={} documentoAssinavelId={}", salvo.getId(), documentoAssinavelId);
    URI location = uriBuilder.path("/documentos-assinaveis/{documentoAssinavelId}/solicitacoes-assinatura/{id}")
        .buildAndExpand(documentoAssinavelId, salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar solicitacoes", description = "Retorna a lista de solicitacoes de um documento assinavel.")
  public ResponseEntity<List<SolicitacaoAssinaturaResponse>> listar(@PathVariable Long documentoAssinavelId) {
    return service.listarPorDocumentoAssinavelId(documentoAssinavelId)
        .map(solicitacoes -> {
          log.info("Listando solicitacoes do documentoAssinavelId={} total={}",
              documentoAssinavelId, solicitacoes.size());
          var resposta = solicitacoes.stream().map(mapper::toResponse).collect(Collectors.toList());
          return ResponseEntity.ok(resposta);
        })
        .orElseGet(() -> {
          log.warn("Documento assinavel nao encontrado id={}", documentoAssinavelId);
          return ResponseEntity.notFound().build();
        });
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar solicitacao", description = "Busca uma solicitacao de assinatura pelo identificador.")
  public ResponseEntity<SolicitacaoAssinaturaResponse> buscarPorId(@PathVariable Long documentoAssinavelId,
      @PathVariable Long id) {
    return service.buscarPorId(documentoAssinavelId, id)
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
  public ResponseEntity<SolicitacaoAssinaturaResponse> atualizar(@PathVariable Long documentoAssinavelId,
      @PathVariable Long id, @Valid @RequestBody SolicitacaoAssinaturaRequest request) {
    if (request.documentoAssinavelId() != null
        && !request.documentoAssinavelId().equals(documentoAssinavelId)) {
      log.warn("Falha ao atualizar solicitacao: documentoAssinavelId divergente path={} body={}",
          documentoAssinavelId, request.documentoAssinavelId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    SolicitacaoAssinaturaRequest ajustado = new SolicitacaoAssinaturaRequest(
        documentoAssinavelId,
        request.status(),
        request.dataSolicitacao(),
        request.dataConclusao());
    return service.atualizar(documentoAssinavelId, id, ajustado)
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
  @Operation(summary = "Cancelar solicitacao", description = "Cancela uma solicitacao de assinatura pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long documentoAssinavelId, @PathVariable Long id) {
    boolean removido = service.remover(documentoAssinavelId, id);
    if (!removido) {
      log.warn("Falha ao cancelar solicitacao: nao encontrada id={} documentoAssinavelId={}", id, documentoAssinavelId);
      return ResponseEntity.notFound().build();
    }
    log.info("Solicitacao cancelada id={}", id);
    return ResponseEntity.noContent().build();
  }
}
