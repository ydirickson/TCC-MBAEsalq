# Servico de Assinatura Eletronica (REST)

CRUD simples para assinaturas seguindo MVC com camada de servico, Spring Boot 4.0.1, Java 21, Spring Data JPA e banco PostgreSQL (rodando via Docker). Nao ha camada de seguranca neste servico por nao fazer parte do escopo do teste.

## Como rodar
Pre-requisitos: JDK 21 e Maven.

```bash
mvn spring-boot:run
```

Por padrao o servico sobe em `http://localhost:8084` e usa o PostgreSQL definido em `.env`/`docker-compose.yml` (`POSTGRES_USER=tcc`, `POSTGRES_PASSWORD=tcc123`, `POSTGRES_DB=tccdb`, porta `5432`). Antes de rodar a aplicacao, suba o banco via `docker compose up -d postgres` na raiz do repositorio.

## Documentacao OpenAPI (Swagger)
Com a aplicacao em execucao:
- OpenAPI JSON: `http://localhost:8084/v3/api-docs`
- Swagger UI: `http://localhost:8084/swagger-ui/index.html`

## Endpoints principais
### Usuarios assinantes (`/usuarios-assinantes`)
- `POST /usuarios-assinantes` — cria usuario assinante
  ```json
  {
    "pessoaId": 1001,
    "email": "assinante@exemplo.com",
    "ativo": true,
    "dataCadastro": "2025-03-01"
  }
  ```
- `GET /usuarios-assinantes`
- `GET /usuarios-assinantes/{id}`
- `PUT /usuarios-assinantes/{id}`
- `DELETE /usuarios-assinantes/{id}`

### Documentos assinaveis (`/documentos-assinaveis`)
- `POST /documentos-assinaveis` — cria documento assinavel
  ```json
  {
    "documentoDiplomaId": 1,
    "descricao": "Diploma versao final",
    "dataCriacao": "2025-03-10T10:00:00"
  }
  ```
- `GET /documentos-assinaveis`
- `GET /documentos-assinaveis/{id}`
- `PUT /documentos-assinaveis/{id}`
- `DELETE /documentos-assinaveis/{id}`

### Solicitacoes (`/solicitacoes-assinatura`)
- `POST /solicitacoes-assinatura` — cria solicitacao
  ```json
  {
    "documentoAssinavelId": 1,
    "status": "PENDENTE",
    "dataSolicitacao": "2025-03-10T10:30:00",
    "dataConclusao": null
  }
  ```
- `GET /solicitacoes-assinatura`
- `GET /solicitacoes-assinatura/{id}`
- `PUT /solicitacoes-assinatura/{id}`
- `DELETE /solicitacoes-assinatura/{id}`

### Assinaturas (`/assinaturas`)
- `POST /assinaturas` — registra assinatura
  ```json
  {
    "solicitacaoId": 1,
    "usuarioAssinanteId": 2,
    "status": "ASSINADA",
    "dataAssinatura": "2025-03-10T11:00:00",
    "motivoRecusa": null
  }
  ```
- `GET /assinaturas`
- `GET /assinaturas/{id}`
- `PUT /assinaturas/{id}`
- `DELETE /assinaturas/{id}`

### Manifestos (`/manifestos-assinatura`)
- `POST /manifestos-assinatura` — cria manifesto
  ```json
  {
    "solicitacaoId": 1,
    "auditoria": "Assinaturas completas",
    "carimboTempo": "2025-03-10T11:05:00",
    "hashFinal": "hash-final"
  }
  ```
- `GET /manifestos-assinatura`
- `GET /manifestos-assinatura/{id}`
- `PUT /manifestos-assinatura/{id}`
- `DELETE /manifestos-assinatura/{id}`
