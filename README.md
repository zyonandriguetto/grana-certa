# 💸 GranaCerta: educação financeira para jovens

Projeto da **Atividade Avaliativa A3: Projeto Final em Spring Boot Java** (UniCuritiba).

## O problema

Na seção [Meu BC – Perguntas Frequentes](https://www.bcb.gov.br/meubc/faqs), o Banco Central do Brasil reúne dúvidas sobre orçamento, juros, cartão de crédito, empréstimos, Pix e golpes. O tema escolhido foi **Educação financeira para jovens**: muita gente chega à vida adulta sem saber montar um orçamento, cai no rotativo do cartão e não reconhece os golpes mais comuns com Pix.

## A solução

O **GranaCerta** junta três frentes:

| Frente | O que o jovem faz |
|---|---|
| **Prática** | Registra mesada/salário e gastos, vê para onde foi o dinheiro, acompanha a **regra 50-30-20** aplicada aos próprios gastos, recebe um **score de saúde financeira** e dicas automáticas, e cria **metas** com depósitos e valor mensal sugerido. |
| **Teoria gamificada** | Segue uma **trilha de lições** baseadas nos temas do Meu BC (orçamento, juros compostos, cartão e rotativo, Pix e golpes, empréstimos/CET/Registrato, reserva de emergência), responde **quizzes** com correção comentada, ganha pontos e aparece no **ranking**. |
| **Simuladores** | Juros compostos, **rotativo do cartão** (com o teto de 100% da Lei 14.690/2023) e **à vista x parcelado** (calcula os juros escondidos no "sem juros"). |

## Tecnologias

- **Java 21** + **Spring Boot 3.5** (Web, Data JPA, Validation)
- **API REST** em JSON (`@RestController`), erros no formato RFC 9457 (`application/problem+json`)
- **PostgreSQL no Supabase** (banco oficial) · H2 em memória (perfil de demonstração/testes)
- **JPA / Hibernate**
- Testes: JUnit 5 + MockMvc

## Arquitetura

```
src/main/java/br/com/granacerta
├── model/        → entidades JPA e enums
├── repository/   → acesso a dados (Spring Data JPA)
├── service/      → regras de negócio (CRUD, painel, quiz, simuladores)
├── dto/          → contratos JSON: Request (entrada validada) e Response (saída)
├── controller/   → @RestController: rotas HTTP + TratadorDeErros
└── config/       → carga de dados de exemplo
supabase/         → script SQL de segurança (RLS) para rodar no Supabase
```

Os controllers não expõem as entidades JPA diretamente (elas têm referências circulares, como `Usuario ↔ Transacao ↔ Meta`): entram `*Dto.Request` e saem `*Dto.Response`.

## Entidades (7, todas relacionadas)

```
Usuario 1───N Transacao N───1 Categoria
   │              │
   │              N
   │              │ (depósito opcional)
   ├──────1───N Meta
   │
   └──────1───N Tentativa N───1 Licao 1───N Pergunta
```

| Entidade | Descrição |
|---|---|
| `Usuario` | Jovem que usa a plataforma (nome, e-mail, nascimento, renda/mesada, avatar) |
| `Categoria` | Tipo de receita ou despesa + classificação 50-30-20 (necessidade, desejo, poupança) |
| `Transacao` | Entrada/saída de dinheiro; pode ser marcada como compra por impulso e ligada a uma meta |
| `Meta` | Objetivo de poupança com valor e prazo; progresso calculado pelos depósitos |
| `Licao` | Conteúdo educativo da trilha (tema, nível, texto, fonte) |
| `Pergunta` | Questão de múltipla escolha (A–D) com explicação |
| `Tentativa` | Resultado de um usuário em um quiz (acertos, pontos, data) |

## Rotas da API

Todas as entidades seguem o mesmo padrão REST:

| Método | Rota | Ação | Resposta |
|---|---|---|---|
| GET | `/api/{recurso}` | Listar | `200` |
| GET | `/api/{recurso}/{id}` | Buscar um | `200` |
| POST | `/api/{recurso}` | Criar | `201` + cabeçalho `Location` |
| PUT | `/api/{recurso}/{id}` | Atualizar | `200` |
| DELETE | `/api/{recurso}/{id}` | Excluir | `204` |

Recursos: `usuarios`, `categorias`, `transacoes`, `metas`, `licoes`, `perguntas`, `tentativas`.

Filtros: `/api/transacoes?usuarioId=`, `/api/metas?usuarioId=`, `/api/perguntas?licaoId=`.

Outras rotas:

| Rota | O que devolve |
|---|---|
| `GET /` ou `GET /api` | Resumo da API com o total de registros (serve de health check) |
| `GET /api/ranking` | Usuários ordenados pela pontuação nos quizzes |
| `GET /api/usuarios/{id}/painel?mes=2026-11` | Painel financeiro do mês (padrão: mês atual) |
| `GET /api/quiz/{licaoId}` | Perguntas da lição, **sem** o gabarito |
| `POST /api/quiz/{licaoId}` | Corrige, grava a tentativa e devolve o gabarito comentado |
| `GET /api/simuladores` | Lista os simuladores |
| `GET /api/simuladores/juros-compostos?inicial=&mensal=&taxa=&meses=` | Juros compostos |
| `GET /api/simuladores/cartao-rotativo?divida=&taxa=&meses=` | Rotativo do cartão |
| `GET /api/simuladores/parcelado-ou-avista?preco=&desconto=&parcelas=&rendimento=` | À vista x parcelado |

### Erros

| Status | Quando | Corpo |
|---|---|---|
| `400` | Validação falhou, JSON ilegível ou parâmetro inválido | `{ "title": "Dados inválidos", "erros": { "campo": "mensagem" } }` |
| `404` | Registro não existe | `{ "title": "Registro não encontrado", "detail": "..." }` |
| `409` | E-mail já cadastrado ou violação de integridade no banco | `{ "title": "...", "detail": "..." }` |
| `422` | Regra de negócio (ex.: excluir categoria que tem transações) | `{ "title": "Regra de negócio", "detail": "..." }` |

### Exemplos

```bash
# criar usuário
curl -i -X POST http://localhost:8080/api/usuarios -H "Content-Type: application/json" \
  -d '{"nome":"Ana","email":"ana@x.com","dataNascimento":"2008-03-10","rendaMensal":500}'

# responder um quiz (chaves = id da pergunta, valores = letra marcada)
curl -X POST http://localhost:8080/api/quiz/1 -H "Content-Type: application/json" \
  -d '{"usuarioId":1,"respostas":{"1":"A","2":"C","3":"B"}}'
```

## Como rodar

### Pré-requisitos
- **JDK 21+** (não precisa instalar Maven: o projeto tem o Maven Wrapper `mvnw`)
- Um projeto no **[Supabase](https://supabase.com)**

### 1. Conectar no Supabase

1. No painel do projeto, clique em **Connect** e escolha **Session pooler**.
2. Copie `.env.example` para `.env` (o `.env` não vai para o git) e preencha com os dados do painel:

```properties
DB_URL=jdbc:postgresql://aws-0-REGIAO.pooler.supabase.com:5432/postgres?sslmode=require
DB_USER=postgres.SEU_PROJECT_REF
DB_PASS=sua_senha_do_banco
```

Também funciona definindo `DB_URL`, `DB_USER` e `DB_PASS` como variáveis de ambiente (elas têm prioridade sobre o `.env`).

> **Por que o Session pooler?** A conexão direta (`db.<ref>.supabase.co`) é só IPv6 no plano gratuito. O Session pooler funciona em redes IPv4 e aceita prepared statements. Se preferir o Transaction pooler (porta `6543`), acrescente `&prepareThreshold=0` na `DB_URL`.

### 2. Rodar
```bash
# Linux/macOS
./mvnw spring-boot:run
# Windows
mvnw.cmd spring-boot:run
```
Acesse **http://localhost:8080/api**. Na primeira execução as tabelas são criadas pelo Hibernate (`ddl-auto=update`) e o banco é populado com 3 usuários, 12 categorias, 6 meses de transações, metas, 6 lições e 21 perguntas.

### 3. Proteger as tabelas (uma vez só)

Tabelas criadas por ORM no schema `public` do Supabase nascem **sem Row Level Security**, e a chave `anon` do Supabase é pública. Depois da primeira execução, rode o conteúdo de [`supabase/habilitar-rls.sql`](supabase/habilitar-rls.sql) no **SQL Editor** do Supabase. A aplicação não é afetada, porque conecta como `postgres` (dono das tabelas).

### Rodar sem Supabase (demonstração rápida)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
Os dados ficam em memória e são recriados a cada execução. Não precisa de `.env`.

### Testes
```bash
./mvnw test
```
Os testes usam o perfil `h2` e cobrem todos os endpoints de leitura, o CRUD com validação, o formato dos erros, o quiz, as regras de negócio e os cálculos dos simuladores.

## Aviso

Os simuladores e lições têm fins educativos e não são recomendação de investimento. Conteúdo inspirado no material público do Banco Central do Brasil.
