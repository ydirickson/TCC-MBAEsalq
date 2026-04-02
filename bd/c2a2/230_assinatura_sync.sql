-- Sincroniza documento assinavel e status de emissao via trigger cross-schema (cenario c2a2)
-- diplomas.documento_diploma INSERT/UPDATE → assinatura.documento_diploma (read model)
--                                          → assinatura.documento_assinavel
--                                          → assinatura.solicitacao_assinatura(PENDENTE)
-- assinatura.solicitacao_assinatura CONCLUIDA/REJEITADA → diplomas.status_emissao ASSINADO/REJEITADO

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'uq_assinatura_documento_assinavel_documento_diploma'
  ) THEN
    ALTER TABLE assinatura.documento_assinavel
      ADD CONSTRAINT uq_assinatura_documento_assinavel_documento_diploma
      UNIQUE (documento_diploma_id);
  END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_assinatura_solicitacao_ativa
  ON assinatura.solicitacao_assinatura (documento_assinavel_id)
  WHERE status IN ('PENDENTE', 'PARCIAL', 'CONCLUIDA');

-- Replica diplomas.documento_diploma em assinatura e cria os artefatos de assinatura
CREATE OR REPLACE FUNCTION public.fn_sync_documento_assinavel_c2a2()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_documento_assinavel_id BIGINT;
BEGIN
  -- Replica o documento_diploma no schema assinatura preservando o mesmo id
  INSERT INTO assinatura.documento_diploma (id, diploma_id, versao, data_geracao, url_arquivo, hash_documento)
  OVERRIDING SYSTEM VALUE
  VALUES (NEW.id, NEW.diploma_id, NEW.versao, NEW.data_geracao, NEW.url_arquivo, NEW.hash_documento)
  ON CONFLICT (id) DO UPDATE SET
    versao         = EXCLUDED.versao,
    data_geracao   = EXCLUDED.data_geracao,
    url_arquivo    = EXCLUDED.url_arquivo,
    hash_documento = EXCLUDED.hash_documento;

  -- Cria documento_assinavel se ainda nao existir para este documento_diploma
  INSERT INTO assinatura.documento_assinavel (documento_diploma_id, descricao, data_criacao)
  VALUES (
    NEW.id,
    'Documento diploma v' || NEW.versao,
    COALESCE(NEW.data_geracao::timestamp, NOW())
  )
  ON CONFLICT ON CONSTRAINT uq_assinatura_documento_assinavel_documento_diploma DO NOTHING;

  SELECT id INTO v_documento_assinavel_id
  FROM assinatura.documento_assinavel
  WHERE documento_diploma_id = NEW.id;

  -- Cria solicitacao_assinatura PENDENTE se nao existir ativa
  IF v_documento_assinavel_id IS NOT NULL THEN
    IF NOT EXISTS (
      SELECT 1 FROM assinatura.solicitacao_assinatura
      WHERE documento_assinavel_id = v_documento_assinavel_id
        AND status IN ('PENDENTE', 'PARCIAL', 'CONCLUIDA')
    ) THEN
      INSERT INTO assinatura.solicitacao_assinatura (documento_assinavel_id, status, data_solicitacao, data_conclusao)
      VALUES (v_documento_assinavel_id, 'PENDENTE', NOW(), NULL);
    END IF;
  END IF;

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_documento_assinavel_c2a2 ON diplomas.documento_diploma;
CREATE TRIGGER trg_sync_documento_assinavel_c2a2
AFTER INSERT OR UPDATE ON diplomas.documento_diploma
FOR EACH ROW
EXECUTE FUNCTION public.fn_sync_documento_assinavel_c2a2();

-- Atualiza diplomas.status_emissao quando solicitacao de assinatura e concluida ou rejeitada
CREATE OR REPLACE FUNCTION public.fn_sync_status_emissao_c2a2()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_requerimento_id BIGINT;
  v_status_emissao  VARCHAR(30);
  v_data_atualizacao TIMESTAMP;
BEGIN
  IF NEW.status IN ('CONCLUIDA', 'REJEITADA')
    AND (TG_OP = 'INSERT' OR OLD.status IS DISTINCT FROM NEW.status) THEN

    v_status_emissao   := CASE NEW.status WHEN 'CONCLUIDA' THEN 'ASSINADO' ELSE 'REJEITADO' END;
    v_data_atualizacao := COALESCE(NEW.data_conclusao, NOW());

    SELECT d.requerimento_id
    INTO v_requerimento_id
    FROM assinatura.documento_assinavel da
    JOIN diplomas.documento_diploma     dd ON dd.id = da.documento_diploma_id
    JOIN diplomas.diploma               d  ON d.id  = dd.diploma_id
    WHERE da.id = NEW.documento_assinavel_id;

    IF v_requerimento_id IS NOT NULL THEN
      INSERT INTO diplomas.status_emissao (requerimento_id, status, data_atualizacao)
      VALUES (v_requerimento_id, v_status_emissao, v_data_atualizacao)
      ON CONFLICT (requerimento_id)
      DO UPDATE SET
        status           = EXCLUDED.status,
        data_atualizacao = EXCLUDED.data_atualizacao;
    END IF;
  END IF;

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_status_emissao_c2a2 ON assinatura.solicitacao_assinatura;
CREATE TRIGGER trg_sync_status_emissao_c2a2
AFTER INSERT OR UPDATE ON assinatura.solicitacao_assinatura
FOR EACH ROW
EXECUTE FUNCTION public.fn_sync_status_emissao_c2a2();
