# 3. Entidades e intersecções (visão geral)
[← Voltar ao índice](./README.md)

## 3.1. Intersecções “fortes” (alta reutilização)
As entidades a seguir aparecem em múltiplos serviços e são candidatas naturais a replicação:

**Identidade e cadastro (aparece em todos):**
- Pessoa
- DocumentoIdentificacao
- Contato
- Endereco

**Vida acadêmica (grad/pós → diplomas):**
- Aluno (perfil acadêmico)
- Vinculo/Matrícula (aluno em curso/programa)
- Curso/Programa
- Situação acadêmica / conclusão

**Documentos formais (diplomas → assinatura):**
- Documento (arquivo, versão, metadados)
- SolicitaçãoAssinatura / Assinatura / Manifesto
