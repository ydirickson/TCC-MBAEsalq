package br.com.tcc.graduacao.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.model.DisciplinaGraduacao;
import br.com.tcc.graduacao.domain.model.TurmaGraduacao;
import br.com.tcc.graduacao.domain.model.TurmaGraduacao.StatusTurma;
import br.com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import br.com.tcc.graduacao.domain.repository.DisciplinaRepository;
import br.com.tcc.graduacao.domain.repository.TurmaGraduacaoRepository;

@Component
public class JsonSeedLoader {

  private static final Logger logger = LoggerFactory.getLogger(JsonSeedLoader.class);

  private final ObjectMapper objectMapper;

  private final CursoGraduacaoRepository cursoGraduacaoRepository;
  private final DisciplinaRepository disciplinaRepository;
  private final TurmaGraduacaoRepository turmaGraduacaoRepository;

  public JsonSeedLoader(
    ObjectMapper objectMapper,
    CursoGraduacaoRepository cursoGraduacaoRepository,
    DisciplinaRepository disciplinaRepository,
    TurmaGraduacaoRepository turmaGraduacaoRepository
  ) {
    this.objectMapper = objectMapper;
    this.cursoGraduacaoRepository = cursoGraduacaoRepository;
    this.disciplinaRepository = disciplinaRepository;
    this.turmaGraduacaoRepository = turmaGraduacaoRepository;
  }

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void seed() throws IOException {
    try {
      Map<String, CursoGraduacao> cursos = this.carregarCursos();
      this.carregarDisciplinas(cursos);
      this.carregarTurmas(cursos);
    } catch (IOException e) {
      logger.error("Failed to load seed data: {}", e.getMessage(), e);
      throw e;
    }
  }

  private Map<String, CursoGraduacao> carregarCursos()
      throws StreamReadException, DatabindException, IOException {
    ClassPathResource resource = new ClassPathResource("fixtures/curso-graduacao.json");
    logger.info("Carregando dados de cursos");
    try (InputStream inputStream = resource.getInputStream()) {
      List<CursoGraduacao> seeds = objectMapper.readValue(
          inputStream, new TypeReference<List<CursoGraduacao>>() {});
      List<CursoGraduacao> salvos = this.cursoGraduacaoRepository.saveAll(seeds);
      logger.info("Salvos {} cursos no banco de dados", salvos.size());
      return salvos.stream()
          .collect(Collectors.toMap(CursoGraduacao::getCodigo, Function.identity()));
    } 
  }

  private void carregarDisciplinas(Map<String, CursoGraduacao> cursos)
      throws StreamReadException, DatabindException, IOException {
    ClassPathResource resource = new ClassPathResource("fixtures/disciplinas-graduacao.json");
    logger.info("Carregando dados de disciplinas");
    try (InputStream inputStream = resource.getInputStream()) {
      List<DisciplinaSeed> seeds = objectMapper.readValue(
          inputStream, new TypeReference<List<DisciplinaSeed>>() {});
      List<DisciplinaGraduacao> disciplinas = seeds.stream()
          .map(seed -> {
            CursoGraduacao curso = cursos.get(seed.cursoCodigo());
            if (curso == null) {
              logger.warn("Curso nao encontrado para disciplina codigo={} cursoCodigo={}",
                  seed.codigo(), seed.cursoCodigo());
              return null;
            }
            return new DisciplinaGraduacao(curso, seed.codigo(), seed.nome(), seed.cargaHoraria());
          })
          .filter(disciplina -> disciplina != null)
          .toList();
      this.disciplinaRepository.saveAll(disciplinas);
      logger.info("Salvas {} disciplinas no banco de dados", disciplinas.size());
    }
  }

  private void carregarTurmas(Map<String, CursoGraduacao> cursos) {
    logger.info("Carregando turmas de graduacao");
    List<TurmaGraduacao> turmas = cursos.values().stream()
        .flatMap(curso -> {
          if (curso.getCodigo() == null || curso.getCodigo().isBlank()) {
            logger.warn("Curso sem codigo, ignorando turmas cursoId={}", curso.getId());
            return java.util.stream.Stream.empty();
          }
          return java.util.stream.IntStream.rangeClosed(2024, 2026)
              .mapToObj(ano -> criarTurma(curso, ano, 1));
        })
        .toList();
    this.turmaGraduacaoRepository.saveAll(turmas);
    logger.info("Salvas {} turmas no banco de dados", turmas.size());
  }

  private TurmaGraduacao criarTurma(CursoGraduacao curso, int ano, int semestre) {
    TurmaGraduacao turma = new TurmaGraduacao();
    turma.setCurso(curso);
    turma.setAno(ano);
    turma.setSemestre(semestre);
    turma.setStatus(StatusTurma.ATIVA);
    String codigoTurma = String.format("%d%02d%s", ano, semestre, curso.getCodigo());
    turma.setId(codigoTurma);
    return turma;
  }

  private record DisciplinaSeed(
      String cursoCodigo,
      String codigo,
      String nome,
      Integer cargaHoraria) {}
}
