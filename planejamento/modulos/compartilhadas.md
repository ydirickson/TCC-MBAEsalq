# Conceitos Compartilhados

## Entidades Comuns
- Pessoa: `id`, nome, data nascimento, nome social (opcional)
- DocumentoIdentificacao: tipo (CPF/RG/Passaporte), número
- Contato: email, telefone (opcionais)
- Endereco: logradouro, cidade, UF, CEP
- VinculoAcademico: `id`, `pessoaId`, `cursoId`, `cursoCodigo`, `cursoNome`, `tipoCursoPrograma`, `tipoVinculo`, `dataIngresso`, `dataConclusao` (opcional), `situacao`
- DocumentoBase: `documentoId`, tipo, hash, versão, localizacao (metadados)

## Eventos Canônicos (Kafka)
- PessoaCriada, PessoaAtualizada
- VinculoAcademicoCriado, VinculoAcademicoAtualizado
- ConclusaoPublicada
- DiplomaEmitido, DocumentoDiplomaCriado, DocumentoDiplomaAtualizado
- AssinaturaParcial, AssinaturaConcluida, AssinaturaRejeitada

## Regras Gerais
- Todas as entidades comuns acima existem com o mesmo esquema em todos os serviços.
- Identificadores numéricos (long) para pessoa e vínculo; evitar chaves compostas.
- Consumidores aplicam atualizações de forma idempotente por id + versão ou timestamp.
