# 4. Definição por serviço (domínio próprio + dados replicados)
[← Voltar ao índice](./README.md)

## 4.1. Serviço de Graduação
### 4.1.1. Responsável (source of truth)
- **Pessoa**: cadastro base do discente que ingressa na graduação.
- **AlunoGraduacao**: perfil acadêmico na graduação.
- **CursoGraduacao**: cursos ofertados pela graduação.
- **VinculoGraduacao**: vínculo entre pessoa/aluno e curso.
- **Disciplina / Turma**: oferta semestral/ano das disciplinas.
- **MatriculaDisciplina**: vínculo do aluno com uma oferta/turma.
- **HistoricoAcademicoGraduacao**: notas, frequência e resultados.
- **SituacaoAcademicaGraduacao**: status do vínculo (ativo, trancado, concluído, cancelado).

### 4.1.2. Entidades (descrição)
- **Pessoa**
  - Campos típicos: `personId`, nome completo, data nascimento, nome social (opcional), etc.
- **DocumentoIdentificacao**
  - Ex.: CPF/RG/passaporte com tipo e número.
- **Contato**
  - Ex.: email, telefone.
- **Endereco**
  - Ex.: logradouro, cidade, UF, CEP.
- **AlunoGraduacao**
  - Ex.: `personId`, dados acadêmicos específicos (ano ingresso, etc.).
- **VinculoGraduacao**
  - Ex.: `academicLinkId`, `personId`, `cursoId`, status.
- **HistoricoAcademicoGraduacao**
  - Ex.: `academicLinkId`, registros de matrícula/nota/frequência por disciplina.

### 4.1.3. Dados replicados (read models locais)
- Pode replicar documentos e assinaturas para exibição/consulta (opcional):
  - **DocumentoDiploma (cópia)**: status do diploma/arquivo emitido.
  - **Assinatura (cópia)**: estado de assinaturas relacionadas.

---

## 4.2. Serviço de Pós-graduação
### 4.2.1. Responsável (source of truth)
- **Pessoa**: cadastro base do discente que ingressa na pós (pode existir pessoa que só exista na pós).
- **AlunoPosGraduacao**: perfil acadêmico na pós.
- **ProgramaPos**: programas (mestrado, doutorado, especialização, etc.).
- **VinculoPosGraduacao**: vínculo entre pessoa/aluno e programa.
- **Orientacao**: orientador/coorientador.
- **Qualificacao**: registros de qualificação (se aplicável).
- **Defesa**: registros de defesa e resultado.
- **SituacaoAcademicaPos**: status (ativo, em pesquisa, concluído, desligado).

### 4.2.2. Entidades (descrição)
- **Pessoa** (mesmo conceito do serviço de graduação, mas com ownership definido por decisão de arquitetura)
- **AlunoPosGraduacao**
  - Ex.: `personId`, linha de pesquisa, área, etc.
- **ProgramaPos**
  - Ex.: `programaId`, nome, nível.
- **VinculoPosGraduacao**
  - Ex.: `academicLinkId`, `personId`, `programaId`, status.
- **Orientacao**
  - Ex.: `academicLinkId`, orientador(es).
- **Qualificacao / Defesa**
  - Ex.: datas, resultado, ata/registro (metadados).

### 4.2.3. Dados replicados (read models locais)
- Opcionalmente, replicar documentos/diplomas/assinaturas para consulta:
  - **DocumentoDiploma (cópia)**
  - **Assinatura (cópia)**

---

## 4.3. Serviço de Diplomas
### 4.3.1. Responsável (source of truth)
- **RequerimentoDiploma**
- **Diploma**
- **BaseEmissaoDiploma (Snapshot)**: conjunto mínimo de dados necessários para emissão (fixa o estado no momento da emissão).
- **DocumentoDiploma**: PDF/arquivo emitido (metadados e versões).
- **StatusEmissao**: estados do fluxo (solicitado, em validação, emitido, cancelado, reemitido, etc.).

### 4.3.2. Entidades (descrição)
- **RequerimentoDiploma**
  - Ex.: `requerimentoId`, `personId`, origem (grad/pós), `academicLinkId`, data solicitação, status.
- **BaseEmissaoDiploma**
  - Snapshot contendo:
    - dados essenciais de Pessoa (nome, documento principal),
    - dados essenciais do curso/programa,
    - dados essenciais de conclusão (data, ato final).
  - Objetivo: reduzir dependência online de outros serviços.
- **Diploma**
  - Ex.: número, livro/folha (simulado), data emissão, status, referência à base/snapshot.
- **DocumentoDiploma**
  - Ex.: `documentoId`, tipo (PDF), hash, versão, localização, timestamp.

### 4.3.3. Dados replicados (read models locais)
- **Pessoa (cópia)**: necessária para emissão/validação (ou via snapshot).
- **Curso/Programa (cópia)**: nome, nível/modalidade.
- **Conclusão (cópia)**: indicação de elegibilidade (graduado/defendido/aprovado).

---

## 4.4. Serviço de Assinatura Eletrônica
### 4.4.1. Responsável (source of truth)
- **UsuarioAssinante** (vínculo com Pessoa)
- **MetodoAssinatura / Credencial / Certificado** (simulado)
- **SolicitacaoAssinatura**
- **Assinatura**
- **ManifestoAssinatura** (metadados de validação e auditoria)
- **DocumentoAssinavel** (metadados do arquivo a assinar, versões)

### 4.4.2. Entidades (descrição)
- **UsuarioAssinante**
  - Ex.: `assinanteId`, `personId`, perfil/permissões.
- **DocumentoAssinavel**
  - Ex.: `documentoId`, origem (diplomas, grad, pós), tipo, hash, versão, status.
- **SolicitacaoAssinatura**
  - Ex.: `solicitacaoId`, `documentoId`, lista de signatários, ordem, prazo, status.
- **Assinatura**
  - Ex.: `assinaturaId`, `solicitacaoId`, `assinanteId`, data, resultado, hash final.
- **ManifestoAssinatura**
  - Ex.: `manifestoId`, trilha de auditoria, carimbo, dados de validação (simulados).

### 4.4.3. Dados replicados (read models locais)
- **Pessoa (cópia)**: para exibir e vincular signatários.
- **DocumentoDiploma (cópia)**: para incorporar e assinar o PDF emitido em Diplomas.
