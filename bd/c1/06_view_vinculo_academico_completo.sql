-- View completa de vinculo_academico com dados de pessoa e documentos

CREATE OR REPLACE VIEW vw_vinculo_academico_completo AS
SELECT
  v.id AS vinculo_id,
  v.pessoa_id,
  p.nome AS pessoa_nome,
  p.nome_social AS pessoa_nome_social,
  p.data_nascimento AS pessoa_data_nascimento,
  di.tipo AS documento_tipo,
  di.numero AS documento_numero,
  c.email AS contato_email,
  c.telefone AS contato_telefone,
  e.logradouro AS endereco_logradouro,
  e.cidade AS endereco_cidade,
  e.uf AS endereco_uf,
  e.cep AS endereco_cep,
  v.curso_id,
  v.curso_codigo,
  v.curso_nome,
  v.curso_tipo,
  v.tipo_vinculo,
  v.data_ingresso,
  v.data_conclusao,
  v.situacao
FROM vinculo_academico v
JOIN pessoa p ON p.id = v.pessoa_id
LEFT JOIN documento_identificacao di ON di.pessoa_id = p.id
LEFT JOIN LATERAL (
  SELECT c.email, c.telefone
  FROM contato c
  WHERE c.pessoa_id = p.id
  ORDER BY c.id
  LIMIT 1
) c ON true
LEFT JOIN LATERAL (
  SELECT e.logradouro, e.cidade, e.uf, e.cep
  FROM endereco e
  WHERE e.pessoa_id = p.id
  ORDER BY e.id
  LIMIT 1
) e ON true;
