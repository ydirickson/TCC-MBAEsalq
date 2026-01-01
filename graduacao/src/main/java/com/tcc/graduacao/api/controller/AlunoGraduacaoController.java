package com.tcc.graduacao.api.controller;

import com.tcc.graduacao.api.dto.AlunoGraduacaoRequest;
import com.tcc.graduacao.api.dto.AlunoGraduacaoResponse;
import com.tcc.graduacao.api.mapper.AlunoGraduacaoMapper;
import com.tcc.graduacao.domain.service.AlunoGraduacaoService;
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
@RequestMapping("/alunos")
public class AlunoGraduacaoController {

  private static final Logger log = LoggerFactory.getLogger(AlunoGraduacaoController.class);

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
          log.info("Aluno criado id={} pessoaId={} cursoId={} dataIngresso={} status={}", aluno.getId(), request.pessoaId(), request.cursoId(), request.dataIngresso(), request.status());
          return ResponseEntity.created(location).body(mapper.toResponse(aluno));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar aluno: curso nao encontrado cursoId={} pessoaId={}", request.cursoId(), request.pessoaId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  public List<AlunoGraduacaoResponse> listar() {
    var alunos = service.listar();
    log.info("Listando alunos total={}", alunos.size());
    return alunos.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<AlunoGraduacaoResponse> buscar(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(aluno -> {
          log.info("Aluno encontrado id={} pessoaId={} cursoId={}", id, aluno.getPessoaId(), aluno.getCurso().getId());
          return ResponseEntity.ok(mapper.toResponse(aluno));
        })
        .orElseGet(() -> {
          log.warn("Aluno nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  public ResponseEntity<AlunoGraduacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AlunoGraduacaoRequest request) {
    return service.atualizar(id, request.pessoaId(), request.cursoId(), request.dataIngresso(), request.status())
        .map(aluno -> {
          log.info("Aluno atualizado id={} pessoaId={} cursoId={} dataIngresso={} status={}", id, request.pessoaId(), request.cursoId(), request.dataIngresso(), request.status());
          return ResponseEntity.ok(mapper.toResponse(aluno));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar aluno: nao encontrado id={} ou cursoId={}", id, request.cursoId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover aluno: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Aluno removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
