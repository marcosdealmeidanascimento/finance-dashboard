# 📊 Finance Dashboard API

Este repositório contém o código-fonte do **Finance Dashboard API**, uma aplicação desenvolvida em **Spring Boot** para o gerenciamento de finanças pessoais ou corporativas. A API permite o controle de utilizadores, fluxos de receitas (Incomes), pagamentos/despesas (Payments), geração de resumos financeiros automatizados e notificações por e-mail.

---

## 🛠️ Tecnologias e Ferramentas Utilizadas

O projeto utiliza o ecossistema do Spring framework aliado a práticas modernas de persistência, segurança e conteinerização:

- **Linguagem Principal:** Java (Spring Boot)
- **Segurança:** Spring Security + JWT (JSON Web Tokens) para autenticação/autorização de rotas.
- **Banco de Dados & Migrações:** (`db/V1__initial_schema.sql`, `V2__indexes.sql`) para controlo de versão do banco de dados relacional (Flyway não implementado).
- **Documentação:** OpenAPI / Swagger (`openapi.json` e `OpenApiConfig.java`).
- **Comunicação & Mensageria:** Envio assíncrono de e-mails (`EmailService.java`, `AsyncConfig.java`) utilizando templates HTML dinâmicos.
- **Tarefas Agendadas:** Execução automática de rotinas e balanços (`ScheduledTasks.java`).
- **Mapeamento de Dados:** MapStruct ou similar (`mapper/`) para conversões eficientes entre Entidades e DTOs.
- **Gerenciador de Dependências:** Maven (`pom.xml`, `mvnw`).
- **Infraestrutura:** Docker Compose (`compose.yaml`) para orquestrar serviços auxiliares (como instâncias de Banco de Dados).

---

## 📂 Visão Geral da Estrutura do Projeto

Abaixo está o mapeamento conceitual das principais camadas organizadas na pasta `src/main/java/com/finances/dashboard/`:

```text
├── ⚙️ config       -> Configurações de Segurança (JWT, CORS, WebSecurity) e execução Assíncrona.
├── 🎮 controller   -> Camada de Rest Controllers expondo as rotas HTTP da API.
├── 📦 dto          -> Data Transfer Objects separados rigorosamente entre Requests e Responses.
├── 🏷️ enums        -> Enumeradores de domínio da aplicação (ex: Status de Pagamento).
├── 🚨 exception    -> Tratamento global de erros e exceções customizadas (ex: ResourceNotFound).
├── 🔄 mapper       -> Classes responsáveis por converter modelos de domínio para DTOs e vice-versa.
├── 🗙 model        -> Entidades de domínio mapeadas para tabelas do banco de dados (JPA/Hibernate).
├── 💾 repository   -> Interfaces Spring Data JPA para comunicação fluida com o banco de dados.
├── 💼 service      -> Camada com as regras de negócio centrais e integrações.
└── ⏰ tasks        -> Rotinas agendadas (Cron jobs) executadas em background.