package br.com.tcc.graduacao.config;

import br.com.tcc.graduacao.domain.model.CursoGraduacao;
import br.com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CursoGraduacaoSeedLoader {

  private final CursoGraduacaoRepository cursoGraduacaoRepository;
  private final ObjectMapper objectMapper;

  public CursoGraduacaoSeedLoader(
      CursoGraduacaoRepository cursoGraduacaoRepository, ObjectMapper objectMapper) {
    this.cursoGraduacaoRepository = cursoGraduacaoRepository;
    this.objectMapper = objectMapper;
  }

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void seed() throws IOException {
    ClassPathResource resource = new ClassPathResource("fixtures/curso-graduacao.json");
    try (InputStream inputStream = resource.getInputStream()) {
      List<CursoGraduacaoSeed> seeds =
          objectMapper.readValue(inputStream, new TypeReference<List<CursoGraduacaoSeed>>() {});

      List<CursoGraduacao> cursos =
          seeds.stream()
              .map(seed -> new CursoGraduacao(seed.codigo(), seed.nome(), seed.cargaHoraria()))
              .toList();

      cursoGraduacaoRepository.saveAll(cursos);
    }
  }

  private record CursoGraduacaoSeed(String codigo, String nome, Integer cargaHoraria) {}
}
