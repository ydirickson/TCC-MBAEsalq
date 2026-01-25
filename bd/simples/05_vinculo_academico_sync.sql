-- Sincroniza vinculo_academico via triggers (cenario 1)

DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_vinculo_academico_pessoa_curso_tipo'
  ) THEN
    ALTER TABLE vinculo_academico
      ADD CONSTRAINT uq_vinculo_academico_pessoa_curso_tipo
      UNIQUE (pessoa_id, curso_id, tipo_vinculo);
  END IF;
END $$;

CREATE OR REPLACE PROCEDURE sync_vinculo_academico(
  p_pessoa_id BIGINT,
  p_curso_id BIGINT,
  p_curso_codigo VARCHAR,
  p_curso_nome VARCHAR,
  p_curso_tipo VARCHAR,
  p_tipo_vinculo VARCHAR,
  p_data_ingresso DATE,
  p_data_conclusao DATE,
  p_situacao VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
  INSERT INTO vinculo_academico (
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
  VALUES (
    p_pessoa_id,
    p_curso_id,
    p_curso_codigo,
    p_curso_nome,
    p_curso_tipo,
    p_tipo_vinculo,
    p_data_ingresso,
    p_data_conclusao,
    p_situacao
  )
  ON CONFLICT (pessoa_id, curso_id, tipo_vinculo)
  DO UPDATE SET
    curso_codigo = EXCLUDED.curso_codigo,
    curso_nome = EXCLUDED.curso_nome,
    curso_tipo = EXCLUDED.curso_tipo,
    data_ingresso = EXCLUDED.data_ingresso,
    data_conclusao = EXCLUDED.data_conclusao,
    situacao = EXCLUDED.situacao;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_vinculo_aluno_graduacao()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_curso_id BIGINT;
  v_curso_codigo VARCHAR(50);
  v_curso_nome VARCHAR(150);
BEGIN
  SELECT c.id, c.codigo, c.nome
  INTO v_curso_id, v_curso_codigo, v_curso_nome
  FROM turma_graduacao t
  JOIN curso_graduacao c ON c.id = t.curso_id
  WHERE t.id = NEW.turma_graduacao_id;

  CALL sync_vinculo_academico(
    NEW.pessoa_id,
    v_curso_id,
    v_curso_codigo,
    v_curso_nome,
    'GRADUACAO',
    'ALUNO',
    NEW.data_matricula,
    NULL,
    NEW.status
  );

  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_vinculo_professor_graduacao()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_curso_id BIGINT;
  v_curso_codigo VARCHAR(50);
  v_curso_nome VARCHAR(150);
BEGIN
  SELECT c.id, c.codigo, c.nome
  INTO v_curso_id, v_curso_codigo, v_curso_nome
  FROM curso_graduacao c
  WHERE c.id = NEW.curso_id;

  CALL sync_vinculo_academico(
    NEW.pessoa_id,
    v_curso_id,
    v_curso_codigo,
    v_curso_nome,
    'GRADUACAO',
    'PROFESSOR',
    NEW.data_ingresso,
    NULL,
    NEW.status
  );

  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_vinculo_aluno_pos()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_programa_id BIGINT;
  v_programa_codigo VARCHAR(50);
  v_programa_nome VARCHAR(255);
  v_curso_tipo VARCHAR(30);
BEGIN
  SELECT p.id, p.codigo, p.nome
  INTO v_programa_id, v_programa_codigo, v_programa_nome
  FROM programa_pos p
  WHERE p.id = NEW.programa_id;

  v_curso_tipo := CASE v_programa_codigo
    WHEN 'PPGCC' THEN 'MESTRADO'
    WHEN 'PPGAG' THEN 'DOUTORADO'
    ELSE 'ESPECIALIZACAO'
  END;

  CALL sync_vinculo_academico(
    NEW.pessoa_id,
    v_programa_id,
    v_programa_codigo,
    v_programa_nome,
    v_curso_tipo,
    'ALUNO',
    NEW.data_matricula,
    NULL,
    NEW.status
  );

  RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION fn_sync_vinculo_professor_pos()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
  v_programa_id BIGINT;
  v_programa_codigo VARCHAR(50);
  v_programa_nome VARCHAR(255);
  v_curso_tipo VARCHAR(30);
BEGIN
  SELECT p.id, p.codigo, p.nome
  INTO v_programa_id, v_programa_codigo, v_programa_nome
  FROM programa_pos p
  WHERE p.id = NEW.programa_id;

  v_curso_tipo := CASE v_programa_codigo
    WHEN 'PPGCC' THEN 'MESTRADO'
    WHEN 'PPGAG' THEN 'DOUTORADO'
    ELSE 'ESPECIALIZACAO'
  END;

  CALL sync_vinculo_academico(
    NEW.pessoa_id,
    v_programa_id,
    v_programa_codigo,
    v_programa_nome,
    v_curso_tipo,
    'PROFESSOR',
    NEW.data_ingresso,
    NULL,
    NEW.status
  );

  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_vinculo_aluno_graduacao ON aluno_graduacao;
CREATE TRIGGER trg_sync_vinculo_aluno_graduacao
AFTER INSERT OR UPDATE ON aluno_graduacao
FOR EACH ROW
EXECUTE FUNCTION fn_sync_vinculo_aluno_graduacao();

DROP TRIGGER IF EXISTS trg_sync_vinculo_professor_graduacao ON professor_graduacao;
CREATE TRIGGER trg_sync_vinculo_professor_graduacao
AFTER INSERT OR UPDATE ON professor_graduacao
FOR EACH ROW
EXECUTE FUNCTION fn_sync_vinculo_professor_graduacao();

DROP TRIGGER IF EXISTS trg_sync_vinculo_aluno_pos ON aluno_pos_graduacao;
CREATE TRIGGER trg_sync_vinculo_aluno_pos
AFTER INSERT OR UPDATE ON aluno_pos_graduacao
FOR EACH ROW
EXECUTE FUNCTION fn_sync_vinculo_aluno_pos();

DROP TRIGGER IF EXISTS trg_sync_vinculo_professor_pos ON professor_pos;
CREATE TRIGGER trg_sync_vinculo_professor_pos
AFTER INSERT OR UPDATE ON professor_pos
FOR EACH ROW
EXECUTE FUNCTION fn_sync_vinculo_professor_pos();
