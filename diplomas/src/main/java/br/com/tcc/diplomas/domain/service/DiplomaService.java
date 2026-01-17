package br.com.tcc.diplomas.domain.service;

import br.com.tcc.diplomas.api.dto.DiplomaRequest;
import br.com.tcc.diplomas.api.mapper.DiplomaMapper;
import br.com.tcc.diplomas.domain.model.Diploma;
import br.com.tcc.diplomas.domain.model.StatusEmissaoTipo;
import br.com.tcc.diplomas.domain.repository.DiplomaRepository;
import br.com.tcc.diplomas.domain.repository.RequerimentoDiplomaRepository;
import br.com.tcc.diplomas.domain.repository.StatusEmissaoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiplomaService {

  private final DiplomaRepository repository;
  private final RequerimentoDiplomaRepository requerimentoRepository;
  private final StatusEmissaoRepository statusRepository;
  private final DiplomaMapper mapper;

  public DiplomaService(DiplomaRepository repository, RequerimentoDiplomaRepository requerimentoRepository,
      StatusEmissaoRepository statusRepository, DiplomaMapper mapper) {
    this.repository = repository;
    this.requerimentoRepository = requerimentoRepository;
    this.statusRepository = statusRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<Diploma> criar(DiplomaRequest request) {
    return requerimentoRepository.findById(request.requerimentoId()).map(requerimento -> {
      var base = requerimento.getBaseEmissao();
      if (base == null) {
        return null;
      }
      Diploma diploma = repository.save(mapper.toEntity(request, requerimento, base));
      var status = requerimento.getStatusEmissao();
      if (status != null) {
        status.setStatus(StatusEmissaoTipo.EMITIDO);
        status.setDataAtualizacao(LocalDateTime.now());
        statusRepository.save(status);
      }
      requerimento.setDiploma(diploma);
      return diploma;
    });
  }

  public List<Diploma> listar() {
    return repository.findAll();
  }

  public Optional<Diploma> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<Diploma> atualizar(Long id, DiplomaRequest request) {
    return requerimentoRepository.findById(request.requerimentoId()).flatMap(requerimento -> {
      var base = requerimento.getBaseEmissao();
      if (base == null) {
        return Optional.empty();
      }
      return repository.findById(id).map(diploma -> {
        mapper.updateEntityFromRequest(request, diploma, requerimento, base);
        return diploma;
      });
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
