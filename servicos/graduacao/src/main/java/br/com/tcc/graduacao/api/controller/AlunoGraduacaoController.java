package br.com.tcc.graduacao.api.controller;

import br.com.tcc.graduacao.api.dto.AlunoGraduacaoCreateRequest;
import br.com.tcc.graduacao.api.dto.AlunoGraduacaoRequest;
import br.com.tcc.graduacao.api.dto.AlunoGraduacaoResponse;
import br.com.tcc.graduacao.api.mapper.AlunoGraduacaoMapper;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.service.AlunoGraduacaoService;
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
@Tag(name = "02 - Alunos", description = "Operacoes de cadastro e manutenção de alunos de graduação")
public class AlunoGraduacaoController {

  private static final Logger log = LoggerFactory.getLogger(AlunoGraduacaoController.class);

  private final AlunoGraduacaoService service;
  private final AlunoGraduacaoMapper mapper;

  public AlunoGraduacaoController(AlunoGraduacaoService service, AlunoGraduacaoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar aluno", description = "Cria um novo aluno de graduação associado a pessoa e turma.")
  public ResponseEntity<AlunoGraduacaoResponse> criar(@Valid @RequestBody AlunoGraduacaoCreateRequest request, UriComponentsBuilder uriBuilder) {
    Pessoa novaPessoa = request.novaPessoa() != null ? request.novaPessoa().toEntity() : null;
    return service.criar(
            request.pessoaId(),
            novaPessoa,
            request.turmaId(),
            request.dataMatricula(),
            request.dataConclusao(),
            request.status())
        .map(aluno -> {
          URI location = uriBuilder.path("/alunos/{id}").buildAndExpand(aluno.getId()).toUri();
          log.info("Aluno criado id={} pessoaId={} turmaId={} dataMatricula={} dataConclusao={} status={}",
              aluno.getId(), aluno.getPessoaId(), request.turmaId(), request.dataMatricula(),
              request.dataConclusao(), request.status());
          return ResponseEntity.created(location).body(mapper.toResponse(aluno));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar aluno: pessoa ou turma nao encontrados pessoaId={} turmaId={}",
              request.pessoaId(), request.turmaId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar alunos", description = "Retorna a lista de alunos cadastrados.")
  public List<AlunoGraduacaoResponse> listar() {
    var alunos = service.listar();
    log.info("Listando alunos total={}", alunos.size());
    return alunos.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar aluno", description = "Busca um aluno pelo identificador.")
  public ResponseEntity<AlunoGraduacaoResponse> buscar(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(aluno -> {
          Long cursoId = aluno.getCurso() != null ? aluno.getCurso().getId() : null;
          log.info("Aluno encontrado id={} pessoaId={} cursoId={}", id, aluno.getPessoaId(), cursoId);
          return ResponseEntity.ok(mapper.toResponse(aluno));
        })
        .orElseGet(() -> {
          log.warn("Aluno nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar aluno", description = "Atualiza os dados de um aluno existente.")
  public ResponseEntity<AlunoGraduacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody AlunoGraduacaoRequest request) {
    return service.atualizar(
            id,
            request.pessoaId(),
            request.turmaId(),
            request.dataMatricula(),
            request.dataConclusao(),
            request.status())
        .map(aluno -> {
          log.info("Aluno atualizado id={} pessoaId={} turmaId={} dataMatricula={} dataConclusao={} status={}",
              id, request.pessoaId(), request.turmaId(), request.dataMatricula(),
              request.dataConclusao(), request.status());
          return ResponseEntity.ok(mapper.toResponse(aluno));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar aluno: pessoa, turma ou aluno nao encontrado id={} pessoaId={} turmaId={}",
              id, request.pessoaId(), request.turmaId());
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
