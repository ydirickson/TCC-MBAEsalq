  -- Sincroniza solicitacao de assinatura a partir de documento de diploma (cenario 1)

  DO $$
  BEGIN
    IF NOT EXISTS (
      SELECT 1
      FROM pg_constraint
      WHERE conname = 'uq_documento_assinavel_documento_diploma'
    ) THEN
      ALTER TABLE documento_assinavel
        ADD CONSTRAINT uq_documento_assinavel_documento_diploma
        UNIQUE (documento_diploma_id);
    END IF;

    IF EXISTS (
      SELECT 1
      FROM pg_constraint
      WHERE conname = 'uq_solicitacao_assinatura_documento_assinavel'
    ) THEN
      ALTER TABLE solicitacao_assinatura
        DROP CONSTRAINT uq_solicitacao_assinatura_documento_assinavel;
    END IF;
  END $$;

  CREATE UNIQUE INDEX IF NOT EXISTS uq_solicitacao_assinatura_ativa
    ON solicitacao_assinatura (documento_assinavel_id)
    WHERE status IN ('PENDENTE', 'PARCIAL', 'CONCLUIDA');

  CREATE OR REPLACE PROCEDURE sync_assinatura_documento_diploma(
    p_documento_diploma_id BIGINT,
    p_descricao VARCHAR,
    p_data_criacao TIMESTAMP
  )
  LANGUAGE plpgsql
  AS $$
  DECLARE
    v_documento_assinavel_id BIGINT;
  BEGIN
    INSERT INTO documento_assinavel (documento_diploma_id, descricao, data_criacao)
    VALUES (
      p_documento_diploma_id,
      COALESCE(p_descricao, 'Documento de diploma'),
      COALESCE(p_data_criacao, NOW())
    )
    ON CONFLICT (documento_diploma_id) DO NOTHING;

    SELECT id
    INTO v_documento_assinavel_id
    FROM documento_assinavel
    WHERE documento_diploma_id = p_documento_diploma_id;

    IF v_documento_assinavel_id IS NOT NULL THEN
      IF NOT EXISTS (
        SELECT 1
        FROM solicitacao_assinatura
        WHERE documento_assinavel_id = v_documento_assinavel_id
          AND status IN ('PENDENTE', 'PARCIAL', 'CONCLUIDA')
      ) THEN
        INSERT INTO solicitacao_assinatura (documento_assinavel_id, status, data_solicitacao, data_conclusao)
        VALUES (v_documento_assinavel_id, 'PENDENTE', NOW(), NULL);
      END IF;
    END IF;
  END;
  $$;

  CREATE OR REPLACE FUNCTION fn_sync_assinatura_documento_diploma()
  RETURNS TRIGGER
  LANGUAGE plpgsql
  AS $$
  DECLARE
    v_descricao VARCHAR(255);
    v_data_criacao TIMESTAMP;
  BEGIN
    v_descricao := 'Documento diploma v' || NEW.versao;
    v_data_criacao := COALESCE(NEW.data_geracao::timestamp, NOW());

    CALL sync_assinatura_documento_diploma(NEW.id, v_descricao, v_data_criacao);

    RETURN NEW;
  END;
  $$;

  CREATE OR REPLACE FUNCTION fn_sync_status_emissao_assinatura()
  RETURNS TRIGGER
  LANGUAGE plpgsql
  AS $$
  DECLARE
    v_requerimento_id BIGINT;
    v_status_emissao VARCHAR(30);
    v_data_atualizacao TIMESTAMP;
  BEGIN
    IF NEW.status IN ('CONCLUIDA', 'REJEITADA')
      AND (TG_OP = 'INSERT' OR OLD.status IS DISTINCT FROM NEW.status) THEN
      v_status_emissao := CASE NEW.status
        WHEN 'CONCLUIDA' THEN 'ASSINADO'
        ELSE 'REJEITADO'
      END;
      v_data_atualizacao := COALESCE(NEW.data_conclusao, NOW());

      SELECT d.requerimento_id
      INTO v_requerimento_id
      FROM documento_assinavel da
      JOIN documento_diploma dd ON dd.id = da.documento_diploma_id
      JOIN diploma d ON d.id = dd.diploma_id
      WHERE da.id = NEW.documento_assinavel_id;

      IF v_requerimento_id IS NOT NULL THEN
        INSERT INTO status_emissao (requerimento_id, status, data_atualizacao)
        VALUES (v_requerimento_id, v_status_emissao, v_data_atualizacao)
        ON CONFLICT (requerimento_id)
        DO UPDATE SET
          status = EXCLUDED.status,
          data_atualizacao = EXCLUDED.data_atualizacao;
      END IF;
    END IF;

    RETURN NEW;
  END;
  $$;

  CREATE OR REPLACE FUNCTION fn_sync_assinatura_pendente()
  RETURNS TRIGGER
  LANGUAGE plpgsql
  AS $$
  BEGIN
    IF NOT EXISTS (
      SELECT 1
      FROM assinatura
      WHERE solicitacao_id = NEW.id
    ) THEN
      INSERT INTO assinatura (solicitacao_id, usuario_assinante_id, status, data_assinatura, motivo_recusa)
      VALUES (NEW.id, NULL, 'PENDENTE', NULL, NULL);
    END IF;
    RETURN NEW;
  END;
  $$;

  CREATE OR REPLACE FUNCTION fn_sync_manifesto_assinatura()
  RETURNS TRIGGER
  LANGUAGE plpgsql
  AS $$
  BEGIN
    IF NEW.status = 'ASSINADA'
      AND (TG_OP = 'INSERT' OR OLD.status IS DISTINCT FROM NEW.status) THEN
      IF NOT EXISTS (
        SELECT 1
        FROM manifesto_assinatura
        WHERE solicitacao_id = NEW.solicitacao_id
      ) THEN
        INSERT INTO manifesto_assinatura (solicitacao_id, auditoria, carimbo_tempo, hash_final)
        VALUES (
          NEW.solicitacao_id,
          'Assinatura concluida',
          NOW(),
          'hash-final-' || NEW.solicitacao_id
        );
      END IF;
    END IF;
    RETURN NEW;
  END;
  $$;

  DROP TRIGGER IF EXISTS trg_sync_assinatura_documento_diploma ON documento_diploma;
  CREATE TRIGGER trg_sync_assinatura_documento_diploma
  AFTER INSERT OR UPDATE ON documento_diploma
  FOR EACH ROW
  EXECUTE FUNCTION fn_sync_assinatura_documento_diploma();

  DROP TRIGGER IF EXISTS trg_sync_assinatura_pendente ON solicitacao_assinatura;
  CREATE TRIGGER trg_sync_assinatura_pendente
  AFTER INSERT ON solicitacao_assinatura
  FOR EACH ROW
  EXECUTE FUNCTION fn_sync_assinatura_pendente();

  DROP TRIGGER IF EXISTS trg_sync_status_emissao_assinatura ON solicitacao_assinatura;
  CREATE TRIGGER trg_sync_status_emissao_assinatura
  AFTER INSERT OR UPDATE ON solicitacao_assinatura
  FOR EACH ROW
  EXECUTE FUNCTION fn_sync_status_emissao_assinatura();

  DROP TRIGGER IF EXISTS trg_sync_manifesto_assinatura ON assinatura;
  CREATE TRIGGER trg_sync_manifesto_assinatura
  AFTER INSERT OR UPDATE ON assinatura
  FOR EACH ROW
  EXECUTE FUNCTION fn_sync_manifesto_assinatura();
