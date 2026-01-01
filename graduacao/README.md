# Serviço de Graduação (REST)

CRUD simples para Graduação seguindo MVC com camada de serviço, Spring Boot 3.2.3, Java 21, Spring Data JPA e banco PostgreSQL (rodando via Docker). Não há camada de segurança neste serviço por não fazer parte do escopo do teste.

> Observação: Spring Boot 4 ainda não possui release estável; o scaffold usa 3.2.3 (já compatível com Java 21) e pode ser atualizado assim que a versão 4 estiver disponível.

## Como rodar
Pré-requisitos: JDK 21 e Maven.

```bash
mvn spring-boot:run
```

Por padrão o serviço sobe em `http://localhost:8081` e usa o PostgreSQL definido em `.env`/`docker-compose.yml` (`POSTGRES_USER=tcc`, `POSTGRES_PASSWORD=tcc123`, `POSTGRES_DB=tccdb`, porta `5432`). Antes de rodar a aplicação, suba o banco via `docker compose up -d postgres` na raiz do repositório.

## Endpoints principais
### Cursos (`/cursos`)
- `POST /cursos` — cria curso  
  ```json
  {"codigo": "BCC", "nome": "Ciencia da Computacao", "cargaHoraria": 3200}
  ```
- `GET /cursos` — lista cursos
- `GET /cursos/{id}` — busca curso
- `PUT /cursos/{id}` — atualiza curso
- `DELETE /cursos/{id}` — remove curso

### Alunos (`/alunos`)
- `POST /alunos` — cria aluno (requer um curso já cadastrado)  
  ```json
  {
    "pessoaId": 1001,
    "cursoId": 1,
    "dataIngresso": "2024-03-01",
    "status": "ATIVO"
  }
  ```
- `GET /alunos` — lista alunos
- `GET /alunos/{id}` — busca aluno
- `PUT /alunos/{id}` — atualiza aluno
- `DELETE /alunos/{id}` — remove aluno
