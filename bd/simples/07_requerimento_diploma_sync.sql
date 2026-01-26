-- Sincroniza requerimento de diploma via trigger (cenario 1)

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_requerimento_diploma_vinculo'
  ) THEN
    ALTER TABLE requerimento_diploma
      ADD CONSTRAINT uq_requerimento_diploma_vinculo
      UNIQUE (vinculo_id);
  END IF;
END $$;

CREATE OR REPLACE PROCEDURE sync_requerimento_diploma(
  p_vinculo_id BIGINT,
  p_pessoa_id BIGINT,
  p_data_solicitacao DATE
)
LANGUAGE plpgsql
AS $$
DECLARE
  v_requerimento_id BIGINT;
  v_pessoa_nome VARCHAR(150);
  v_pessoa_nome_social VARCHAR(150);
  v_pessoa_data_nascimento DATE;
  v_curso_codigo VARCHAR(50);
  v_curso_nome VARCHAR(150);
  v_curso_tipo VARCHAR(30);
  v_data_conclusao DATE;
BEGIN
  SELECT p.nome,
         p.nome_social,
         p.data_nascimento,
         v.curso_codigo,
         v.curso_nome,
         v.curso_tipo,
         v.data_conclusao
  INTO v_pessoa_nome,
       v_pessoa_nome_social,
       v_pessoa_data_nascimento,
       v_curso_codigo,
       v_curso_nome,
       v_curso_tipo,
       v_data_conclusao
  FROM pessoa p
  JOIN vinculo_academico v ON v.id = p_vinculo_id
  WHERE p.id = p_pessoa_id;

  v_data_conclusao := COALESCE(v_data_conclusao, p_data_solicitacao);

  INSERT INTO requerimento_diploma (pessoa_id, vinculo_id, data_solicitacao)
  VALUES (p_pessoa_id, p_vinculo_id, p_data_solicitacao)
  ON CONFLICT (vinculo_id)
  DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    data_solicitacao = EXCLUDED.data_solicitacao
  RETURNING id INTO v_requerimento_id;

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
  VALUES (
    v_requerimento_id,
    p_pessoa_id,
    v_pessoa_nome,
    v_pessoa_nome_social,
    v_pessoa_data_nascimento,
    v_curso_codigo,
    v_curso_nome,
    v_curso_tipo,
    v_data_conclusao,
    NULL
  )
  ON CONFLICT (requerimento_id)
  DO UPDATE SET
    pessoa_id = EXCLUDED.pessoa_id,
    pessoa_nome = EXCLUDED.pessoa_nome,
    pessoa_nome_social = EXCLUDED.pessoa_nome_social,
    pessoa_data_nascimento = EXCLUDED.pessoa_data_nascimento,
    curso_codigo = EXCLUDED.curso_codigo,
    curso_nome = EXCLUDED.curso_nome,
    curso_tipo = EXCLUDED.curso_tipo,
    data_conclusao = EXCLUDED.data_conclusao,
    data_colacao_grau = EXCLUDED.data_colacao_grau;

  INSERT INTO status_emissao (requerimento_id, status, data_atualizacao)
  VALUES (v_requerimento_id, 'SOLICITADO', NOW())
  ON CONFLICT (requerimento_id) DO NOTHING;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_requerimento_diploma()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
  IF NEW.tipo_vinculo = 'ALUNO'
     AND NEW.situacao = 'CONCLUIDO' THEN
    IF TG_OP = 'INSERT'
       OR OLD.situacao IS DISTINCT FROM NEW.situacao
       OR OLD.data_conclusao IS DISTINCT FROM NEW.data_conclusao THEN
      CALL sync_requerimento_diploma(
        NEW.id,
        NEW.pessoa_id,
        COALESCE(NEW.data_conclusao, CURRENT_DATE)
      );
    END IF;
  END IF;

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_requerimento_diploma ON vinculo_academico;
CREATE TRIGGER trg_sync_requerimento_diploma
AFTER INSERT OR UPDATE ON vinculo_academico
FOR EACH ROW
EXECUTE FUNCTION fn_sync_requerimento_diploma();
