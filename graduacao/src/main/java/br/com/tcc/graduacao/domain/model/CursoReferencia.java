package br.com.tcc.graduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class CursoReferencia {

  @Column(name = "curso_id")
  private Long id;

  @Column(name = "curso_codigo", length = 50)
  private String codigo;

  @Column(name = "curso_nome", length = 150)
  private String nome;

  @Enumerated(EnumType.STRING)
  @Column(name = "curso_tipo", length = 30)
  private TipoCurso tipo;

  public CursoReferencia() {
    // JPA
  }

  public CursoReferencia(Long id, String codigo, String nome, TipoCurso tipo) {
    this.id = id;
    this.codigo = codigo;
    this.nome = nome;
    this.tipo = tipo;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getNome() {
    return nome;
  }

  public void setNome(String nome) {
    this.nome = nome;
  }

  public TipoCurso getTipo() {
    return tipo;
  }

  public void setTipo(TipoCurso tipo) {
    this.tipo = tipo;
  }
}
