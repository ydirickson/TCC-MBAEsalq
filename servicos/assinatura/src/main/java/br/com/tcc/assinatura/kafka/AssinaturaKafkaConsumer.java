package br.com.tcc.assinatura.kafka;

import br.com.tcc.assinatura.domain.model.CursoProgramaReferencia;
import br.com.tcc.assinatura.domain.model.Pessoa;
import br.com.tcc.assinatura.domain.model.SituacaoAcademica;
import br.com.tcc.assinatura.domain.model.TipoCursoPrograma;
import br.com.tcc.assinatura.domain.model.TipoVinculo;
import br.com.tcc.assinatura.domain.model.VinculoAcademico;
import br.com.tcc.assinatura.domain.repository.PessoaRepository;
import br.com.tcc.assinatura.domain.repository.VinculoAcademicoRepository;
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

  public AssinaturaKafkaConsumer(PessoaRepository pessoaRepository, VinculoAcademicoRepository vinculoRepository) {
    this.pessoaRepository = pessoaRepository;
    this.vinculoRepository = vinculoRepository;
  }

  @KafkaListener(topics = {"tcc.graduacao.pessoa", "tcc.pos_graduacao.pessoa"}, groupId = "${spring.application.name}")
  @Transactional
  public void consumirPessoa(PessoaEvent event) {
    log.debug("Replicando Pessoa em assinatura: id={}", event.id());
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
}
