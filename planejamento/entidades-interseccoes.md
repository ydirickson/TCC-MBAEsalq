# 3. Entidades e intersecções (visão geral)
[← Voltar ao índice](./README.md)

## 3.1. Intersecções “fortes” (alta reutilização)
As entidades a seguir aparecem em múltiplos serviços e são candidatas naturais a replicação:

**Identidade e cadastro (aparece em todos):**
- Pessoa
- DocumentoIdentificação
- Contato
- Endereço

**Vida acadêmica (grad/pós → diplomas):**
- Aluno (perfil acadêmico: graduação/pós)
- VínculoAcadêmico (pessoa em curso/programa, com referência ao catálogo e tipoCursoPrograma)
- Curso/Programa (catálogo)
- Situação acadêmica / conclusão

**Documentos formais (diplomas → assinatura):**
- Documento (arquivo, versão, metadados)
- DocumentoOficial (grad/pós → assinatura)
- DocumentoAssinavel (diplomas e documentos oficiais)
- SolicitaçãoAssinatura / Assinatura / Manifesto
