package com.tcc.graduacao.api.controller;

import com.tcc.graduacao.api.dto.AlunoGraduacaoRequest;
import com.tcc.graduacao.api.dto.AlunoGraduacaoResponse;
import com.tcc.graduacao.api.mapper.AlunoGraduacaoMapper;
import com.tcc.graduacao.domain.service.AlunoGraduacaoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
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
@RequestMapping("/alunos")
public class AlunoGraduacaoController {

  private final AlunoGraduacaoService service;
  private final AlunoGraduacaoMapper mapper;

  public AlunoGraduacaoController(AlunoGraduacaoService service, AlunoGraduacaoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  public ResponseEntity<AlunoGraduacaoResponse> criar(@Valid @RequestBody AlunoGraduacaoRequest request, UriComponentsBuilder uriBuilder) {
    return service.criar(request.pessoaId(), request.cursoId(), request.dataIngresso(), request.status())
        .map(aluno -> {
          URI location = uriBuilder.path("/alunos/{id}").buildAndExpand(aluno.getId()).toUri();
          return ResponseEntity.created(location).body(mapper.toResponse(aluno));
        })
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping
  public List<AlunoGraduacaoResponse> listar() {
    return service.listar().stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<AlunoGraduacaoResponse> buscar(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(aluno -> ResponseEntity.ok(mapper.toResponse(aluno)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<AlunoGraduacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AlunoGraduacaoRequest request) {
    return service.atualizar(id, request.pessoaId(), request.cursoId(), request.dataIngresso(), request.status())
        .map(aluno -> ResponseEntity.ok(mapper.toResponse(aluno)))
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
