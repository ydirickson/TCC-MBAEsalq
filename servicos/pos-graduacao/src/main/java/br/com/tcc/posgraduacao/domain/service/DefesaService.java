package br.com.tcc.posgraduacao.domain.service;

import br.com.tcc.posgraduacao.api.dto.DefesaMembroRequest;
import br.com.tcc.posgraduacao.api.dto.DefesaRequest;
import br.com.tcc.posgraduacao.domain.model.AlunoPosGraduacao;
import br.com.tcc.posgraduacao.domain.model.Defesa;
import br.com.tcc.posgraduacao.domain.model.DefesaMembro;
import br.com.tcc.posgraduacao.domain.model.ProfessorPosGraduacao;
import br.com.tcc.posgraduacao.domain.repository.AlunoPosGraduacaoRepository;
import br.com.tcc.posgraduacao.domain.repository.DefesaMembroRepository;
import br.com.tcc.posgraduacao.domain.repository.DefesaRepository;
import br.com.tcc.posgraduacao.domain.repository.ProfessorPosGraduacaoRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefesaService {

  private final DefesaRepository defesaRepository;
  private final DefesaMembroRepository membroRepository;
  private final AlunoPosGraduacaoRepository alunoRepository;
  private final ProfessorPosGraduacaoRepository professorRepository;

  public DefesaService(
      DefesaRepository defesaRepository,
      DefesaMembroRepository membroRepository,
      AlunoPosGraduacaoRepository alunoRepository,
      ProfessorPosGraduacaoRepository professorRepository) {
    this.defesaRepository = defesaRepository;
    this.membroRepository = membroRepository;
    this.alunoRepository = alunoRepository;
    this.professorRepository = professorRepository;
  }

  @Transactional
  public Optional<Defesa> criar(DefesaRequest request) {
    Optional<AlunoPosGraduacao> alunoOpt = alunoRepository.findById(request.alunoId());
    if (alunoOpt.isEmpty()) {
      return Optional.empty();
    }
    Defesa defesa = new Defesa(alunoOpt.get(), request.tipo(), request.nota());
    return Optional.of(defesaRepository.save(defesa));
  }

  public List<Defesa> listar(Long alunoId) {
    if (alunoId != null) {
      return defesaRepository.findByAlunoId(alunoId);
    }
    return defesaRepository.findAll();
  }

  public Optional<Defesa> buscarPorId(Long id) {
    return defesaRepository.findById(id);
  }

  @Transactional
  public Optional<Defesa> atualizar(Long id, DefesaRequest request) {
    Optional<AlunoPosGraduacao> alunoOpt = alunoRepository.findById(request.alunoId());
    if (alunoOpt.isEmpty()) {
      return Optional.empty();
    }
    return defesaRepository.findById(id).map(defesa -> {
      defesa.setAluno(alunoOpt.get());
      defesa.setTipo(request.tipo());
      defesa.setNota(request.nota());
      return defesa;
    });
  }

  @Transactional
  public boolean remover(Long id) {
    if (!defesaRepository.existsById(id)) {
      return false;
    }
    membroRepository.findByDefesaId(id).forEach(membroRepository::delete);
    defesaRepository.deleteById(id);
    return true;
  }

  @Transactional
  public Optional<DefesaMembro> criarMembro(Long defesaId, DefesaMembroRequest request) {
    Optional<Defesa> defesaOpt = defesaRepository.findById(defesaId);
    Optional<ProfessorPosGraduacao> professorOpt = professorRepository.findById(request.professorId());
    if (defesaOpt.isEmpty() || professorOpt.isEmpty()) {
      return Optional.empty();
    }
    DefesaMembro membro = new DefesaMembro(defesaOpt.get(), professorOpt.get(), request.nota(), request.presidente());
    return Optional.of(membroRepository.save(membro));
  }

  public List<DefesaMembro> listarMembros(Long defesaId) {
    return membroRepository.findByDefesaId(defesaId);
  }

  public Optional<DefesaMembro> buscarMembro(Long defesaId, Long membroId) {
    return membroRepository.findByDefesaIdAndId(defesaId, membroId);
  }

  @Transactional
  public Optional<DefesaMembro> atualizarMembro(Long defesaId, Long membroId, DefesaMembroRequest request) {
    Optional<Defesa> defesaOpt = defesaRepository.findById(defesaId);
    Optional<ProfessorPosGraduacao> professorOpt = professorRepository.findById(request.professorId());
    if (defesaOpt.isEmpty() || professorOpt.isEmpty()) {
      return Optional.empty();
    }
    return membroRepository.findByDefesaIdAndId(defesaId, membroId).map(membro -> {
      membro.setDefesa(defesaOpt.get());
      membro.setProfessor(professorOpt.get());
      membro.setNota(request.nota());
      membro.setPresidente(request.presidente());
      return membro;
    });
  }

  @Transactional
  public boolean removerMembro(Long defesaId, Long membroId) {
    if (membroRepository.findByDefesaIdAndId(defesaId, membroId).isEmpty()) {
      return false;
    }
    membroRepository.deleteById(membroId);
    return true;
  }
}
