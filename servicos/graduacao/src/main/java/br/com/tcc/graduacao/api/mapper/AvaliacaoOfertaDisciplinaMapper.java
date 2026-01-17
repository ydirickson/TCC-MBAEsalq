package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.AvaliacaoOfertaDisciplinaRequest;
import br.com.tcc.graduacao.api.dto.AvaliacaoOfertaDisciplinaResponse;
import br.com.tcc.graduacao.domain.model.AvaliacaoOfertaDisciplina;
import br.com.tcc.graduacao.domain.model.OfertaDisciplina;
import org.springframework.stereotype.Component;

@Component
public class AvaliacaoOfertaDisciplinaMapper {

  public AvaliacaoOfertaDisciplina toEntity(OfertaDisciplina oferta, AvaliacaoOfertaDisciplinaRequest request) {
    if (oferta == null || request == null) {
      return null;
    }
    AvaliacaoOfertaDisciplina avaliacao = new AvaliacaoOfertaDisciplina();
    avaliacao.setOfertaDisciplina(oferta);
    avaliacao.setNome(request.nome());
    avaliacao.setPeso(request.peso());
    return avaliacao;
  }

  public AvaliacaoOfertaDisciplinaResponse toResponse(AvaliacaoOfertaDisciplina avaliacao) {
    if (avaliacao == null) {
      return null;
    }
    Long ofertaId = avaliacao.getOfertaDisciplina() != null ? avaliacao.getOfertaDisciplina().getId() : null;
    return new AvaliacaoOfertaDisciplinaResponse(
        avaliacao.getId(),
        ofertaId,
        avaliacao.getNome(),
        avaliacao.getPeso());
  }

  public void updateEntityFromRequest(
      AvaliacaoOfertaDisciplinaRequest request,
      OfertaDisciplina oferta,
      AvaliacaoOfertaDisciplina avaliacao) {
    if (request == null || oferta == null || avaliacao == null) {
      return;
    }
    avaliacao.setOfertaDisciplina(oferta);
    avaliacao.setNome(request.nome());
    avaliacao.setPeso(request.peso());
  }
}
