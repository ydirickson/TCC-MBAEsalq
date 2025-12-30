package com.tcc.graduacao.domain.service;

import com.tcc.graduacao.domain.model.AlunoGraduacao;
import com.tcc.graduacao.domain.model.CursoGraduacao;
import com.tcc.graduacao.domain.model.SituacaoAcademica;
import com.tcc.graduacao.domain.repository.AlunoGraduacaoRepository;
import com.tcc.graduacao.domain.repository.CursoGraduacaoRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlunoGraduacaoService {

  private final AlunoGraduacaoRepository alunoRepository;
  private final CursoGraduacaoRepository cursoRepository;

  @Transactional
  public Optional<AlunoGraduacao> criar(Long pessoaId, Long cursoId, LocalDate dataIngresso, SituacaoAcademica status) {
    return cursoRepository.findById(cursoId)
        .map(curso -> alunoRepository.save(new AlunoGraduacao(pessoaId, curso, dataIngresso, status)));
  }

  public List<AlunoGraduacao> listar() {
    return alunoRepository.findAll();
  }

  public Optional<AlunoGraduacao> buscarPorId(Long id) {
    return alunoRepository.findById(id);
  }

  @Transactional
  public Optional<AlunoGraduacao> atualizar(Long id, Long pessoaId, Long cursoId, LocalDate dataIngresso, SituacaoAcademica status) {
    Optional<CursoGraduacao> cursoOpt = cursoRepository.findById(cursoId);
    if (cursoOpt.isEmpty()) {
      return Optional.empty();
    }

    return alunoRepository.findById(id).map(aluno -> {
      aluno.setPessoaId(pessoaId);
      aluno.setCurso(cursoOpt.get());
      aluno.setDataIngresso(dataIngresso);
      aluno.setStatus(status);
      return aluno;
    });
  }

  @Transactional
  public boolean remover(Long id) {
    if (!alunoRepository.existsById(id)) {
      return false;
    }
    alunoRepository.deleteById(id);
    return true;
  }
}
