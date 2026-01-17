package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.api.dto.AvaliacaoAlunoRequest;
import br.com.tcc.graduacao.api.mapper.AvaliacaoAlunoMapper;
import br.com.tcc.graduacao.domain.model.AvaliacaoAluno;
import br.com.tcc.graduacao.domain.model.AvaliacaoOfertaDisciplina;
import br.com.tcc.graduacao.domain.model.MatriculaDisciplina;
import br.com.tcc.graduacao.domain.repository.AvaliacaoAlunoRepository;
import br.com.tcc.graduacao.domain.repository.AvaliacaoOfertaDisciplinaRepository;
import br.com.tcc.graduacao.domain.repository.MatriculaDisciplinaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoAlunoService {

  private final AvaliacaoAlunoRepository repository;
  private final MatriculaDisciplinaRepository matriculaRepository;
  private final AvaliacaoOfertaDisciplinaRepository avaliacaoRepository;
  private final AvaliacaoAlunoMapper mapper;

  public AvaliacaoAlunoService(
      AvaliacaoAlunoRepository repository,
      MatriculaDisciplinaRepository matriculaRepository,
      AvaliacaoOfertaDisciplinaRepository avaliacaoRepository,
      AvaliacaoAlunoMapper mapper) {
    this.repository = repository;
    this.matriculaRepository = matriculaRepository;
    this.avaliacaoRepository = avaliacaoRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<AvaliacaoAluno> criar(Long matriculaId, AvaliacaoAlunoRequest request) {
    Optional<MatriculaDisciplina> matriculaOpt = matriculaRepository.findById(matriculaId);
    Optional<AvaliacaoOfertaDisciplina> avaliacaoOpt = avaliacaoRepository.findById(request.avaliacaoId());
    if (matriculaOpt.isEmpty() || avaliacaoOpt.isEmpty()) {
      return Optional.empty();
    }
    if (!avaliacaoPertenceOferta(matriculaOpt.get(), avaliacaoOpt.get())) {
      return Optional.empty();
    }
    AvaliacaoAluno avaliacaoAluno = mapper.toEntity(matriculaOpt.get(), avaliacaoOpt.get(), request);
    return Optional.of(repository.save(avaliacaoAluno));
  }

  public List<AvaliacaoAluno> listar(Long matriculaId) {
    return repository.findByMatriculaId(matriculaId);
  }

  public Optional<AvaliacaoAluno> buscarPorId(Long matriculaId, Long avaliacaoAlunoId) {
    return repository.findByIdAndMatriculaId(avaliacaoAlunoId, matriculaId);
  }

  @Transactional
  public Optional<AvaliacaoAluno> atualizar(Long matriculaId, Long avaliacaoAlunoId, AvaliacaoAlunoRequest request) {
    Optional<MatriculaDisciplina> matriculaOpt = matriculaRepository.findById(matriculaId);
    Optional<AvaliacaoOfertaDisciplina> avaliacaoOpt = avaliacaoRepository.findById(request.avaliacaoId());
    if (matriculaOpt.isEmpty() || avaliacaoOpt.isEmpty()) {
      return Optional.empty();
    }
    if (!avaliacaoPertenceOferta(matriculaOpt.get(), avaliacaoOpt.get())) {
      return Optional.empty();
    }
    return repository.findByIdAndMatriculaId(avaliacaoAlunoId, matriculaId).map(avaliacaoAluno -> {
      mapper.updateEntityFromRequest(request, matriculaOpt.get(), avaliacaoOpt.get(), avaliacaoAluno);
      return avaliacaoAluno;
    });
  }

  @Transactional
  public boolean remover(Long matriculaId, Long avaliacaoAlunoId) {
    Optional<AvaliacaoAluno> avaliacaoOpt = repository.findByIdAndMatriculaId(avaliacaoAlunoId, matriculaId);
    if (avaliacaoOpt.isEmpty()) {
      return false;
    }
    repository.delete(avaliacaoOpt.get());
    return true;
  }

  private boolean avaliacaoPertenceOferta(MatriculaDisciplina matricula, AvaliacaoOfertaDisciplina avaliacao) {
    if (matricula.getOfertaDisciplina() == null || avaliacao.getOfertaDisciplina() == null) {
      return false;
    }
    Long ofertaMatriculaId = matricula.getOfertaDisciplina().getId();
    Long ofertaAvaliacaoId = avaliacao.getOfertaDisciplina().getId();
    return ofertaMatriculaId != null && ofertaMatriculaId.equals(ofertaAvaliacaoId);
  }
}
