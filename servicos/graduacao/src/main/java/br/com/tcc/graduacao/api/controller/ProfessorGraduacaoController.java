package br.com.tcc.graduacao.api.controller;

import br.com.tcc.graduacao.api.dto.ProfessorGraduacaoCreateRequest;
import br.com.tcc.graduacao.api.dto.ProfessorGraduacaoRequest;
import br.com.tcc.graduacao.api.dto.ProfessorGraduacaoResponse;
import br.com.tcc.graduacao.api.mapper.ProfessorGraduacaoMapper;
import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.service.ProfessorGraduacaoService;
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
@RequestMapping("/professores")
@Tag(name = "03 - Professores", description = "Operacoes de cadastro e manutencao de professores de graduacao")
public class ProfessorGraduacaoController {

  private static final Logger log = LoggerFactory.getLogger(ProfessorGraduacaoController.class);

  private final ProfessorGraduacaoService service;
  private final ProfessorGraduacaoMapper mapper;

  public ProfessorGraduacaoController(ProfessorGraduacaoService service, ProfessorGraduacaoMapper mapper) {
    this.service = service;
    this.mapper = mapper;
  }

  @PostMapping
  @Operation(summary = "Criar professor", description = "Cria um novo professor de graduacao associado a pessoa e curso.")
  public ResponseEntity<ProfessorGraduacaoResponse> criar(@Valid @RequestBody ProfessorGraduacaoCreateRequest request,
      UriComponentsBuilder uriBuilder) {
    Pessoa novaPessoa = request.novaPessoa() != null ? request.novaPessoa().toEntity() : null;
    return service.criar(
            request.pessoaId(),
            novaPessoa,
            request.cursoId(),
            request.dataIngresso(),
            request.nivelDocente(),
            request.status())
        .map(professor -> {
          URI location = uriBuilder.path("/professores/{id}").buildAndExpand(professor.getId()).toUri();
          log.info("Professor criado id={} pessoaId={} cursoId={} dataIngresso={} nivelDocente={} status={}",
              professor.getId(), professor.getPessoaId(), request.cursoId(), request.dataIngresso(),
              request.nivelDocente(), request.status());
          return ResponseEntity.created(location).body(mapper.toResponse(professor));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar professor: pessoa ou curso nao encontrados pessoaId={} cursoId={}",
              request.pessoaId(), request.cursoId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar professores", description = "Retorna a lista de professores cadastrados.")
  public List<ProfessorGraduacaoResponse> listar() {
    var professores = service.listar();
    log.info("Listando professores total={}", professores.size());
    return professores.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar professor", description = "Busca um professor pelo identificador.")
  public ResponseEntity<ProfessorGraduacaoResponse> buscar(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(professor -> {
          Long cursoId = professor.getCurso() != null ? professor.getCurso().getId() : null;
          log.info("Professor encontrado id={} pessoaId={} cursoId={}", id, professor.getPessoaId(), cursoId);
          return ResponseEntity.ok(mapper.toResponse(professor));
        })
        .orElseGet(() -> {
          log.warn("Professor nao encontrado id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar professor", description = "Atualiza os dados de um professor existente.")
  public ResponseEntity<ProfessorGraduacaoResponse> atualizar(@PathVariable Long id,
      @Valid @RequestBody ProfessorGraduacaoRequest request) {
    return service.atualizar(
            id,
            request.pessoaId(),
            request.cursoId(),
            request.dataIngresso(),
            request.nivelDocente(),
            request.status())
        .map(professor -> {
          log.info("Professor atualizado id={} pessoaId={} cursoId={} dataIngresso={} nivelDocente={} status={}",
              id, request.pessoaId(), request.cursoId(), request.dataIngresso(), request.nivelDocente(),
              request.status());
          return ResponseEntity.ok(mapper.toResponse(professor));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar professor: pessoa, curso ou professor nao encontrado id={} pessoaId={} cursoId={}",
              id, request.pessoaId(), request.cursoId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover professor", description = "Remove um professor pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover professor: nao encontrado id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Professor removido id={}", id);
    return ResponseEntity.noContent().build();
  }
}
