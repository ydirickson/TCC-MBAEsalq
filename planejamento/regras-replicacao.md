# 5. Definição das intersecções e regras de replicação
[← Voltar ao índice](./README.md)

## 5.1. Pessoa (intersecção principal)
- Pessoa é usada por todos os serviços.
- É necessário definir **onde Pessoa é criada e “mandada”** (owner).
  - Opção A (comum): Graduação e Pós são “fontes” independentes e Assinatura/Diplomas consomem.
  - Opção B: Um serviço “Identidade” central (não será criado neste TCC, mas pode ser simulado).
- No experimento, recomenda-se escolher um owner principal para simplificar medições:
  - Ex.: Graduação como owner primário de Pessoa, Pós cria Pessoa apenas se não existir.

## 5.2. Conclusão / elegibilidade para diploma
- Diplomas precisa saber quando um vínculo (grad/pós) está concluído.
- Graduação e Pós produzem o estado “concluído” (ou equivalente).
- Diplomas consome esse estado para permitir emissão.

## 5.3. Documento de diploma e assinatura
- Diplomas gera **DocumentoDiploma** quando emite.
- Assinatura eletrônica consome o evento de documento gerado para criar **DocumentoAssinavel**.
- Assinatura produz eventos de “assinatura concluída” que Diplomas pode consumir para atualizar status do diploma/documento.
