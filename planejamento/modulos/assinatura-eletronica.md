# Serviço de Assinatura Eletrônica

## Entidades
- UsuárioAssinante (ligado a Pessoa)
- DocumentoAssinável (referência ao documento a ser assinado)
- SolicitaçãoAssinatura
- Assinatura
- ManifestoAssinatura (auditoria, carimbo, hash final)
- Read models: Pessoa (cópia), DocumentoDiploma (cópia), DocumentoOficial (cópia)

## Regras de Negócio da Simulação
- Consome `DocumentoDiplomaCriado` e `DocumentoOficialCriado` para criar `DocumentoAssinavel`.
- Abre `SolicitacaoAssinatura` **apenas** se não existir solicitação ativa/concluída para o documento.
- Ao criar a solicitação, gera uma `Assinatura` em `PENDENTE` (pronta para assinar).
- Quando assina, registra `Assinatura` em `ASSINADA` e gera `ManifestoAssinatura`.
- Solicitação `CANCELADA`/`REJEITADA` permite nova solicitação futura; `CONCLUIDA` bloqueia novas.
- Replica Pessoa para exibir dados dos signatários; atualiza via `PessoaAtualizada`.

## Interfaces REST (simulação)
- Solicitações são subrecursos do documento assinável: `GET/POST/DELETE /documentos-assinaveis/{id}/solicitacoes-assinatura`.
