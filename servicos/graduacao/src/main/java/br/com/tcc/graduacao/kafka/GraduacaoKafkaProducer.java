package br.com.tcc.graduacao.kafka;

import br.com.tcc.graduacao.domain.model.Pessoa;
import br.com.tcc.graduacao.domain.model.VinculoAcademico;
import br.com.tcc.graduacao.domain.model.SituacaoAcademica;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!no-kafka")
public class GraduacaoKafkaProducer {

  static final String TOPICO_PESSOA = "tcc.graduacao.pessoa";
  static final String TOPICO_VINCULO = "tcc.graduacao.vinculo_academico";
  static final String TOPICO_CONCLUSAO = "tcc.graduacao.conclusao";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  public GraduacaoKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void publicarPessoa(Pessoa pessoa) {
    PessoaEvent event = new PessoaEvent(
        pessoa.getId(),
        pessoa.getNome(),
        pessoa.getDataNascimento(),
        pessoa.getNomeSocial());
    kafkaTemplate.send(TOPICO_PESSOA, String.valueOf(pessoa.getId()), event);
  }

  public void publicarVinculo(VinculoAcademico vinculo) {
    VinculoAcademicoEvent event = new VinculoAcademicoEvent(
        vinculo.getId(),
        vinculo.getPessoa().getId(),
        vinculo.getCurso().getId(),
        vinculo.getCurso().getCodigo(),
        vinculo.getCurso().getNome(),
        vinculo.getCurso().getTipo().name(),
        vinculo.getTipoVinculo().name(),
        vinculo.getDataIngresso(),
        vinculo.getDataConclusao(),
        vinculo.getSituacao().name());
    kafkaTemplate.send(TOPICO_VINCULO, String.valueOf(vinculo.getId()), event);

    if (SituacaoAcademica.CONCLUIDO.equals(vinculo.getSituacao())) {
      ConclusaoPublicadaEvent conclusao = new ConclusaoPublicadaEvent(
          vinculo.getPessoa().getId(),
          vinculo.getId(),
          vinculo.getCurso().getTipo().name(),
          vinculo.getDataConclusao());
      kafkaTemplate.send(TOPICO_CONCLUSAO, String.valueOf(vinculo.getPessoa().getId()), conclusao);
    }
  }
}
