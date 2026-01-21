# 4. Definição por serviço (domínio próprio + dados replicados)
[← Voltar ao índice](./README.md)

## 4.1. Serviço de Graduação
### 4.1.1. Responsável (source of truth)
- **Pessoa**: cadastro base do discente e do docente na graduação.
- **DocumentoIdentificação / Contato / Endereço**: cadastro de identificação e comunicação.
- **VínculoAcadêmico**: vínculo entre pessoa e curso (com referência do curso).
- **AlunoGraduação**: perfil acadêmico na graduação (matrícula por turma).
- **ProfessorGraduação**: perfil docente da graduação.
- **CursoGraduação**: cursos ofertados pela graduação.
- **DisciplinaGraduação / TurmaGraduação / OfertaDisciplina**: oferta acadêmica.
- **MatrículaDisciplina**: vínculo do aluno com uma oferta.
- **AvaliaçãoOfertaDisciplina / AvaliaçãoAluno**: avaliações e notas.

### 4.1.2. Entidades (descrição)
- **Pessoa**
  - Campos típicos: `id`, nome completo, data nascimento, nome social (opcional).
- **DocumentoIdentificação**
  - Tipo (CPF/RG/passaporte) e número.
- **Contato**
  - Email e telefone.
- **Endereço**
  - Logradouro, cidade, UF, CEP.
- **AlunoGraduação**
  - Referências a `Pessoa` e `TurmaGraduacao`, data de matrícula e status.
- **VínculoAcadêmico**
  - Referência à `Pessoa`, `CursoReferencia` (id/código/nome/tipo), tipo de vínculo, datas de ingresso/conclusão e situação.
- **ProfessorGraduação**
  - Referências a `Pessoa` e `CursoGraduacao`, data de ingresso, nível docente e situação funcional.
- **CursoGraduação**
  - Código, nome e carga horária.
- **DisciplinaGraduação**
  - Curso, código, nome e carga horária.
- **TurmaGraduação**
  - Curso, ano, semestre e status.
- **OfertaDisciplina**
  - Disciplina, professor, ano e semestre.
- **MatrículaDisciplina**
  - Aluno, oferta, data de matrícula, status e nota.
- **AvaliaçãoOfertaDisciplina**
  - Oferta, nome e peso da avaliação.
- **AvaliaçãoAluno**
  - Matrícula, avaliação e nota.

### 4.1.3. Dados replicados (read models locais)
- Pode replicar documentos e assinaturas para exibição/consulta (opcional):
  - **DocumentoDiploma (cópia)**: status do diploma/arquivo emitido.
  - **Assinatura (cópia)**: estado de assinaturas relacionadas.

---

## 4.2. Serviço de Pós-graduação
### 4.2.1. Responsável (source of truth)
- **Pessoa**: cadastro base do discente que ingressa na pós (pode existir pessoa que só exista na pós).
- **AlunoPósGraduação**: perfil acadêmico na pós.
- **ProgramaPós**: programas (mestrado, doutorado, especialização, etc.).
- **VínculoPósGraduação**: vínculo entre pessoa/aluno e programa.
- **Orientação**: orientador/coorientador.
- **Qualificação**: registros de qualificação (se aplicável).
- **Defesa**: registros de defesa e resultado.
- **SituaçãoAcadêmicaPós**: status (ativo, em pesquisa, concluído, desligado).

### 4.2.2. Entidades (descrição)
- **Pessoa** (mesmo conceito do serviço de graduação, mas com ownership definido por decisão de arquitetura)
- **AlunoPósGraduação**
  - Ex.: `pessoaId`, linha de pesquisa, área, etc.
- **ProgramaPós**
  - Ex.: `programaId`, nome, nível.
- **VínculoPósGraduação**
  - Ex.: `vinculoId`, `pessoaId`, `programaId`, status.
- **Orientação**
  - Ex.: `vinculoId`, orientador(es).
- **Qualificação / Defesa**
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
  - Ex.: `requerimentoId`, `pessoaId`, origem (grad/pós), `vinculoId`, data solicitação, status.
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
- **UsuárioAssinante** (vínculo com Pessoa)
- **MétodoAssinatura / Credencial / Certificado** (simulado)
- **SolicitaçãoAssinatura**
- **Assinatura**
- **ManifestoAssinatura** (metadados de validação e auditoria)
- **DocumentoAssinável** (metadados do arquivo a assinar, versões)

### 4.4.2. Entidades (descrição)
- **UsuárioAssinante**
  - Ex.: `assinanteId`, `pessoaId`, perfil/permissões.
- **DocumentoAssinável**
  - Ex.: `documentoId`, origem (diplomas, grad, pós), tipo, hash, versão, status.
- **SolicitaçãoAssinatura**
  - Ex.: `solicitacaoId`, `documentoId`, lista de signatários, ordem, prazo, status.
- **Assinatura**
  - Ex.: `assinaturaId`, `solicitacaoId`, `assinanteId`, data, resultado, hash final.
- **ManifestoAssinatura**
  - Ex.: `manifestoId`, trilha de auditoria, carimbo, dados de validação (simulados).

### 4.4.3. Dados replicados (read models locais)
- **Pessoa (cópia)**: para exibir e vincular signatários.
- **DocumentoDiploma (cópia)**: para incorporar e assinar o PDF emitido em Diplomas.
