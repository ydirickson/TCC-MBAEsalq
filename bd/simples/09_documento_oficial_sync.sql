-- Sincroniza documentos oficiais (graduacao/pos) para solicitacao de assinatura (cenario 1)

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_documento_oficial_origem'
  ) THEN
    ALTER TABLE documento_oficial
      ADD CONSTRAINT uq_documento_oficial_origem
      UNIQUE (origem_servico, origem_id, versao);
  END IF;

  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_documento_assinavel_documento_oficial'
  ) THEN
    ALTER TABLE documento_assinavel
      ADD CONSTRAINT uq_documento_assinavel_documento_oficial
      UNIQUE (documento_oficial_id);
  END IF;
END $$;

CREATE OR REPLACE PROCEDURE sync_documento_oficial_assinatura(
  p_origem_servico VARCHAR,
  p_origem_id BIGINT,
  p_pessoa_id BIGINT,
  p_tipo_documento VARCHAR,
  p_data_emissao DATE,
  p_versao INTEGER,
  p_url_arquivo VARCHAR,
  p_hash_documento VARCHAR
)
LANGUAGE plpgsql
AS $$
DECLARE
  v_documento_oficial_id BIGINT;
  v_documento_assinavel_id BIGINT;
  v_descricao VARCHAR(255);
BEGIN
  INSERT INTO documento_oficial (
    origem_servico,
    origem_id,
    pessoa_id,
    tipo_documento,
    data_emissao,
    versao,
    url_arquivo,
    hash_documento
  )
  VALUES (
    p_origem_servico,
    p_origem_id,
    p_pessoa_id,
    p_tipo_documento,
    p_data_emissao,
    p_versao,
    p_url_arquivo,
    p_hash_documento
  )
  ON CONFLICT (origem_servico, origem_id, versao)
  DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    tipo_documento = EXCLUDED.tipo_documento,
    data_emissao = EXCLUDED.data_emissao,
    url_arquivo = EXCLUDED.url_arquivo,
    hash_documento = EXCLUDED.hash_documento
  RETURNING id INTO v_documento_oficial_id;

  v_descricao := p_tipo_documento || ' - ' || p_origem_servico;

  INSERT INTO documento_assinavel (documento_oficial_id, descricao, data_criacao)
  VALUES (
    v_documento_oficial_id,
    v_descricao,
    NOW()
  )
  ON CONFLICT (documento_oficial_id) DO NOTHING;

  SELECT id
  INTO v_documento_assinavel_id
  FROM documento_assinavel
  WHERE documento_oficial_id = v_documento_oficial_id;

  IF v_documento_assinavel_id IS NOT NULL THEN
    INSERT INTO solicitacao_assinatura (documento_assinavel_id, status, data_solicitacao, data_conclusao)
    VALUES (v_documento_assinavel_id, 'PENDENTE', NOW(), NULL)
    ON CONFLICT (documento_assinavel_id) DO NOTHING;
  END IF;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_documento_oficial_graduacao()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  CALL sync_documento_oficial_assinatura(
    'GRADUACAO',
    NEW.id,
    NEW.pessoa_id,
    NEW.tipo_documento,
    NEW.data_emissao,
    NEW.versao,
    NEW.url_arquivo,
    NEW.hash_documento
  );

  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_documento_oficial_pos()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  CALL sync_documento_oficial_assinatura(
    'POS',
    NEW.id,
    NEW.pessoa_id,
    NEW.tipo_documento,
    NEW.data_emissao,
    NEW.versao,
    NEW.url_arquivo,
    NEW.hash_documento
  );

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_documento_oficial_graduacao ON documento_oficial_graduacao;
CREATE TRIGGER trg_sync_documento_oficial_graduacao
AFTER INSERT OR UPDATE ON documento_oficial_graduacao
FOR EACH ROW
EXECUTE FUNCTION fn_sync_documento_oficial_graduacao();

DROP TRIGGER IF EXISTS trg_sync_documento_oficial_pos ON documento_oficial_pos;
CREATE TRIGGER trg_sync_documento_oficial_pos
AFTER INSERT OR UPDATE ON documento_oficial_pos
FOR EACH ROW
EXECUTE FUNCTION fn_sync_documento_oficial_pos();
