package br.com.tcc.posgraduacao.api.controller;

import br.com.tcc.posgraduacao.api.dto.AlunoPosGraduacaoCreateRequest;
import br.com.tcc.posgraduacao.api.dto.AlunoPosGraduacaoRequest;
import br.com.tcc.posgraduacao.api.dto.AlunoPosGraduacaoResponse;
import br.com.tcc.posgraduacao.api.mapper.AlunoPosGraduacaoMapper;
import br.com.tcc.posgraduacao.domain.model.Pessoa;
import br.com.tcc.posgraduacao.domain.service.AlunoPosGraduacaoService;
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
@RequestMapping("/alunos")
@Tag(name = "02 - Alunos", description = "Operacoes de cadastro e manutencao de alunos de pos-graduacao")
public class AlunoPosGraduacaoController {

  private static final Logger log = LoggerFactory.getLogger(AlunoPosGraduacaoController.class);

  private final AlunoPosGraduacaoService service;
  private final AlunoPosGraduacaoMapper mapper;

  public AlunoPosGraduacaoController(AlunoPosGraduacaoService service, AlunoPosGraduacaoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar aluno", description = "Cria um novo aluno de pos-graduacao associado a pessoa e programa.")
  public ResponseEntity<AlunoPosGraduacaoResponse> criar(@Valid @RequestBody AlunoPosGraduacaoCreateRequest request, UriComponentsBuilder uriBuilder) {
    Pessoa novaPessoa = request.novaPessoa() != null ? request.novaPessoa().toEntity() : null;
    return service.criar(
            request.pessoaId(),
            novaPessoa,
            request.programaId(),
            request.orientadorId(),
            request.dataMatricula(),
            request.status())
        .map(aluno -> {
          URI location = uriBuilder.path("/alunos/{id}").buildAndExpand(aluno.getId()).toUri();
          log.info("Aluno criado id={} pessoaId={} programaId={} dataMatricula={} status={}",
              aluno.getId(), aluno.getPessoaId(), request.programaId(), request.dataMatricula(), request.status());
          return ResponseEntity.created(location).body(mapper.toResponse(aluno));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar aluno: pessoa, programa ou orientador nao encontrados pessoaId={} programaId={} orientadorId={}",
              request.pessoaId(), request.programaId(), request.orientadorId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar alunos", description = "Retorna a lista de alunos cadastrados.")
  public List<AlunoPosGraduacaoResponse> listar() {
    var alunos = service.listar();
    log.info("Listando alunos total={}", alunos.size());
    return alunos.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar aluno", description = "Busca um aluno pelo identificador.")
  public ResponseEntity<AlunoPosGraduacaoResponse> buscar(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(aluno -> {
          Long programaId = aluno.getPrograma() != null ? aluno.getPrograma().getId() : null;
          log.info("Aluno encontrado id={} pessoaId={} programaId={}", id, aluno.getPessoaId(), programaId);
          return ResponseEntity.ok(mapper.toResponse(aluno));
        })
        .orElseGet(() -> {
          log.warn("Aluno nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar aluno", description = "Atualiza os dados de um aluno existente.")
  public ResponseEntity<AlunoPosGraduacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AlunoPosGraduacaoRequest request) {
    return service.atualizar(
            id,
            request.pessoaId(),
            request.programaId(),
            request.orientadorId(),
            request.dataMatricula(),
            request.status())
        .map(aluno -> {
          log.info("Aluno atualizado id={} pessoaId={} programaId={} dataMatricula={} status={}",
              id, request.pessoaId(), request.programaId(), request.dataMatricula(), request.status());
          return ResponseEntity.ok(mapper.toResponse(aluno));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar aluno: pessoa, programa, orientador ou aluno nao encontrado id={} pessoaId={} programaId={}",
              id, request.pessoaId(), request.programaId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover aluno", description = "Remove um aluno pelo identificador.")
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
