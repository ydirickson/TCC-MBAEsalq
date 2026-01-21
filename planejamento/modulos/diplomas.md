# Serviço de Diplomas

## Entidades
- RequerimentoDiploma
- BaseEmissãoDiploma (snapshot de Pessoa, Curso/Programa, Conclusão no momento da solicitação)
- Diploma
- DocumentoDiploma (metadados do PDF emitido)
- StatusEmissão (estado do fluxo)
- Read models: Pessoa (cópia), VínculoAcadêmico (cópia), Curso/Programa (cópia), Conclusão (cópia)

## Regras de Negócio da Simulação
- Consome `VinculoAcademicoAtualizado` e `ConclusaoPublicada` para manter elegibilidade local.
- Ao registrar `RequerimentoDiploma`, valida elegibilidade (status concluído) e cria `BaseEmissaoDiploma` fixando os dados.
- Emissão cria `Diploma` e `DocumentoDiploma`; publica `DiplomaEmitido` e `DocumentoDiplomaCriado`.
- Reemissão gera nova versão do documento e publica `DocumentoDiplomaAtualizado`.
- Consome `AssinaturaConcluida` para atualizar `StatusEmissao` (ex.: emitido-assinado).
