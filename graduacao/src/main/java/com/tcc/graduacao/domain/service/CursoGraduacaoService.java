package com.tcc.graduacao.domain.service;

import com.tcc.graduacao.api.dto.CursoRequest;
import com.tcc.graduacao.api.mapper.CursoMapper;
import com.tcc.graduacao.domain.model.CursoGraduacao;
import com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CursoGraduacaoService {

  private final CursoGraduacaoRepository repository;
  private final CursoMapper mapper;

  public CursoGraduacaoService(CursoGraduacaoRepository repository, CursoMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Transactional
  public CursoGraduacao criarCurso(CursoRequest request) {
    return repository.save(mapper.toEntity(request));
  }

  public List<CursoGraduacao> listar() {
    return repository.findAll();
  }

  public Optional<CursoGraduacao> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<CursoGraduacao> atualizar(Long id, CursoRequest request) {
    return repository.findById(id).map(curso -> {
      mapper.updateEntityFromRequest(request, curso);
      return curso;
    });
  }

  @Transactional
  public boolean remover(Long id) {
    if (!repository.existsById(id)) {
      return false;
    }
    repository.deleteById(id);
    return true;
  }
}
