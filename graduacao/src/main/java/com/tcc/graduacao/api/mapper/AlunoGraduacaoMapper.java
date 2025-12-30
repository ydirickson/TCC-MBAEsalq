package com.tcc.graduacao.api.mapper;

import com.tcc.graduacao.api.dto.AlunoGraduacaoResponse;
import com.tcc.graduacao.domain.model.AlunoGraduacao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AlunoGraduacaoMapper {

  @Mapping(target = "cursoId", source = "curso.id")
  @Mapping(target = "cursoCodigo", source = "curso.codigo")
  @Mapping(target = "cursoNome", source = "curso.nome")
  AlunoGraduacaoResponse toResponse(AlunoGraduacao entity);
}
