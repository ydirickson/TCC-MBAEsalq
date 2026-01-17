package br.com.tcc.assinatura.api.controller;

import br.com.tcc.assinatura.api.dto.UsuarioAssinanteRequest;
import br.com.tcc.assinatura.api.dto.UsuarioAssinanteResponse;
import br.com.tcc.assinatura.api.mapper.UsuarioAssinanteMapper;
import br.com.tcc.assinatura.domain.service.UsuarioAssinanteService;
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
@RequestMapping("/usuarios-assinantes")
@Tag(name = "03 - Usuarios assinantes", description = "Operacoes de cadastro de usuarios assinantes")
public class UsuarioAssinanteController {

  private static final Logger log = LoggerFactory.getLogger(UsuarioAssinanteController.class);

  private final UsuarioAssinanteService service;
  private final UsuarioAssinanteMapper mapper;

  public UsuarioAssinanteController(UsuarioAssinanteService service, UsuarioAssinanteMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar usuario", description = "Cria um usuario assinante vinculado a uma pessoa.")
  public ResponseEntity<UsuarioAssinanteResponse> criar(@Valid @RequestBody UsuarioAssinanteRequest request,
      UriComponentsBuilder uriBuilder) {
    var salvoOpt = service.criar(request);
    if (salvoOpt.isEmpty()) {
      log.warn("Falha ao criar usuario: pessoa nao encontrada id={}", request.pessoaId());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    var salvo = salvoOpt.get();
    log.info("Usuario assinante criado id={} pessoaId={}", salvo.getId(), request.pessoaId());
    URI location = uriBuilder.path("/usuarios-assinantes/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  @Operation(summary = "Listar usuarios", description = "Retorna a lista de usuarios assinantes cadastrados.")
  public List<UsuarioAssinanteResponse> listar() {
    var usuarios = service.listar();
    log.info("Listando usuarios assinantes total={}", usuarios.size());
    return usuarios.stream().map(mapper::toResponse).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar usuario", description = "Busca um usuario assinante pelo identificador.")
  public ResponseEntity<UsuarioAssinanteResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(usuario -> {
          log.info("Usuario assinante encontrado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(usuario));
        })
        .orElseGet(() -> {
          log.warn("Usuario assinante nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar usuario", description = "Atualiza um usuario assinante existente.")
  public ResponseEntity<UsuarioAssinanteResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody UsuarioAssinanteRequest request) {
    return service.atualizar(id, request)
        .map(usuario -> {
          log.info("Usuario assinante atualizado id={}", id);
          return ResponseEntity.ok(mapper.toResponse(usuario));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar usuario assinante id={}", id);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover usuario", description = "Remove um usuario assinante pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover usuario assinante: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Usuario assinante removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
