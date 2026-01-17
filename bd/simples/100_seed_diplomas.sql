WITH selecionados AS (
  SELECT id,
         row_number() OVER (ORDER BY id) AS rn
  FROM vinculo_academico
  WHERE tipo_vinculo = 'GRADUACAO'
    AND situacao = 'ATIVO'
  ORDER BY id
  LIMIT 6
)
UPDATE vinculo_academico v
SET situacao = 'CONCLUIDO',
    data_conclusao = DATE '2024-12-20' + (s.rn * 7)::int
FROM selecionados s
WHERE v.id = s.id;

WITH vinculos_concluidos AS (
  SELECT v.id AS vinculo_id,
         v.pessoa_id,
         v.curso_codigo,
         v.curso_nome,
         v.curso_tipo,
         v.data_conclusao,
         row_number() OVER (ORDER BY v.id) AS rn
  FROM vinculo_academico v
  WHERE v.situacao = 'CONCLUIDO'
    AND v.data_conclusao IS NOT NULL
  ORDER BY v.id
  LIMIT 6
)
INSERT INTO requerimento_diploma (pessoa_id, vinculo_id, data_solicitacao)
SELECT v.pessoa_id,
       v.vinculo_id,
       v.data_conclusao + (v.rn * 5)
FROM vinculos_concluidos v;

INSERT INTO base_emissao_diploma (
  requerimento_id,
  pessoa_id,
  pessoa_nome,
  pessoa_nome_social,
  pessoa_data_nascimento,
  curso_codigo,
  curso_nome,
  curso_tipo,
  data_conclusao,
  data_colacao_grau
)
SELECT r.id,
       p.id,
       p.nome,
       p.nome_social,
       p.data_nascimento,
       v.curso_codigo,
       v.curso_nome,
       v.curso_tipo,
       v.data_conclusao,
       v.data_conclusao + INTERVAL '60 days'
FROM requerimento_diploma r
JOIN pessoa p ON p.id = r.pessoa_id
JOIN vinculo_academico v ON v.id = r.vinculo_id;

WITH requerimentos AS (
  SELECT r.id AS requerimento_id,
         b.id AS base_emissao_id,
         row_number() OVER (ORDER BY r.id) AS rn
  FROM requerimento_diploma r
  JOIN base_emissao_diploma b ON b.requerimento_id = r.id
)
INSERT INTO diploma (requerimento_id, base_emissao_id, numero_registro, data_emissao)
SELECT r.requerimento_id,
       r.base_emissao_id,
       'DIP-' || lpad(r.requerimento_id::text, 4, '0'),
       DATE '2025-02-15' + (r.rn * 3)
FROM requerimentos r
WHERE r.rn <= 3;

INSERT INTO documento_diploma (diploma_id, versao, data_geracao, url_arquivo, hash_documento)
SELECT d.id,
       1,
       d.data_emissao,
       'https://arquivos.tcc.local/diplomas/' || d.id || '/v1.pdf',
       'hash-' || d.id || '-v1'
FROM diploma d;

INSERT INTO status_emissao (requerimento_id, status, data_atualizacao)
SELECT r.id,
       CASE WHEN d.id IS NULL THEN 'SOLICITADO' ELSE 'EMITIDO' END,
       NOW()
FROM requerimento_diploma r
LEFT JOIN diploma d ON d.requerimento_id = r.id;
