package br.com.tcc.posgraduacao.api.controller;

import br.com.tcc.posgraduacao.api.dto.DefesaMembroRequest;
import br.com.tcc.posgraduacao.api.dto.DefesaMembroResponse;
import br.com.tcc.posgraduacao.api.dto.DefesaRequest;
import br.com.tcc.posgraduacao.api.dto.DefesaResponse;
import br.com.tcc.posgraduacao.api.mapper.DefesaMapper;
import br.com.tcc.posgraduacao.api.mapper.DefesaMembroMapper;
import br.com.tcc.posgraduacao.domain.service.DefesaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/defesas")
@Tag(name = "11 - Defesas", description = "Operacoes de qualificacao e defesa final")
public class DefesaController {

  private static final Logger log = LoggerFactory.getLogger(DefesaController.class);

  private final DefesaService service;
  private final DefesaMapper mapper;
  private final DefesaMembroMapper membroMapper;

  public DefesaController(DefesaService service, DefesaMapper mapper, DefesaMembroMapper membroMapper) {
    this.service = service;
    this.mapper = mapper;
    this.membroMapper = membroMapper;
  }

  @PostMapping
  @Operation(summary = "Criar defesa", description = "Cria uma defesa (qualificacao ou defesa final) para um aluno.")
  public ResponseEntity<DefesaResponse> criar(@Valid @RequestBody DefesaRequest request, UriComponentsBuilder uriBuilder) {
    return service.criar(request)
        .map(defesa -> {
          URI location = uriBuilder.path("/defesas/{id}").buildAndExpand(defesa.getId()).toUri();
          log.info("Defesa criada id={} alunoId={} tipo={}", defesa.getId(), request.alunoId(), request.tipo());
          return ResponseEntity.created(location).body(mapper.toResponse(defesa));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar defesa alunoId={}", request.alunoId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping
  @Operation(summary = "Listar defesas", description = "Lista defesas, com filtro opcional por aluno.")
  public List<DefesaResponse> listar(@RequestParam(required = false) Long alunoId) {
    var defesas = service.listar(alunoId);
    log.info("Listando defesas total={} alunoId={}", defesas.size(), alunoId);
    return defesas.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar defesa", description = "Busca uma defesa pelo identificador.")
  public ResponseEntity<DefesaResponse> buscarPorId(@PathVariable Long id) {
    return service.buscarPorId(id)
        .map(defesa -> {
          log.info("Defesa encontrada id={}", id);
          return ResponseEntity.ok(mapper.toResponse(defesa));
        })
        .orElseGet(() -> {
          log.warn("Defesa nao encontrada id={}", id);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{id}")
  @Operation(summary = "Atualizar defesa", description = "Atualiza os dados de uma defesa.")
  public ResponseEntity<DefesaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody DefesaRequest request) {
    return service.atualizar(id, request)
        .map(defesa -> {
          log.info("Defesa atualizada id={} alunoId={} tipo={}", id, request.alunoId(), request.tipo());
          return ResponseEntity.ok(mapper.toResponse(defesa));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar defesa id={} alunoId={}", id, request.alunoId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Remover defesa", description = "Remove uma defesa pelo identificador.")
  public ResponseEntity<Void> remover(@PathVariable Long id) {
    boolean removido = service.remover(id);
    if (!removido) {
      log.warn("Falha ao remover defesa id={}", id);
      return ResponseEntity.notFound().build();
    }
    log.info("Defesa removida id={}", id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{defesaId}/membros")
  @Operation(summary = "Criar membro", description = "Adiciona um membro a banca da defesa.")
  public ResponseEntity<DefesaMembroResponse> criarMembro(
      @PathVariable Long defesaId,
      @Valid @RequestBody DefesaMembroRequest request,
      UriComponentsBuilder uriBuilder) {
    return service.criarMembro(defesaId, request)
        .map(membro -> {
          URI location = uriBuilder.path("/defesas/{defesaId}/membros/{id}")
              .buildAndExpand(defesaId, membro.getId())
              .toUri();
          log.info("Membro criado id={} defesaId={} professorId={}", membro.getId(), defesaId, request.professorId());
          return ResponseEntity.created(location).body(membroMapper.toResponse(membro));
        })
        .orElseGet(() -> {
          log.warn("Falha ao criar membro defesaId={} professorId={}", defesaId, request.professorId());
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @GetMapping("/{defesaId}/membros")
  @Operation(summary = "Listar membros", description = "Lista membros da banca de uma defesa.")
  public List<DefesaMembroResponse> listarMembros(@PathVariable Long defesaId) {
    var membros = service.listarMembros(defesaId);
    log.info("Listando membros total={} defesaId={}", membros.size(), defesaId);
    return membros.stream()
        .map(membroMapper::toResponse)
        .collect(Collectors.toList());
  }

  @GetMapping("/{defesaId}/membros/{membroId}")
  @Operation(summary = "Buscar membro", description = "Busca um membro da banca pelo identificador.")
  public ResponseEntity<DefesaMembroResponse> buscarMembro(
      @PathVariable Long defesaId,
      @PathVariable Long membroId) {
    return service.buscarMembro(defesaId, membroId)
        .map(membro -> {
          log.info("Membro encontrado id={} defesaId={}", membroId, defesaId);
          return ResponseEntity.ok(membroMapper.toResponse(membro));
        })
        .orElseGet(() -> {
          log.warn("Membro nao encontrado id={} defesaId={}", membroId, defesaId);
          return ResponseEntity.notFound().build();
        });
  }

  @PutMapping("/{defesaId}/membros/{membroId}")
  @Operation(summary = "Atualizar membro", description = "Atualiza um membro da banca.")
  public ResponseEntity<DefesaMembroResponse> atualizarMembro(
      @PathVariable Long defesaId,
      @PathVariable Long membroId,
      @Valid @RequestBody DefesaMembroRequest request) {
    return service.atualizarMembro(defesaId, membroId, request)
        .map(membro -> {
          log.info("Membro atualizado id={} defesaId={} professorId={}", membroId, defesaId, request.professorId());
          return ResponseEntity.ok(membroMapper.toResponse(membro));
        })
        .orElseGet(() -> {
          log.warn("Falha ao atualizar membro id={} defesaId={}", membroId, defesaId);
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        });
  }

  @DeleteMapping("/{defesaId}/membros/{membroId}")
  @Operation(summary = "Remover membro", description = "Remove um membro da banca.")
  public ResponseEntity<Void> removerMembro(@PathVariable Long defesaId, @PathVariable Long membroId) {
    boolean removido = service.removerMembro(defesaId, membroId);
    if (!removido) {
      log.warn("Falha ao remover membro id={} defesaId={}", membroId, defesaId);
      return ResponseEntity.notFound().build();
    }
    log.info("Membro removido id={} defesaId={}", membroId, defesaId);
    return ResponseEntity.noContent().build();
  }
}
