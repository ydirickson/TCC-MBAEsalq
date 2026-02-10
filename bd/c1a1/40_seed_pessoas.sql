WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Ana Almeida', '1980-01-01', 'Ana Almeida Social')
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000000' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'ana.almeida0@exemplo.com', '11 97000-1000' FROM pessoa
  UNION ALL
  SELECT id, 'ana.almeida0@sec.exemplo.com', '21 98000-2000' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Almeida 100', 'Sao Paulo', 'SP', '10000-200' FROM pessoa
  UNION ALL
  SELECT id, 'Avenida Principal 300', 'Sao Paulo', 'SP', '20000-300' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Bruno Dias', '1981-02-02', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000001' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'bruno.dias1@exemplo.com', '11 97001-1001' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Dias 101', 'Rio de Janeiro', 'RJ', '10001-201' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Carla Gomes', '1982-03-03', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000002' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'carla.gomes2@exemplo.com', '11 97002-1002' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Gomes 102', 'Belo Horizonte', 'MG', '10002-202' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Daniel Junqueira', '1983-04-04', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000003' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'daniel.junqueira3@exemplo.com', '11 97003-1003' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Junqueira 103', 'Curitiba', 'PR', '10003-203' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Eduardo Mendes', '1984-05-05', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000004' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'eduardo.mendes4@exemplo.com', '11 97004-1004' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Mendes 104', 'Porto Alegre', 'RS', '10004-204' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Fernanda Pereira', '1985-06-06', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000005' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'fernanda.pereira5@exemplo.com', '11 97005-1005' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Pereira 105', 'Salvador', 'BA', '10005-205' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Gabriel Silva', '1986-07-07', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000006' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'gabriel.silva6@exemplo.com', '11 97006-1006' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Silva 106', 'Recife', 'PE', '10006-206' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Helena Vieira', '1987-08-08', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000007' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'helena.vieira7@exemplo.com', '11 97007-1007' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Vieira 107', 'Fortaleza', 'CE', '10007-207' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Igor Yamamoto', '1988-09-09', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000008' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'igor.yamamoto8@exemplo.com', '11 97008-1008' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Yamamoto 108', 'Florianopolis', 'SC', '10008-208' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Juliana Barbosa', '1989-10-10', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000009' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'juliana.barbosa9@exemplo.com', '11 97009-1009' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Barbosa 109', 'Goiania', 'GO', '10009-209' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Kai Esteves', '1990-11-11', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000010' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'kai.esteves10@exemplo.com', '11 97010-1010' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Esteves 110', 'Sao Paulo', 'SP', '10010-210' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Larissa Hernandes', '1991-12-12', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000011' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'larissa.hernandes11@exemplo.com', '11 97011-1011' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Hernandes 111', 'Rio de Janeiro', 'RJ', '10011-211' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Marcos Klein', '1992-01-13', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000012' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'marcos.klein12@exemplo.com', '11 97012-1012' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Klein 112', 'Belo Horizonte', 'MG', '10012-212' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Natalia Nogueira', '1993-02-14', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000013' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'natalia.nogueira13@exemplo.com', '11 97013-1013' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Nogueira 113', 'Curitiba', 'PR', '10013-213' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Otavio Queiroz', '1994-03-15', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000014' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'otavio.queiroz14@exemplo.com', '11 97014-1014' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Queiroz 114', 'Porto Alegre', 'RS', '10014-214' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Patricia Teixeira', '1995-04-16', 'Patricia Teixeira Social')
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000015' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'patricia.teixeira15@exemplo.com', '11 97015-1015' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Teixeira 115', 'Salvador', 'BA', '10015-215' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Rafael Wagner', '1996-05-17', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000016' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'rafael.wagner16@exemplo.com', '11 97016-1016' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Wagner 116', 'Recife', 'PE', '10016-216' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Sabrina Zanetti', '1997-06-18', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000017' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'sabrina.zanetti17@exemplo.com', '11 97017-1017' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Zanetti 117', 'Fortaleza', 'CE', '10017-217' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Thiago Cardoso', '1998-07-19', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000018' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'thiago.cardoso18@exemplo.com', '11 97018-1018' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Cardoso 118', 'Florianopolis', 'SC', '10018-218' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Vanessa Ferreira', '1999-08-20', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000019' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'vanessa.ferreira19@exemplo.com', '11 97019-1019' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ferreira 119', 'Goiania', 'GO', '10019-219' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Will Ibrahim', '2000-09-21', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000020' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'will.ibrahim20@exemplo.com', '11 97020-1020' FROM pessoa
  UNION ALL
  SELECT id, 'will.ibrahim20@sec.exemplo.com', '21 98020-2020' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ibrahim 120', 'Sao Paulo', 'SP', '10020-220' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Xavier Lopes', '2001-10-22', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000021' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'xavier.lopes21@exemplo.com', '11 97021-1021' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Lopes 121', 'Rio de Janeiro', 'RJ', '10021-221' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Yara Oliveira', '2002-11-23', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000022' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'yara.oliveira22@exemplo.com', '11 97022-1022' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Oliveira 122', 'Belo Horizonte', 'MG', '10022-222' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Zeca Ribeiro', '1980-12-24', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000023' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'zeca.ribeiro23@exemplo.com', '11 97023-1023' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ribeiro 123', 'Curitiba', 'PR', '10023-223' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Ana Uchoa', '1981-01-25', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000024' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'ana.uchoa24@exemplo.com', '11 97024-1024' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Uchoa 124', 'Porto Alegre', 'RS', '10024-224' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Bruno Ximenes', '1982-02-26', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000025' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'bruno.ximenes25@exemplo.com', '11 97025-1025' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ximenes 125', 'Salvador', 'BA', '10025-225' FROM pessoa
  UNION ALL
  SELECT id, 'Avenida Principal 325', 'Salvador', 'BA', '20025-325' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Carla Almeida', '1983-03-27', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000026' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'carla.almeida26@exemplo.com', '11 97026-1026' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Almeida 126', 'Recife', 'PE', '10026-226' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Daniel Dias', '1984-04-01', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000027' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'daniel.dias27@exemplo.com', '11 97027-1027' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Dias 127', 'Fortaleza', 'CE', '10027-227' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Eduardo Gomes', '1985-05-02', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000028' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'eduardo.gomes28@exemplo.com', '11 97028-1028' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Gomes 128', 'Florianopolis', 'SC', '10028-228' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Fernanda Junqueira', '1986-06-03', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000029' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'fernanda.junqueira29@exemplo.com', '11 97029-1029' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Junqueira 129', 'Goiania', 'GO', '10029-229' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Gabriel Mendes', '1987-07-04', 'Gabriel Mendes Social')
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000030' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'gabriel.mendes30@exemplo.com', '11 97030-1030' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Mendes 130', 'Sao Paulo', 'SP', '10030-230' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Helena Pereira', '1988-08-05', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000031' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'helena.pereira31@exemplo.com', '11 97031-1031' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Pereira 131', 'Rio de Janeiro', 'RJ', '10031-231' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Igor Silva', '1989-09-06', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000032' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'igor.silva32@exemplo.com', '11 97032-1032' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Silva 132', 'Belo Horizonte', 'MG', '10032-232' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Juliana Vieira', '1990-10-07', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000033' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'juliana.vieira33@exemplo.com', '11 97033-1033' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Vieira 133', 'Curitiba', 'PR', '10033-233' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Kai Yamamoto', '1991-11-08', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000034' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'kai.yamamoto34@exemplo.com', '11 97034-1034' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Yamamoto 134', 'Porto Alegre', 'RS', '10034-234' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Larissa Barbosa', '1992-12-09', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000035' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'larissa.barbosa35@exemplo.com', '11 97035-1035' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Barbosa 135', 'Salvador', 'BA', '10035-235' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Marcos Esteves', '1993-01-10', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000036' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'marcos.esteves36@exemplo.com', '11 97036-1036' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Esteves 136', 'Recife', 'PE', '10036-236' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Natalia Hernandes', '1994-02-11', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000037' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'natalia.hernandes37@exemplo.com', '11 97037-1037' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Hernandes 137', 'Fortaleza', 'CE', '10037-237' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Otavio Klein', '1995-03-12', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000038' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'otavio.klein38@exemplo.com', '11 97038-1038' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Klein 138', 'Florianopolis', 'SC', '10038-238' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Patricia Nogueira', '1996-04-13', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000039' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'patricia.nogueira39@exemplo.com', '11 97039-1039' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Nogueira 139', 'Goiania', 'GO', '10039-239' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Rafael Queiroz', '1997-05-14', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000040' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'rafael.queiroz40@exemplo.com', '11 97040-1040' FROM pessoa
  UNION ALL
  SELECT id, 'rafael.queiroz40@sec.exemplo.com', '21 98040-2040' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Queiroz 140', 'Sao Paulo', 'SP', '10040-240' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Sabrina Teixeira', '1998-06-15', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000041' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'sabrina.teixeira41@exemplo.com', '11 97041-1041' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Teixeira 141', 'Rio de Janeiro', 'RJ', '10041-241' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Thiago Wagner', '1999-07-16', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000042' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'thiago.wagner42@exemplo.com', '11 97042-1042' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Wagner 142', 'Belo Horizonte', 'MG', '10042-242' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Vanessa Zanetti', '2000-08-17', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000043' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'vanessa.zanetti43@exemplo.com', '11 97043-1043' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Zanetti 143', 'Curitiba', 'PR', '10043-243' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Will Cardoso', '2001-09-18', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000044' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'will.cardoso44@exemplo.com', '11 97044-1044' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Cardoso 144', 'Porto Alegre', 'RS', '10044-244' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Xavier Ferreira', '2002-10-19', 'Xavier Ferreira Social')
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000045' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'xavier.ferreira45@exemplo.com', '11 97045-1045' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ferreira 145', 'Salvador', 'BA', '10045-245' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Yara Ibrahim', '1980-11-20', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000046' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'yara.ibrahim46@exemplo.com', '11 97046-1046' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ibrahim 146', 'Recife', 'PE', '10046-246' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Zeca Lopes', '1981-12-21', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000047' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'zeca.lopes47@exemplo.com', '11 97047-1047' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Lopes 147', 'Fortaleza', 'CE', '10047-247' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Ana Oliveira', '1982-01-22', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000048' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'ana.oliveira48@exemplo.com', '11 97048-1048' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Oliveira 148', 'Florianopolis', 'SC', '10048-248' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Bruno Ribeiro', '1983-02-23', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000049' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'bruno.ribeiro49@exemplo.com', '11 97049-1049' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ribeiro 149', 'Goiania', 'GO', '10049-249' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Carla Uchoa', '1984-03-24', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000050' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'carla.uchoa50@exemplo.com', '11 97050-1050' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Uchoa 150', 'Sao Paulo', 'SP', '10050-250' FROM pessoa
  UNION ALL
  SELECT id, 'Avenida Principal 350', 'Sao Paulo', 'SP', '20050-350' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Daniel Ximenes', '1985-04-25', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000051' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'daniel.ximenes51@exemplo.com', '11 97051-1051' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ximenes 151', 'Rio de Janeiro', 'RJ', '10051-251' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Eduardo Almeida', '1986-05-26', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000052' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'eduardo.almeida52@exemplo.com', '11 97052-1052' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Almeida 152', 'Belo Horizonte', 'MG', '10052-252' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Fernanda Dias', '1987-06-27', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000053' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'fernanda.dias53@exemplo.com', '11 97053-1053' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Dias 153', 'Curitiba', 'PR', '10053-253' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Gabriel Gomes', '1988-07-01', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000054' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'gabriel.gomes54@exemplo.com', '11 97054-1054' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Gomes 154', 'Porto Alegre', 'RS', '10054-254' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Helena Junqueira', '1989-08-02', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000055' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'helena.junqueira55@exemplo.com', '11 97055-1055' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Junqueira 155', 'Salvador', 'BA', '10055-255' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Igor Mendes', '1990-09-03', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000056' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'igor.mendes56@exemplo.com', '11 97056-1056' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Mendes 156', 'Recife', 'PE', '10056-256' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Juliana Pereira', '1991-10-04', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000057' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'juliana.pereira57@exemplo.com', '11 97057-1057' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Pereira 157', 'Fortaleza', 'CE', '10057-257' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Kai Silva', '1992-11-05', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000058' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'kai.silva58@exemplo.com', '11 97058-1058' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Silva 158', 'Florianopolis', 'SC', '10058-258' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Larissa Vieira', '1993-12-06', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000059' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'larissa.vieira59@exemplo.com', '11 97059-1059' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Vieira 159', 'Goiania', 'GO', '10059-259' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Marcos Yamamoto', '1994-01-07', 'Marcos Yamamoto Social')
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000060' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'marcos.yamamoto60@exemplo.com', '11 97060-1060' FROM pessoa
  UNION ALL
  SELECT id, 'marcos.yamamoto60@sec.exemplo.com', '21 98060-2060' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Yamamoto 160', 'Sao Paulo', 'SP', '10060-260' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Natalia Barbosa', '1995-02-08', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000061' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'natalia.barbosa61@exemplo.com', '11 97061-1061' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Barbosa 161', 'Rio de Janeiro', 'RJ', '10061-261' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Otavio Esteves', '1996-03-09', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000062' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'otavio.esteves62@exemplo.com', '11 97062-1062' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Esteves 162', 'Belo Horizonte', 'MG', '10062-262' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Patricia Hernandes', '1997-04-10', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000063' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'patricia.hernandes63@exemplo.com', '11 97063-1063' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Hernandes 163', 'Curitiba', 'PR', '10063-263' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Rafael Klein', '1998-05-11', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000064' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'rafael.klein64@exemplo.com', '11 97064-1064' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Klein 164', 'Porto Alegre', 'RS', '10064-264' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Sabrina Nogueira', '1999-06-12', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000065' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'sabrina.nogueira65@exemplo.com', '11 97065-1065' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Nogueira 165', 'Salvador', 'BA', '10065-265' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Thiago Queiroz', '2000-07-13', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000066' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'thiago.queiroz66@exemplo.com', '11 97066-1066' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Queiroz 166', 'Recife', 'PE', '10066-266' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Vanessa Teixeira', '2001-08-14', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000067' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'vanessa.teixeira67@exemplo.com', '11 97067-1067' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Teixeira 167', 'Fortaleza', 'CE', '10067-267' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Will Wagner', '2002-09-15', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000068' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'will.wagner68@exemplo.com', '11 97068-1068' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Wagner 168', 'Florianopolis', 'SC', '10068-268' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Xavier Zanetti', '1980-10-16', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000069' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'xavier.zanetti69@exemplo.com', '11 97069-1069' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Zanetti 169', 'Goiania', 'GO', '10069-269' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Yara Cardoso', '1981-11-17', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000070' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'yara.cardoso70@exemplo.com', '11 97070-1070' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Cardoso 170', 'Sao Paulo', 'SP', '10070-270' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Zeca Ferreira', '1982-12-18', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000071' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'zeca.ferreira71@exemplo.com', '11 97071-1071' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ferreira 171', 'Rio de Janeiro', 'RJ', '10071-271' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Ana Ibrahim', '1983-01-19', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000072' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'ana.ibrahim72@exemplo.com', '11 97072-1072' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ibrahim 172', 'Belo Horizonte', 'MG', '10072-272' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Bruno Lopes', '1984-02-20', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000073' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'bruno.lopes73@exemplo.com', '11 97073-1073' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Lopes 173', 'Curitiba', 'PR', '10073-273' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Carla Oliveira', '1985-03-21', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000074' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'carla.oliveira74@exemplo.com', '11 97074-1074' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Oliveira 174', 'Porto Alegre', 'RS', '10074-274' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Daniel Ribeiro', '1986-04-22', 'Daniel Ribeiro Social')
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000075' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'daniel.ribeiro75@exemplo.com', '11 97075-1075' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ribeiro 175', 'Salvador', 'BA', '10075-275' FROM pessoa
  UNION ALL
  SELECT id, 'Avenida Principal 375', 'Salvador', 'BA', '20075-375' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Eduardo Uchoa', '1987-05-23', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000076' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'eduardo.uchoa76@exemplo.com', '11 97076-1076' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Uchoa 176', 'Recife', 'PE', '10076-276' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Fernanda Ximenes', '1988-06-24', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000077' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'fernanda.ximenes77@exemplo.com', '11 97077-1077' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ximenes 177', 'Fortaleza', 'CE', '10077-277' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Gabriel Almeida', '1989-07-25', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000078' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'gabriel.almeida78@exemplo.com', '11 97078-1078' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Almeida 178', 'Florianopolis', 'SC', '10078-278' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Helena Dias', '1990-08-26', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000079' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'helena.dias79@exemplo.com', '11 97079-1079' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Dias 179', 'Goiania', 'GO', '10079-279' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Igor Gomes', '1991-09-27', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000080' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'igor.gomes80@exemplo.com', '11 97080-1080' FROM pessoa
  UNION ALL
  SELECT id, 'igor.gomes80@sec.exemplo.com', '21 98080-2080' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Gomes 180', 'Sao Paulo', 'SP', '10080-280' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Juliana Junqueira', '1992-10-01', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000081' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'juliana.junqueira81@exemplo.com', '11 97081-1081' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Junqueira 181', 'Rio de Janeiro', 'RJ', '10081-281' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Kai Mendes', '1993-11-02', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000082' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'kai.mendes82@exemplo.com', '11 97082-1082' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Mendes 182', 'Belo Horizonte', 'MG', '10082-282' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Larissa Pereira', '1994-12-03', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000083' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'larissa.pereira83@exemplo.com', '11 97083-1083' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Pereira 183', 'Curitiba', 'PR', '10083-283' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Marcos Silva', '1995-01-04', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000084' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'marcos.silva84@exemplo.com', '11 97084-1084' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Silva 184', 'Porto Alegre', 'RS', '10084-284' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Natalia Vieira', '1996-02-05', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000085' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'natalia.vieira85@exemplo.com', '11 97085-1085' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Vieira 185', 'Salvador', 'BA', '10085-285' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Otavio Yamamoto', '1997-03-06', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000086' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'otavio.yamamoto86@exemplo.com', '11 97086-1086' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Yamamoto 186', 'Recife', 'PE', '10086-286' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Patricia Barbosa', '1998-04-07', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000087' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'patricia.barbosa87@exemplo.com', '11 97087-1087' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Barbosa 187', 'Fortaleza', 'CE', '10087-287' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Rafael Esteves', '1999-05-08', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000088' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'rafael.esteves88@exemplo.com', '11 97088-1088' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Esteves 188', 'Florianopolis', 'SC', '10088-288' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Sabrina Hernandes', '2000-06-09', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000089' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'sabrina.hernandes89@exemplo.com', '11 97089-1089' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Hernandes 189', 'Goiania', 'GO', '10089-289' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Thiago Klein', '2001-07-10', 'Thiago Klein Social')
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000090' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'thiago.klein90@exemplo.com', '11 97090-1090' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Klein 190', 'Sao Paulo', 'SP', '10090-290' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Vanessa Nogueira', '2002-08-11', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000091' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'vanessa.nogueira91@exemplo.com', '11 97091-1091' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Nogueira 191', 'Rio de Janeiro', 'RJ', '10091-291' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Will Queiroz', '1980-09-12', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000092' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'will.queiroz92@exemplo.com', '11 97092-1092' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Queiroz 192', 'Belo Horizonte', 'MG', '10092-292' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Xavier Teixeira', '1981-10-13', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000093' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'xavier.teixeira93@exemplo.com', '11 97093-1093' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Teixeira 193', 'Curitiba', 'PR', '10093-293' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Yara Wagner', '1982-11-14', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000094' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'yara.wagner94@exemplo.com', '11 97094-1094' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Wagner 194', 'Porto Alegre', 'RS', '10094-294' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Zeca Zanetti', '1983-12-15', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000095' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'zeca.zanetti95@exemplo.com', '11 97095-1095' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Zanetti 195', 'Salvador', 'BA', '10095-295' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Ana Cardoso', '1984-01-16', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000096' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'ana.cardoso96@exemplo.com', '11 97096-1096' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Cardoso 196', 'Recife', 'PE', '10096-296' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Bruno Ferreira', '1985-02-17', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'RG', 'DOC000097' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'bruno.ferreira97@exemplo.com', '11 97097-1097' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ferreira 197', 'Fortaleza', 'CE', '10097-297' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Carla Ibrahim', '1986-03-18', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'PASSAPORTE', 'DOC000098' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'carla.ibrahim98@exemplo.com', '11 97098-1098' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Ibrahim 198', 'Florianopolis', 'SC', '10098-298' FROM pessoa
)
SELECT 1;

WITH pessoa AS (
  INSERT INTO pessoa (nome, data_nascimento, nome_social)
  VALUES ('Daniel Lopes', '1987-04-19', NULL)
  RETURNING id
),
documento AS (
  INSERT INTO documento_identificacao (pessoa_id, tipo, numero)
  SELECT id, 'CPF', 'DOC000099' FROM pessoa
)
, contatos AS (
  INSERT INTO contato (pessoa_id, email, telefone)
  SELECT id, 'daniel.lopes99@exemplo.com', '11 97099-1099' FROM pessoa
)
, enderecos AS (
  INSERT INTO endereco (pessoa_id, logradouro, cidade, uf, cep)
  SELECT id, 'Rua Lopes 199', 'Goiania', 'GO', '10099-299' FROM pessoa
)
SELECT 1;
