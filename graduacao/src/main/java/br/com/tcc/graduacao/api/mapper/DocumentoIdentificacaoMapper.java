package br.com.tcc.graduacao.api.mapper;

import br.com.tcc.graduacao.api.dto.DocumentoIdentificacaoRequest;
import br.com.tcc.graduacao.api.dto.DocumentoIdentificacaoResponse;
import br.com.tcc.graduacao.domain.model.DocumentoIdentificacao;
import br.com.tcc.graduacao.domain.model.Pessoa;
import org.springframework.stereotype.Component;

@Component
public class DocumentoIdentificacaoMapper {

  public DocumentoIdentificacao toEntity(Pessoa pessoa, DocumentoIdentificacaoRequest request) {
    if (pessoa == null || request == null) {
      return null;
    }
    DocumentoIdentificacao documento = new DocumentoIdentificacao();
    documento.setPessoa(pessoa);
    documento.setTipo(request.tipo());
    documento.setNumero(request.numero());
    return documento;
  }

  public DocumentoIdentificacaoResponse toResponse(DocumentoIdentificacao documento) {
    if (documento == null) {
      return null;
    }
    Long pessoaId = documento.getPessoa() != null ? documento.getPessoa().getId() : null;
    return new DocumentoIdentificacaoResponse(
        documento.getId(),
        pessoaId,
        documento.getTipo(),
        documento.getNumero());
  }

  public void updateEntityFromRequest(DocumentoIdentificacaoRequest request, DocumentoIdentificacao documento) {
    if (request == null || documento == null) {
      return;
    }
    documento.setTipo(request.tipo());
    documento.setNumero(request.numero());
  }
}
