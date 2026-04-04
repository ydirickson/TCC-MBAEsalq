package br.com.tcc.diplomas.kafka;

import br.com.tcc.diplomas.domain.model.CursoProgramaReferencia;
import br.com.tcc.diplomas.domain.model.Pessoa;
import br.com.tcc.diplomas.domain.model.RequerimentoDiploma;
import br.com.tcc.diplomas.domain.model.SituacaoAcademica;
import br.com.tcc.diplomas.domain.model.StatusEmissaoTipo;
import br.com.tcc.diplomas.domain.model.TipoCursoPrograma;
import br.com.tcc.diplomas.domain.model.TipoVinculo;
import br.com.tcc.diplomas.domain.model.VinculoAcademico;
import br.com.tcc.diplomas.domain.repository.PessoaRepository;
import br.com.tcc.diplomas.domain.repository.RequerimentoDiplomaRepository;
import br.com.tcc.diplomas.domain.repository.StatusEmissaoRepository;
import br.com.tcc.diplomas.domain.repository.VinculoAcademicoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!no-kafka")
public class DiplomasKafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(DiplomasKafkaConsumer.class);

  private final PessoaRepository pessoaRepository;
  private final VinculoAcademicoRepository vinculoRepository;
  private final RequerimentoDiplomaRepository requerimentoRepository;
  private final StatusEmissaoRepository statusEmissaoRepository;
  private final ObjectMapper objectMapper;

  public DiplomasKafkaConsumer(
      PessoaRepository pessoaRepository,
      VinculoAcademicoRepository vinculoRepository,
      RequerimentoDiplomaRepository requerimentoRepository,
      StatusEmissaoRepository statusEmissaoRepository,
      ObjectMapper objectMapper) {
    this.pessoaRepository = pessoaRepository;
    this.vinculoRepository = vinculoRepository;
    this.requerimentoRepository = requerimentoRepository;
    this.statusEmissaoRepository = statusEmissaoRepository;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = {"tcc.graduacao.pessoa", "tcc.pos_graduacao.pessoa"}, groupId = "${spring.application.name}")
  @Transactional
  public void consumirPessoa(String payload) throws Exception {
    PessoaEvent event = objectMapper.readValue(payload, PessoaEvent.class);
    log.debug("Replicando Pessoa em diplomas: id={}", event.id());
    pessoaRepository.upsert(event.id(), event.nome(), event.dataNascimento(), event.nomeSocial());
  }

  @KafkaListener(topics = {"tcc.graduacao.vinculo_academico", "tcc.pos_graduacao.vinculo_academico"}, groupId = "${spring.application.name}")
  @Transactional
  public void consumirVinculoAcademico(String payload) throws Exception {
    VinculoAcademicoEvent event = objectMapper.readValue(payload, VinculoAcademicoEvent.class);
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
  public void consumirConclusao(String payload) throws Exception {
    ConclusaoPublicadaEvent event = objectMapper.readValue(payload, ConclusaoPublicadaEvent.class);
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

  @KafkaListener(topics = "tcc.assinatura.solicitacao_concluida", groupId = "${spring.application.name}")
  @Transactional
  public void consumirSolicitacaoConcluida(String payload) throws Exception {
    SolicitacaoConcluidaEvent event = objectMapper.readValue(payload, SolicitacaoConcluidaEvent.class);
    log.debug("Atualizando StatusEmissao por conclusao de assinatura: documentoDiplomaId={}", event.documentoDiplomaId());
    statusEmissaoRepository.findByDocumentoDiplomaId(event.documentoDiplomaId()).ifPresent(statusEmissao -> {
      StatusEmissaoTipo novoStatus = "CONCLUIDA".equals(event.status())
          ? StatusEmissaoTipo.ASSINADO
          : StatusEmissaoTipo.REJEITADO;
      statusEmissao.setStatus(novoStatus);
      statusEmissao.setDataAtualizacao(event.dataConclusao() != null ? event.dataConclusao() : LocalDateTime.now());
      log.debug("StatusEmissao atualizado id={} status={}", statusEmissao.getId(), novoStatus);
    });
  }
}
