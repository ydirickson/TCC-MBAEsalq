package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.api.dto.MatriculaDisciplinaRequest;
import br.com.tcc.posgraduacao.api.mapper.MatriculaDisciplinaMapper;
import br.com.tcc.posgraduacao.domain.model.AlunoPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.MatriculaDisciplina;
import br.com.tcc.posgraduacao.domain.model.OfertaDisciplina;
import br.com.tcc.posgraduacao.domain.repository.AlunoPosGraduacaoRepository;
import br.com.tcc.posgraduacao.domain.repository.MatriculaDisciplinaRepository;
import br.com.tcc.posgraduacao.domain.repository.OfertaDisciplinaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatriculaDisciplinaService {

  private final MatriculaDisciplinaRepository repository;
  private final AlunoPosGraduacaoRepository alunoRepository;
  private final OfertaDisciplinaRepository ofertaDisciplinaRepository;
  private final MatriculaDisciplinaMapper mapper;

  public MatriculaDisciplinaService(
      MatriculaDisciplinaRepository repository,
      AlunoPosGraduacaoRepository alunoRepository,
      OfertaDisciplinaRepository ofertaDisciplinaRepository,
      MatriculaDisciplinaMapper mapper) {
    this.repository = repository;
    this.alunoRepository = alunoRepository;
    this.ofertaDisciplinaRepository = ofertaDisciplinaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<MatriculaDisciplina> criar(MatriculaDisciplinaRequest request) {
    Optional<AlunoPosGraduacao> alunoOpt = alunoRepository.findById(request.alunoId());
    Optional<OfertaDisciplina> ofertaOpt = ofertaDisciplinaRepository.findById(request.ofertaDisciplinaId());
    if (alunoOpt.isEmpty() || ofertaOpt.isEmpty()) {
      return Optional.empty();
    }
    MatriculaDisciplina matricula = mapper.toEntity(alunoOpt.get(), ofertaOpt.get(), request);
    return Optional.of(repository.save(matricula));
  }

  public List<MatriculaDisciplina> listar(Long alunoId, Long ofertaDisciplinaId) {
    if (alunoId != null) {
      return repository.findByAlunoId(alunoId);
    }
    if (ofertaDisciplinaId != null) {
      return repository.findByOfertaDisciplinaId(ofertaDisciplinaId);
    }
    return repository.findAll();
  }

  public Optional<MatriculaDisciplina> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<MatriculaDisciplina> atualizar(Long id, MatriculaDisciplinaRequest request) {
    Optional<AlunoPosGraduacao> alunoOpt = alunoRepository.findById(request.alunoId());
    Optional<OfertaDisciplina> ofertaOpt = ofertaDisciplinaRepository.findById(request.ofertaDisciplinaId());
    if (alunoOpt.isEmpty() || ofertaOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(matricula -> {
      matricula.setAluno(alunoOpt.get());
      matricula.setOfertaDisciplina(ofertaOpt.get());
      matricula.setDataMatricula(request.dataMatricula());
      matricula.setStatus(request.status());
      matricula.setNota(request.nota());
      return matricula;
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
