package br.com.tcc.graduacao.domain.service;

import br.com.tcc.graduacao.api.dto.AvaliacaoOfertaDisciplinaRequest;
import br.com.tcc.graduacao.api.mapper.AvaliacaoOfertaDisciplinaMapper;
import br.com.tcc.graduacao.domain.model.AvaliacaoOfertaDisciplina;
import br.com.tcc.graduacao.domain.model.OfertaDisciplina;
import br.com.tcc.graduacao.domain.repository.AvaliacaoOfertaDisciplinaRepository;
import br.com.tcc.graduacao.domain.repository.OfertaDisciplinaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoOfertaDisciplinaService {

  private final AvaliacaoOfertaDisciplinaRepository repository;
  private final OfertaDisciplinaRepository ofertaRepository;
  private final AvaliacaoOfertaDisciplinaMapper mapper;

  public AvaliacaoOfertaDisciplinaService(
      AvaliacaoOfertaDisciplinaRepository repository,
      OfertaDisciplinaRepository ofertaRepository,
      AvaliacaoOfertaDisciplinaMapper mapper) {
    this.repository = repository;
    this.ofertaRepository = ofertaRepository;
    this.mapper = mapper;
  }

  @Transactional
  public Optional<AvaliacaoOfertaDisciplina> criar(Long ofertaDisciplinaId, AvaliacaoOfertaDisciplinaRequest request) {
    Optional<OfertaDisciplina> ofertaOpt = ofertaRepository.findById(ofertaDisciplinaId);
    if (ofertaOpt.isEmpty()) {
      return Optional.empty();
    }
    AvaliacaoOfertaDisciplina avaliacao = mapper.toEntity(ofertaOpt.get(), request);
    return Optional.of(repository.save(avaliacao));
  }

  public List<AvaliacaoOfertaDisciplina> listar(Long ofertaDisciplinaId) {
    return repository.findByOfertaDisciplinaId(ofertaDisciplinaId);
  }

  public Optional<AvaliacaoOfertaDisciplina> buscarPorId(Long ofertaDisciplinaId, Long avaliacaoId) {
    return repository.findByIdAndOfertaDisciplinaId(avaliacaoId, ofertaDisciplinaId);
  }

  @Transactional
  public Optional<AvaliacaoOfertaDisciplina> atualizar(
      Long ofertaDisciplinaId,
      Long avaliacaoId,
      AvaliacaoOfertaDisciplinaRequest request) {
    Optional<OfertaDisciplina> ofertaOpt = ofertaRepository.findById(ofertaDisciplinaId);
    if (ofertaOpt.isEmpty()) {
      return Optional.empty();
    }
    return repository.findByIdAndOfertaDisciplinaId(avaliacaoId, ofertaDisciplinaId).map(avaliacao -> {
      mapper.updateEntityFromRequest(request, ofertaOpt.get(), avaliacao);
      return avaliacao;
    });
  }

  @Transactional
  public boolean remover(Long ofertaDisciplinaId, Long avaliacaoId) {
    Optional<AvaliacaoOfertaDisciplina> avaliacaoOpt =
        repository.findByIdAndOfertaDisciplinaId(avaliacaoId, ofertaDisciplinaId);
    if (avaliacaoOpt.isEmpty()) {
      return false;
    }
    repository.delete(avaliacaoOpt.get());
    return true;
  }
}
