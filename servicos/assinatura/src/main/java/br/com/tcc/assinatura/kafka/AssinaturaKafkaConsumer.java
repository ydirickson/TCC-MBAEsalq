package br.com.tcc.assinatura.kafka;

import br.com.tcc.assinatura.domain.model.Assinatura;
import br.com.tcc.assinatura.domain.model.CursoProgramaReferencia;
import br.com.tcc.assinatura.domain.model.DocumentoAssinavel;
import br.com.tcc.assinatura.domain.model.DocumentoDiploma;
import br.com.tcc.assinatura.domain.model.Pessoa;
import br.com.tcc.assinatura.domain.model.SituacaoAcademica;
import br.com.tcc.assinatura.domain.model.SolicitacaoAssinatura;
import br.com.tcc.assinatura.domain.model.StatusAssinatura;
import br.com.tcc.assinatura.domain.model.StatusSolicitacaoAssinatura;
import br.com.tcc.assinatura.domain.model.TipoCursoPrograma;
import br.com.tcc.assinatura.domain.model.TipoVinculo;
import br.com.tcc.assinatura.domain.model.VinculoAcademico;
import br.com.tcc.assinatura.domain.repository.AssinaturaRepository;
import br.com.tcc.assinatura.domain.repository.DocumentoAssinavelRepository;
import br.com.tcc.assinatura.domain.repository.DocumentoDiplomaRepository;
import br.com.tcc.assinatura.domain.repository.PessoaRepository;
import br.com.tcc.assinatura.domain.repository.SolicitacaoAssinaturaRepository;
import br.com.tcc.assinatura.domain.repository.VinculoAcademicoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!no-kafka")
public class AssinaturaKafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(AssinaturaKafkaConsumer.class);

  private final PessoaRepository pessoaRepository;
  private final VinculoAcademicoRepository vinculoRepository;
  private final DocumentoDiplomaRepository documentoDiplomaRepository;
  private final DocumentoAssinavelRepository documentoAssinavelRepository;
  private final SolicitacaoAssinaturaRepository solicitacaoRepository;
  private final AssinaturaRepository assinaturaRepository;
  private final ObjectMapper objectMapper;

  public AssinaturaKafkaConsumer(PessoaRepository pessoaRepository, VinculoAcademicoRepository vinculoRepository,
      DocumentoDiplomaRepository documentoDiplomaRepository, DocumentoAssinavelRepository documentoAssinavelRepository,
      SolicitacaoAssinaturaRepository solicitacaoRepository, AssinaturaRepository assinaturaRepository,
      ObjectMapper objectMapper) {
    this.pessoaRepository = pessoaRepository;
    this.vinculoRepository = vinculoRepository;
    this.documentoDiplomaRepository = documentoDiplomaRepository;
    this.documentoAssinavelRepository = documentoAssinavelRepository;
    this.solicitacaoRepository = solicitacaoRepository;
    this.assinaturaRepository = assinaturaRepository;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = {"tcc.graduacao.pessoa", "tcc.pos_graduacao.pessoa"}, groupId = "${spring.application.name}")
  @Transactional
  public void consumirPessoa(String payload) throws Exception {
    PessoaEvent event = objectMapper.readValue(payload, PessoaEvent.class);
    log.debug("Replicando Pessoa em assinatura: id={}", event.id());
    pessoaRepository.upsert(event.id(), event.nome(), event.dataNascimento(), event.nomeSocial());
  }

  @KafkaListener(topics = {"tcc.graduacao.vinculo_academico", "tcc.pos_graduacao.vinculo_academico"}, groupId = "${spring.application.name}")
  @Transactional
  public void consumirVinculoAcademico(String payload) throws Exception {
    VinculoAcademicoEvent event = objectMapper.readValue(payload, VinculoAcademicoEvent.class);
    log.debug("Replicando VinculoAcademico em assinatura: id={}", event.id());
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

  @KafkaListener(topics = "tcc.diplomas.documento_diploma", groupId = "${spring.application.name}")
  @Transactional
  public void consumirDocumentoDiploma(String payload) throws Exception {
    DocumentoDiplomaEvent event = objectMapper.readValue(payload, DocumentoDiplomaEvent.class);
    log.debug("Processando DocumentoDiploma em assinatura: id={}", event.id());

    DocumentoDiploma documentoDiploma = documentoDiplomaRepository.findById(event.id()).orElseGet(DocumentoDiploma::new);
    documentoDiploma.setId(event.id());
    documentoDiploma.setDiplomaId(event.diplomaId());
    documentoDiploma.setVersao(event.versao());
    documentoDiploma.setDataGeracao(event.dataGeracao());
    documentoDiploma.setUrlArquivo(event.urlArquivo());
    documentoDiploma.setHashDocumento(event.hashDocumento());
    documentoDiplomaRepository.save(documentoDiploma);

    DocumentoAssinavel documentoAssinavel = documentoAssinavelRepository
        .findByDocumentoDiplomaId(event.id())
        .orElseGet(() -> {
          DocumentoAssinavel novo = new DocumentoAssinavel(
              documentoDiploma,
              "Documento diploma v" + event.versao(),
              LocalDateTime.now());
          return documentoAssinavelRepository.save(novo);
        });

    boolean jaExisteSolicitacao = solicitacaoRepository.existsByDocumentoAssinavelIdAndStatusIn(
        documentoAssinavel.getId(),
        List.of(StatusSolicitacaoAssinatura.PENDENTE, StatusSolicitacaoAssinatura.PARCIAL,
            StatusSolicitacaoAssinatura.CONCLUIDA));
    if (!jaExisteSolicitacao) {
      SolicitacaoAssinatura solicitacao = new SolicitacaoAssinatura(
          documentoAssinavel,
          StatusSolicitacaoAssinatura.PENDENTE,
          LocalDateTime.now(),
          null);
      solicitacaoRepository.save(solicitacao);

      Assinatura assinatura = new Assinatura();
      assinatura.setSolicitacao(solicitacao);
      assinatura.setStatus(StatusAssinatura.PENDENTE);
      assinaturaRepository.save(assinatura);
    }
  }
}
