# Conceitos Compartilhados

## Entidades Comuns
- Pessoa: `id`, nome, data nascimento, nome social (opcional)
- DocumentoIdentificação: tipo (CPF/RG/Passaporte), número
- Contato: email, telefone (opcionais)
- Endereço: logradouro, cidade, UF, CEP
- VínculoAcadêmico: `id`, `pessoaId`, `cursoId`, `cursoCodigo`, `cursoNome`, `tipoCursoPrograma`, `tipoVinculo`, `dataIngresso`, `dataConclusao` (opcional), `situacao`
- DocumentoBase: `documentoId`, tipo, hash, versão, localizacao (metadados)
- DocumentoOficial: `id`, `origemServico`, `origemId`, `pessoaId`, `tipoDocumento`, `dataEmissao`, `versao`, `urlArquivo`, `hashDocumento`

## Eventos Canônicos (Kafka)
- PessoaCriada, PessoaAtualizada
- VinculoAcademicoCriado, VinculoAcademicoAtualizado
- ConclusaoPublicada
- DiplomaEmitido, DocumentoDiplomaCriado, DocumentoDiplomaAtualizado
- DocumentoOficialCriado, DocumentoOficialAtualizado
- SolicitacaoAssinaturaCriada, SolicitacaoAssinaturaCancelada
- AssinaturaParcial, AssinaturaConcluida, AssinaturaRejeitada

## Regras Gerais
- Todas as entidades comuns acima existem com o mesmo esquema em todos os serviços.
- Identificadores numéricos (long) para pessoa e vínculo; evitar chaves compostas.
- Consumidores aplicam atualizações de forma idempotente por id + versão ou timestamp.
