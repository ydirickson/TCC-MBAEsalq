# Serviço de Diplomas (REST)

CRUD simples para Diplomas seguindo MVC com camada de serviço, Spring Boot 4.0.1, Java 21, Spring Data JPA e banco PostgreSQL (rodando via Docker). Não há camada de segurança neste serviço por não fazer parte do escopo do teste.

## Como rodar
Pré-requisitos: JDK 21 e Maven.

```bash
mvn spring-boot:run
```

Por padrão o serviço sobe em `http://localhost:8083` e usa o PostgreSQL definido em `.env`/`docker-compose.yml` (`POSTGRES_USER=tcc`, `POSTGRES_PASSWORD=tcc123`, `POSTGRES_DB=tccdb`, porta `5432`). Antes de rodar a aplicação, suba o banco via `docker compose up -d postgres` na raiz do repositório.

## Documentação OpenAPI (Swagger)
Com a aplicação em execução:
- OpenAPI JSON: `http://localhost:8083/v3/api-docs`
- Swagger UI: `http://localhost:8083/swagger-ui/index.html`

## Endpoints principais
### Requerimentos (`/requerimentos`)
- `POST /requerimentos` — cria requerimento e base de emissão
  ```json
  {
    "pessoaId": 1001,
    "vinculoId": 10,
    "cursoCodigo": "BCC",
    "cursoNome": "Ciência da Computação",
    "cursoTipo": "GRADUACAO",
    "dataConclusao": "2024-12-20",
    "dataColacaoGrau": "2025-02-15",
    "dataSolicitacao": "2025-03-01"
  }
  ```
- `GET /requerimentos` — lista requerimentos
- `GET /requerimentos/{id}` — busca requerimento
- `PUT /requerimentos/{id}` — atualiza requerimento
- `DELETE /requerimentos/{id}` — remove requerimento

### Diplomas (`/diplomas`)
- `POST /diplomas` — cria diploma
  ```json
  {"requerimentoId": 1, "numeroRegistro": "2025-0001", "dataEmissao": "2025-03-10"}
  ```
- `GET /diplomas` — lista diplomas
- `GET /diplomas/{id}` — busca diploma
- `PUT /diplomas/{id}` — atualiza diploma
- `DELETE /diplomas/{id}` — remove diploma
