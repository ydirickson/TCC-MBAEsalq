package br.com.tcc.posgraduacao.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Embeddable
public class ProgramaReferencia {

  @Column(name = "programa_id")
  private Long id;

  @Column(name = "programa_codigo", length = 50)
  private String codigo;

  @Column(name = "programa_nome", length = 150)
  private String nome;

  @Enumerated(EnumType.STRING)
  @Column(name = "programa_tipo", length = 30)
  private TipoPrograma tipo;

  public ProgramaReferencia() {
    // JPA
  }

  public ProgramaReferencia(Long id, String codigo, String nome, TipoPrograma tipo) {
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

  public TipoPrograma getTipo() {
    return tipo;
  }

  public void setTipo(TipoPrograma tipo) {
    this.tipo = tipo;
  }
}
