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

- **Java 21** + **Spring Boot 3.5** (Web, Data JPA, Validation, Thymeleaf)
- **MySQL 8** (banco oficial) · H2 em memória (perfil de demonstração/testes)
- **JPA / Hibernate**
- Front-end: **Thymeleaf + Bootstrap 5 + Bootstrap Icons + Chart.js**, responsivo
- Testes: JUnit 5 + MockMvc

## Arquitetura MVC

```
src/main/java/br/com/granacerta
├── model/        → Model: entidades JPA e enums
├── repository/   → acesso a dados (Spring Data JPA)
├── service/      → regras de negócio (CRUD, painel, quiz, simuladores)
├── controller/   → Controller: rotas HTTP (@Controller)
└── config/       → carga de dados de exemplo
src/main/resources
├── templates/    → View: páginas Thymeleaf
└── static/css/   → estilos
```

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

## Rotas de CRUD

Todas as entidades têm o mesmo padrão:

| Método | Rota | Ação |
|---|---|---|
| GET | `/{recurso}` | Listar |
| GET | `/{recurso}/novo` | Formulário de cadastro |
| POST | `/{recurso}` | Criar / atualizar (com validação) |
| GET | `/{recurso}/{id}/editar` | Formulário de edição |
| POST | `/{recurso}/{id}/excluir` | Excluir |

Recursos: `usuarios`, `categorias`, `transacoes`, `metas`, `licoes`, `perguntas`, `tentativas`.

Outras rotas: `/usuarios/{id}` (painel), `/metas/{id}`, `/licoes/{id}`, `/quiz/{licaoId}?usuarioId=`, `/ranking`, `/simuladores/...`.

## Como rodar

### Pré-requisitos
- **JDK 21+** (não precisa instalar Maven: o projeto tem o Maven Wrapper `mvnw`)
- **MySQL 8** (ou Docker)

### 1. Subir o MySQL

Com Docker (mais fácil):
```bash
docker compose up -d
```
Ou use um MySQL já instalado. O banco `granacerta` é criado automaticamente.

### 2. Configurar a conexão (se necessário)

O padrão é `root` / `root` em `localhost:3306`. Para mudar, edite `src/main/resources/application.properties` ou use variáveis de ambiente:

```bash
# Linux/macOS
export DB_USER=seu_usuario DB_PASS=sua_senha
# Windows (PowerShell)
$env:DB_USER="seu_usuario"; $env:DB_PASS="sua_senha"
```

### 3. Rodar
```bash
# Linux/macOS
./mvnw spring-boot:run
# Windows
mvnw.cmd spring-boot:run
```
Acesse **http://localhost:8080**. Na primeira execução o banco é populado com 3 usuários, 12 categorias, 6 meses de transações, metas, 6 lições e 21 perguntas.

### Rodar sem MySQL (demonstração rápida)
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
Os dados ficam em memória e são recriados a cada execução.

### Testes
```bash
./mvnw test
```
Os testes abrem todas as páginas, exercitam o CRUD com validação, o quiz, a regra de exclusão de categorias e conferem os cálculos dos simuladores.

## Aviso

Os simuladores e lições têm fins educativos e não são recomendação de investimento. Conteúdo inspirado no material público do Banco Central do Brasil.
