package br.com.tcc.diplomas.api.controller;

import br.com.tcc.diplomas.api.dto.StatusEmissaoRequest;
import br.com.tcc.diplomas.api.dto.StatusEmissaoResponse;
import br.com.tcc.diplomas.api.mapper.StatusEmissaoMapper;
import br.com.tcc.diplomas.domain.service.StatusEmissaoService;
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
@RequestMapping("/status-emissao")
@Tag(name = "07 - Status de emissao", description = "Operacoes do status de emissao do diploma")
public class StatusEmissaoController {

  private static final Logger log = LoggerFactory.getLogger(StatusEmissaoController.class);

  private final StatusEmissaoService service;
  private final StatusEmissaoMapper mapper;

  public StatusEmissaoController(StatusEmissaoService service, StatusEmissaoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar status", description = "Cria manualmente um status de emissao.")
  public ResponseEntity<StatusEmissaoResponse> criar(@Valid @RequestBody StatusEmissaoRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar status: requerimento nao encontrado id={}", request.requerimentoId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Status criado id={} requerimentoId={}", salvo.getId(), request.requerimentoId());
    URI location = uriBuilder.path("/status-emissao/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar status", description = "Retorna a lista de status de emissao cadastrados.")
  public List<StatusEmissaoResponse> listar() {
    var statusList = service.listar();
    log.info("Listando status de emissao total={}", statusList.size());
    return statusList.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar status", description = "Busca um status de emissao pelo identificador.")
  public ResponseEntity<StatusEmissaoResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(status -> {
          log.info("Status encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(status));
        })
        .orElseGet(() -> {
          log.warn("Status nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar status", description = "Atualiza um status de emissao existente.")
  public ResponseEntity<StatusEmissaoResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody StatusEmissaoRequest request) {
    return service.atualizar(id, request)
        .map(status -> {
          log.info("Status atualizado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(status));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar status id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover status", description = "Remove um status de emissao pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover status: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Status removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
