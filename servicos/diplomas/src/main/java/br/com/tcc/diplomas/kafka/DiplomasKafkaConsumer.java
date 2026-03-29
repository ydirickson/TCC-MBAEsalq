package br.com.tcc.diplomas.kafka;

import br.com.tcc.diplomas.domain.model.CursoProgramaReferencia;
import br.com.tcc.diplomas.domain.model.Pessoa;
import br.com.tcc.diplomas.domain.model.RequerimentoDiploma;
import br.com.tcc.diplomas.domain.model.SituacaoAcademica;
import br.com.tcc.diplomas.domain.model.TipoCursoPrograma;
import br.com.tcc.diplomas.domain.model.TipoVinculo;
import br.com.tcc.diplomas.domain.model.VinculoAcademico;
import br.com.tcc.diplomas.domain.repository.PessoaRepository;
import br.com.tcc.diplomas.domain.repository.RequerimentoDiplomaRepository;
import br.com.tcc.diplomas.domain.repository.VinculoAcademicoRepository;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DiplomasKafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(DiplomasKafkaConsumer.class);

  private final PessoaRepository pessoaRepository;
  private final VinculoAcademicoRepository vinculoRepository;
  private final RequerimentoDiplomaRepository requerimentoRepository;

  public DiplomasKafkaConsumer(
      PessoaRepository pessoaRepository,
      VinculoAcademicoRepository vinculoRepository,
      RequerimentoDiplomaRepository requerimentoRepository) {
    this.pessoaRepository = pessoaRepository;
    this.vinculoRepository = vinculoRepository;
    this.requerimentoRepository = requerimentoRepository;
  }

  @KafkaListener(topics = {"tcc.graduacao.pessoa", "tcc.pos_graduacao.pessoa"}, groupId = "${spring.application.name}")
  @Transactional
  public void consumirPessoa(PessoaEvent event) {
    log.debug("Replicando Pessoa em diplomas: id={}", event.id());
    Pessoa pessoa = pessoaRepository.findById(event.id()).orElseGet(Pessoa::new);
    pessoa.setId(event.id());
    pessoa.setNome(event.nome());
    pessoa.setDataNascimento(event.dataNascimento());
    pessoa.setNomeSocial(event.nomeSocial());
    pessoaRepository.save(pessoa);
  }

  @KafkaListener(topics = {"tcc.graduacao.vinculo_academico", "tcc.pos_graduacao.vinculo_academico"}, groupId = "${spring.application.name}")
  @Transactional
  public void consumirVinculoAcademico(VinculoAcademicoEvent event) {
    log.debug("Replicando VinculoAcademico em diplomas: id={}", event.id());
    Pessoa pessoa = pessoaRepository.findById(event.pessoaId())
        .orElseThrow(() -> new IllegalStateException("Pessoa nao encontrada para vinculo: pessoaId=" + event.pessoaId()));

    CursoProgramaReferencia curso = new CursoProgramaReferencia(
        event.cursoId(),
        event.cursoCodigo(),
        event.cursoNome(),
        TipoCursoPrograma.valueOf(event.cursoTipo()));

    VinculoAcademico vinculo = vinculoRepository.findById(event.id()).orElseGet(VinculoAcademico::new);
    vinculo.setId(event.id());
    vinculo.setPessoa(pessoa);
    vinculo.setCurso(curso);
    vinculo.setTipoVinculo(TipoVinculo.valueOf(event.tipoVinculo()));
    vinculo.setDataIngresso(event.dataIngresso());
    vinculo.setDataConclusao(event.dataConclusao());
    vinculo.setSituacao(SituacaoAcademica.valueOf(event.situacao()));
    vinculoRepository.save(vinculo);
  }

  @KafkaListener(topics = {"tcc.graduacao.conclusao", "tcc.pos_graduacao.conclusao"}, groupId = "${spring.application.name}")
  @Transactional
  public void consumirConclusao(ConclusaoPublicadaEvent event) {
    log.debug("Criando RequerimentoDiploma por conclusao: vinculoId={}", event.vinculoAcademicoId());
    if (requerimentoRepository.existsByVinculoAcademicoId(event.vinculoAcademicoId())) {
      log.debug("RequerimentoDiploma ja existe para vinculoId={}, ignorando", event.vinculoAcademicoId());
      return;
    }
    Pessoa pessoa = pessoaRepository.findById(event.pessoaId())
        .orElseThrow(() -> new IllegalStateException("Pessoa nao encontrada: pessoaId=" + event.pessoaId()));
    VinculoAcademico vinculo = vinculoRepository.findById(event.vinculoAcademicoId())
        .orElseThrow(() -> new IllegalStateException("VinculoAcademico nao encontrado: id=" + event.vinculoAcademicoId()));

    RequerimentoDiploma requerimento = new RequerimentoDiploma(pessoa, vinculo, LocalDate.now());
    requerimentoRepository.save(requerimento);
    log.debug("RequerimentoDiploma criado: vinculoId={}", event.vinculoAcademicoId());
  }
}
