# EventHub

O EventHub é uma aplicação Web Full-Stack focada no gerenciamento e planejamento colaborativo de eventos. A plataforma permite que organizadores proponham opções de datas, deleguem tarefas e coordenem decisões com suas equipes em tempo real.

## Arquitetura e Tecnologias

**Backend:**
- Java 21 & Spring Boot 3
- Spring Security (Autenticação Stateless)
- Spring Data JPA (PostgreSQL)
- Spring WebSocket (Comunicação em Tempo Real via STOMP)
- Processamento Assíncrono para Mensageria e Notificações

**Frontend:**
- React 19 & Vite
- TanStack Query (Gerenciamento de Estado e Cache de Requisições)
- Axios (Configurado para manipulação segura de cookies HTTP)
- CSS Modularizado

## Recursos de Segurança Aplicados

- **Proteção contra XSS:** Armazenamento de Tokens JWT via *HttpOnly Cookies*, isolando credenciais de acesso do motor JavaScript do navegador.
- **Prevenção de IDOR:** Implementação de Object-Level Security e extração da identidade do usuário diretamente do contexto de segurança isolado do servidor.
- **Performance e Escalabilidade:** Disparo de notificações e processamento de e-mails via *background jobs* (`@Async`) associados a consultas paginadas (`Pageable`), mitigando estresse e consumo excessivo de memória (OOM) no banco de dados.

## Pré-requisitos

Para executar o projeto em ambiente local, certifique-se de ter os seguintes componentes instalados:
- Docker e Docker Compose
- Node.js (versão 18 ou superior)

## Instruções de Execução

### 1. API e Banco de Dados (Docker)

O banco de dados PostgreSQL e a API Spring Boot são orquestrados em contêineres via Docker Compose. A partir do diretório raiz do repositório, inicie os serviços:

```bash
docker-compose up -d --build
```
Após o processo de build do Maven, a API estará acessível na porta padrão `http://localhost:8080`.

### 2. Aplicação Cliente (React)

O frontend foi desenvolvido como uma Single Page Application e utiliza o servidor de desenvolvimento do Vite. Para evitar incidentes de CORS durante o desenvolvimento, o Vite atua como um proxy reverso e redireciona rotas `/api` para a porta 8080.

Acesse o diretório do frontend, instale as dependências e inicie o ambiente:

```bash
cd frontend
npm install
npm run dev
```
A interface de usuário estará acessível em `http://localhost:5173`.
