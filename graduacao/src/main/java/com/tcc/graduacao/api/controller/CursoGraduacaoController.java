package com.tcc.graduacao.api.controller;

import com.tcc.graduacao.api.dto.CursoRequest;
import com.tcc.graduacao.api.dto.CursoResponse;
import com.tcc.graduacao.api.mapper.CursoMapper;
import com.tcc.graduacao.domain.service.CursoGraduacaoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CursoGraduacaoController {

  private final CursoGraduacaoService service;
  private final CursoMapper mapper;

  @PostMapping
  public ResponseEntity<CursoResponse> criar(@Valid @RequestBody CursoRequest request, UriComponentsBuilder uriBuilder) {
    var salvo = service.criarCurso(request);
    URI location = uriBuilder.path("/cursos/{id}").buildAndExpand(salvo.getId()).toUri();
    return ResponseEntity.created(location).body(mapper.toResponse(salvo));
  }

  @GetMapping
  public List<CursoResponse> listar() {
    return service.listar().stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CursoResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(curso -> ResponseEntity.ok(mapper.toResponse(curso)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<CursoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody CursoRequest request) {
    return service.atualizar(id, request)
        .map(curso -> ResponseEntity.ok(mapper.toResponse(curso)))
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }
}
