SET search_path TO diplomas;

INSERT INTO pessoa (id, nome, data_nascimento, nome_social)
SELECT id, nome, data_nascimento, nome_social
FROM graduacao.pessoa
ON CONFLICT DO NOTHING;

INSERT INTO documento_identificacao (id, pessoa_id, tipo, numero)
SELECT id, pessoa_id, tipo, numero
FROM graduacao.documento_identificacao
ON CONFLICT DO NOTHING;

INSERT INTO contato (id, pessoa_id, email, telefone)
SELECT id, pessoa_id, email, telefone
FROM graduacao.contato
ON CONFLICT DO NOTHING;

INSERT INTO endereco (id, pessoa_id, logradouro, cidade, uf, cep)
SELECT id, pessoa_id, logradouro, cidade, uf, cep
FROM graduacao.endereco
ON CONFLICT DO NOTHING;

INSERT INTO vinculo_academico (
  id,
  pessoa_id,
  curso_id,
  curso_codigo,
  curso_nome,
  curso_tipo,
  tipo_vinculo,
  data_ingresso,
  data_conclusao,
  situacao
)
SELECT
  id,
  pessoa_id,
  curso_id,
  curso_codigo,
  curso_nome,
  curso_tipo,
  tipo_vinculo,
  data_ingresso,
  data_conclusao,
  situacao
FROM graduacao.vinculo_academico
ON CONFLICT DO NOTHING;
