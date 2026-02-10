WITH pessoas_selecionadas AS (
  SELECT id,
         row_number() OVER (ORDER BY id) AS rn
  FROM pessoa
  ORDER BY id
  LIMIT 4
)
INSERT INTO usuario_assinante (pessoa_id, email, ativo, data_cadastro)
SELECT p.id,
       'assinante' || p.id || '@exemplo.com',
       TRUE,
       DATE '2025-01-10' + ((p.rn - 1) * 3)::int
FROM pessoas_selecionadas p;

WITH documentos_base AS (
  SELECT id,
         row_number() OVER (ORDER BY id) AS rn
  FROM documento_diploma
  ORDER BY id
  LIMIT 3
)
INSERT INTO documento_assinavel (documento_diploma_id, descricao, data_criacao)
SELECT d.id,
       'Documento assinavel v' || d.rn,
       TIMESTAMP '2025-02-20 10:00:00' + ((d.rn - 1) * INTERVAL '2 days')
FROM documentos_base d;

WITH documentos_assinaveis AS (
  SELECT id,
         row_number() OVER (ORDER BY id) AS rn
  FROM documento_assinavel
  ORDER BY id
  LIMIT 3
)
INSERT INTO solicitacao_assinatura (documento_assinavel_id, status, data_solicitacao, data_conclusao)
SELECT d.id,
       CASE d.rn
         WHEN 1 THEN 'CONCLUIDA'
         WHEN 2 THEN 'PARCIAL'
         ELSE 'REJEITADA'
       END,
       TIMESTAMP '2025-02-22 09:00:00' + ((d.rn - 1) * INTERVAL '1 day'),
       CASE d.rn
         WHEN 1 THEN TIMESTAMP '2025-02-23 16:30:00'
         WHEN 2 THEN TIMESTAMP '2025-02-23 12:00:00'
         ELSE NULL
       END
FROM documentos_assinaveis d;

WITH solicitacoes AS (
  SELECT s.id,
         s.status,
         row_number() OVER (ORDER BY s.id) AS rn
  FROM solicitacao_assinatura s
  ORDER BY s.id
  LIMIT 3
),
assinantes AS (
  SELECT ua.id,
         row_number() OVER (ORDER BY ua.id) AS rn
  FROM usuario_assinante ua
  ORDER BY ua.id
  LIMIT 3
)
INSERT INTO assinatura (solicitacao_id, usuario_assinante_id, status, data_assinatura, motivo_recusa)
SELECT s.id,
       a.id,
       CASE s.status
         WHEN 'REJEITADA' THEN 'REJEITADA'
         ELSE 'ASSINADA'
       END,
       TIMESTAMP '2025-02-23 10:00:00' + ((s.rn - 1) * INTERVAL '1 day'),
       CASE s.status
         WHEN 'REJEITADA' THEN 'Documento recusado pelo assinante'
         ELSE NULL
       END
FROM solicitacoes s
JOIN assinantes a ON a.rn = s.rn;

INSERT INTO manifesto_assinatura (solicitacao_id, auditoria, carimbo_tempo, hash_final)
SELECT s.id,
       'Assinaturas concluidas',
       TIMESTAMP '2025-02-23 16:45:00',
       'hash-final-' || s.id
FROM solicitacao_assinatura s
WHERE s.status = 'CONCLUIDA';
