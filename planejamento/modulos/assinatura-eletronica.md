# Serviço de Assinatura Eletrônica

## Entidades
- UsuárioAssinante (ligado a Pessoa)
- DocumentoAssinável (referência ao documento a ser assinado)
- SolicitaçãoAssinatura
- Assinatura
- ManifestoAssinatura (auditoria, carimbo, hash final)
- Read models: Pessoa (cópia), DocumentoDiploma (cópia)

## Regras de Negócio da Simulação
- Consome `DocumentoDiplomaCriado` para criar `DocumentoAssinavel` e `SolicitacaoAssinatura` com signatários.
- Cada assinatura registrada publica `AssinaturaParcial`; quando todos assinam, publica `AssinaturaConcluida`.
- Erros ou recusa publicam `AssinaturaRejeitada` (gera carga de eventos).
- Replica Pessoa para exibir dados dos signatários; atualiza via `PessoaAtualizada`.
