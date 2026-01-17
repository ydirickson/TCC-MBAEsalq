package br.com.tcc.assinatura.api.controller;

import br.com.tcc.assinatura.api.dto.PessoaRequest;
import br.com.tcc.assinatura.api.dto.PessoaResponse;
import br.com.tcc.assinatura.api.mapper.PessoaMapper;
import br.com.tcc.assinatura.domain.service.PessoaService;
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
@RequestMapping("/pessoas")
@Tag(name = "01 - Pessoas", description = "Operacoes de cadastro e manutencao de pessoas (read model)")
public class PessoaController {

  private static final Logger log = LoggerFactory.getLogger(PessoaController.class);

  private final PessoaService service;
  private final PessoaMapper mapper;

  public PessoaController(PessoaService service, PessoaMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar pessoa", description = "Cria uma pessoa (read model) no servico de assinatura.")
  public ResponseEntity<PessoaResponse> criar(@Valid @RequestBody PessoaRequest request, UriComponentsBuilder uriBuilder) {
    var salvo = service.criar(request);
    log.info("Pessoa criada id={} nome={}", salvo.getId(), request.nome());
    URI location = uriBuilder.path("/pessoas/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar pessoas", description = "Retorna a lista de pessoas cadastradas.")
  public List<PessoaResponse> listar() {
    var pessoas = service.listar();
    log.info("Listando pessoas total={}", pessoas.size());
    return pessoas.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar pessoa", description = "Busca uma pessoa pelo identificador.")
  public ResponseEntity<PessoaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(pessoa -> {
          log.info("Pessoa encontrada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(pessoa));
        })
        .orElseGet(() -> {
          log.warn("Pessoa nao encontrada id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar pessoa", description = "Atualiza uma pessoa existente.")
  public ResponseEntity<PessoaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody PessoaRequest request) {
    return service.atualizar(id, request)
        .map(pessoa -> {
          log.info("Pessoa atualizada id={} nome={}", id, request.nome());
          return ResponseEntity.ok(mapper.toResponse(pessoa));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar pessoa: nao encontrada id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover pessoa", description = "Remove uma pessoa pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover pessoa: nao encontrada id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Pessoa removida id={}", id);
    return ResponseEntity.noContent().build();
  }
}
