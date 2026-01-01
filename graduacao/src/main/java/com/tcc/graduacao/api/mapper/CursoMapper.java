package com.tcc.graduacao.api.mapper;

import com.tcc.graduacao.api.dto.CursoRequest;
import com.tcc.graduacao.api.dto.CursoResponse;
import com.tcc.graduacao.domain.model.CursoGraduacao;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CursoMapper {

  CursoGraduacao toEntity(CursoRequest request);

  CursoResponse toResponse(CursoGraduacao entity);

  void updateEntityFromRequest(CursoRequest request, @MappingTarget CursoGraduacao entity);
}
