SET search_path TO assinatura;

INSERT INTO pessoa (id, nome, data_nascimento, nome_social)
SELECT id, nome, data_nascimento, nome_social
FROM diplomas.pessoa
ON CONFLICT DO NOTHING;

INSERT INTO documento_identificacao (id, pessoa_id, tipo, numero)
SELECT id, pessoa_id, tipo, numero
FROM diplomas.documento_identificacao
ON CONFLICT DO NOTHING;

INSERT INTO contato (id, pessoa_id, email, telefone)
SELECT id, pessoa_id, email, telefone
FROM diplomas.contato
ON CONFLICT DO NOTHING;

INSERT INTO endereco (id, pessoa_id, logradouro, cidade, uf, cep)
SELECT id, pessoa_id, logradouro, cidade, uf, cep
FROM diplomas.endereco
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
FROM diplomas.vinculo_academico
ON CONFLICT DO NOTHING;

INSERT INTO documento_diploma (id, diploma_id, versao, data_geracao, url_arquivo, hash_documento)
SELECT id, diploma_id, versao, data_geracao, url_arquivo, hash_documento
FROM diplomas.documento_diploma
ON CONFLICT DO NOTHING;
