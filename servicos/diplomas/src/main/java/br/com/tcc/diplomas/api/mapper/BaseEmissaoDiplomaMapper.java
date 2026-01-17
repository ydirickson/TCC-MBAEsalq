package br.com.tcc.diplomas.api.mapper;

import br.com.tcc.diplomas.api.dto.BaseEmissaoDiplomaRequest;
import br.com.tcc.diplomas.api.dto.BaseEmissaoDiplomaResponse;
import br.com.tcc.diplomas.api.dto.RequerimentoDiplomaRequest;
import br.com.tcc.diplomas.domain.model.BaseEmissaoDiploma;
import br.com.tcc.diplomas.domain.model.Pessoa;
import br.com.tcc.diplomas.domain.model.RequerimentoDiploma;
import org.springframework.stereotype.Component;

@Component
public class BaseEmissaoDiplomaMapper {

  public BaseEmissaoDiploma toEntity(BaseEmissaoDiplomaRequest request, RequerimentoDiploma requerimento) {
    if (request == null || requerimento == null) {
      return null;
    }
    BaseEmissaoDiploma entity = new BaseEmissaoDiploma();
    entity.setRequerimento(requerimento);
    entity.setPessoaId(request.pessoaId());
    entity.setPessoaNome(request.pessoaNome());
    entity.setPessoaNomeSocial(request.pessoaNomeSocial());
    entity.setPessoaDataNascimento(request.pessoaDataNascimento());
    entity.setCursoCodigo(request.cursoCodigo());
    entity.setCursoNome(request.cursoNome());
    entity.setCursoTipo(request.cursoTipo());
    entity.setDataConclusao(request.dataConclusao());
    entity.setDataColacaoGrau(request.dataColacaoGrau());
    return entity;
  }

  public BaseEmissaoDiploma toEntityFromRequerimento(RequerimentoDiplomaRequest request, RequerimentoDiploma requerimento,
      Pessoa pessoa) {
    if (request == null || requerimento == null || pessoa == null) {
      return null;
    }
    BaseEmissaoDiploma entity = new BaseEmissaoDiploma();
    entity.setRequerimento(requerimento);
    entity.setPessoaId(pessoa.getId());
    entity.setPessoaNome(pessoa.getNome());
    entity.setPessoaNomeSocial(pessoa.getNomeSocial());
    entity.setPessoaDataNascimento(pessoa.getDataNascimento());
    entity.setCursoCodigo(request.cursoCodigo());
    entity.setCursoNome(request.cursoNome());
    entity.setCursoTipo(request.cursoTipo());
    entity.setDataConclusao(request.dataConclusao());
    entity.setDataColacaoGrau(request.dataColacaoGrau());
    return entity;
  }

  public BaseEmissaoDiplomaResponse toResponse(BaseEmissaoDiploma entity) {
    if (entity == null) {
      return null;
    }
    return new BaseEmissaoDiplomaResponse(
        entity.getId(),
        entity.getRequerimento() != null ? entity.getRequerimento().getId() : null,
        entity.getPessoaId(),
        entity.getPessoaNome(),
        entity.getPessoaNomeSocial(),
        entity.getPessoaDataNascimento(),
        entity.getCursoCodigo(),
        entity.getCursoNome(),
        entity.getCursoTipo(),
        entity.getDataConclusao(),
        entity.getDataColacaoGrau());
  }

  public void updateEntityFromRequest(BaseEmissaoDiplomaRequest request, BaseEmissaoDiploma entity,
      RequerimentoDiploma requerimento) {
    if (request == null || entity == null || requerimento == null) {
      return;
    }
    entity.setRequerimento(requerimento);
    entity.setPessoaId(request.pessoaId());
    entity.setPessoaNome(request.pessoaNome());
    entity.setPessoaNomeSocial(request.pessoaNomeSocial());
    entity.setPessoaDataNascimento(request.pessoaDataNascimento());
    entity.setCursoCodigo(request.cursoCodigo());
    entity.setCursoNome(request.cursoNome());
    entity.setCursoTipo(request.cursoTipo());
    entity.setDataConclusao(request.dataConclusao());
    entity.setDataColacaoGrau(request.dataColacaoGrau());
  }
}
