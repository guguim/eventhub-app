# EventHub

O EventHub é um sistema robusto e completo para **planejamento colaborativo de eventos**. Desenvolvido com uma arquitetura moderna dividida entre Backend (Java/Spring Boot) e Frontend (React), a plataforma resolve a complexidade de organizar encontros, permitindo votação de datas em tempo real, delegação de tarefas, envio de convites e gestão completa de convidados.

## 🌟 Principais Funcionalidades

- **Votação de Datas em Tempo Real:** Integração nativa com WebSockets (STOMP), permitindo que convidados votem nas melhores opções de datas e o painel seja atualizado na tela de todos instantaneamente.
- **Gestão de Tarefas (To-Do):** Sistema embutido de tarefas por evento, com atribuição de responsáveis e controle de status.
- **Sistema de Convites e Notificações:** Envio automatizado de e-mails de convite utilizando `JavaMailSender` processado de forma assíncrona (`@Async`) em background.
- **Dashboard e Histórico:** Painel dinâmico listando os próximos eventos, com paginação e ordenação no banco de dados.
- **Controle de Acesso Granular:** Diferenciação clara entre Organizadores (que podem editar/deletar o evento) e Convidados.

---

## 🏗️ Arquitetura e Segurança (Backend)

O núcleo do sistema foi desenhado visando performance, manutenibilidade e segurança:

- **Java 21 & Spring Boot 3:** Uso da stack mais recente e performática do ecossistema Java.
- **Segurança e Prevenção de IDOR:** Validação de acesso a nível de objeto (Object-Level Security). Um usuário jamais consegue alterar eventos ou tarefas de terceiros manipulando IDs na URL.
- **JWT em HttpOnly Cookies:** Prevenção absoluta contra ataques XSS. O token de sessão transita via Cookies criptografados (`HttpOnly`) blindando o acesso via JavaScript malicioso.
- **Processamento Assíncrono e em Lote (Prevenção de OOM):** Tarefas pesadas (como buscar milhares de usuários e enviar e-mails em massa) utilizam a anotação `@Async` rodando em threads separadas, e o banco é consultado em lotes via `Pageable`.
- **Tratamento Global de Exceções:** Padrão `@RestControllerAdvice` e `ProblemDetail` para padronizar respostas de erro de negócio (ex: 404 Not Found, 403 Forbidden).
- **Domain-Driven Design (DTOs):** Total isolamento entre as Entidades do Banco de Dados (`JPA`) e a camada de apresentação usando *Records* e DTOs, evitando vazamento de dados sensíveis.
- **Cobertura de Testes:** Lógica de negócios coberta por Testes Unitários utilizando **JUnit 5** e **Mockito** (ex: `EventServiceTest`, `TaskServiceTest`).

---

## 🎨 Frontend (React)

A interface de usuário foi pensada para ser limpa, rápida e responsiva.

- **React 19 & Vite:** Ecossistema rápido e moderno.
- **TanStack Query (React Query):** Motor profissional de gerenciamento de estado assíncrono. Garante cache de requisições, retries automáticos, e evita *race conditions* ou renderizações desnecessárias ao navegar pela aplicação.
- **Axios Interceptors:** Configuração centralizada que captura erros `401 Unauthorized` e renova sessões/redireciona o usuário de forma inteligente, lidando perfeitamente com a estratégia de envio automático das credenciais (Cookies).

---

## ⚙️ Como Rodar o Projeto

A aplicação está dockerizada para facilitar a inicialização. 

### Pré-requisitos
- **Docker** e **Docker Compose**
- **Node.js** (Necessário APENAS para baixar as dependências e compilar o código do Frontend React na pasta `/frontend`).

### 1. Iniciar Banco de Dados e API (Backend)
No diretório raiz do projeto, execute o comando abaixo para compilar o código Java e subir o contêiner do PostgreSQL e do Spring Boot:
```bash
docker-compose up -d --build
```
> O backend ficará disponível em `http://localhost:8080`.

### 2. Iniciar a Aplicação Cliente (Frontend)
Abra um novo terminal, entre na pasta do frontend, instale os pacotes (via Node) e inicie o servidor local (Vite):
```bash
cd frontend
npm install
npm run dev
```
> Acesse o sistema no navegador através de `http://localhost:5173`. O Vite já está configurado como um Proxy reverso para redirecionar as chamadas da API sem gerar erros de CORS.
