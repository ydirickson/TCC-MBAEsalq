package br.com.tcc.diplomas.domain.service;

import br.com.tcc.diplomas.api.dto.RequerimentoDiplomaRequest;
import br.com.tcc.diplomas.api.mapper.BaseEmissaoDiplomaMapper;
import br.com.tcc.diplomas.api.mapper.RequerimentoDiplomaMapper;
import br.com.tcc.diplomas.domain.model.RequerimentoDiploma;
import br.com.tcc.diplomas.domain.model.SituacaoAcademica;
import br.com.tcc.diplomas.domain.model.StatusEmissao;
import br.com.tcc.diplomas.domain.model.StatusEmissaoTipo;
import br.com.tcc.diplomas.domain.repository.BaseEmissaoDiplomaRepository;
import br.com.tcc.diplomas.domain.repository.PessoaRepository;
import br.com.tcc.diplomas.domain.repository.RequerimentoDiplomaRepository;
import br.com.tcc.diplomas.domain.repository.StatusEmissaoRepository;
import br.com.tcc.diplomas.domain.repository.VinculoAcademicoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RequerimentoDiplomaService {

  private final RequerimentoDiplomaRepository repository;
  private final PessoaRepository pessoaRepository;
  private final VinculoAcademicoRepository vinculoRepository;
  private final BaseEmissaoDiplomaRepository baseRepository;
  private final StatusEmissaoRepository statusRepository;
  private final RequerimentoDiplomaMapper mapper;
  private final BaseEmissaoDiplomaMapper baseMapper;

  public RequerimentoDiplomaService(RequerimentoDiplomaRepository repository, PessoaRepository pessoaRepository,
      VinculoAcademicoRepository vinculoRepository, BaseEmissaoDiplomaRepository baseRepository,
      StatusEmissaoRepository statusRepository, RequerimentoDiplomaMapper mapper, BaseEmissaoDiplomaMapper baseMapper) {
    this.repository = repository;
    this.pessoaRepository = pessoaRepository;
    this.vinculoRepository = vinculoRepository;
    this.baseRepository = baseRepository;
    this.statusRepository = statusRepository;
    this.mapper = mapper;
    this.baseMapper = baseMapper;
  }

  @Transactional
  public RequerimentoDiploma criar(RequerimentoDiplomaRequest request) {
    var pessoa = pessoaRepository.findById(request.pessoaId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pessoa nao encontrada"));
    var vinculo = vinculoRepository.findById(request.vinculoId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vinculo academico nao encontrado"));
    if (vinculo.getSituacao() != SituacaoAcademica.CONCLUIDO) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Vinculo nao esta concluido");
    }

    RequerimentoDiploma requerimento = mapper.toEntity(request, pessoa, vinculo);
    requerimento = repository.save(requerimento);

    var base = baseMapper.toEntityFromRequerimento(request, requerimento, pessoa);
    base = baseRepository.save(base);

    var status = new StatusEmissao(requerimento, StatusEmissaoTipo.SOLICITADO, LocalDateTime.now());
    status = statusRepository.save(status);

    requerimento.setBaseEmissao(base);
    requerimento.setStatusEmissao(status);

    return requerimento;
  }

  public List<RequerimentoDiploma> listar() {
    return repository.findAll();
  }

  public Optional<RequerimentoDiploma> buscarPorId(Long id) {
    return repository.findById(id);
  }

  @Transactional
  public Optional<RequerimentoDiploma> atualizar(Long id, RequerimentoDiplomaRequest request) {
    var pessoa = pessoaRepository.findById(request.pessoaId());
    var vinculo = vinculoRepository.findById(request.vinculoId());
    if (pessoa.isEmpty() || vinculo.isEmpty()) {
      return Optional.empty();
    }
    return repository.findById(id).map(requerimento -> {
      mapper.updateEntityFromRequest(request, requerimento, pessoa.get(), vinculo.get());
      var base = requerimento.getBaseEmissao();
      if (base != null) {
        base.setPessoaId(pessoa.get().getId());
        base.setPessoaNome(pessoa.get().getNome());
        base.setPessoaNomeSocial(pessoa.get().getNomeSocial());
        base.setPessoaDataNascimento(pessoa.get().getDataNascimento());
        base.setCursoCodigo(request.cursoCodigo());
        base.setCursoNome(request.cursoNome());
        base.setCursoTipo(request.cursoTipo());
        base.setDataConclusao(request.dataConclusao());
        base.setDataColacaoGrau(request.dataColacaoGrau());
      }
      return requerimento;
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
