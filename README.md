# 🗓️ EventHub

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white" />
</p>

O **EventHub** é uma aplicação Full-Stack completa para gerenciamento e planejamento colaborativo de eventos. Desenvolvida do zero com foco em arquitetura limpa, segurança e alta reatividade, a plataforma permite que organizadores sugiram opções de datas, deleguem tarefas e tomem decisões com a sua equipe em tempo real.

## ✨ Funcionalidades

- **Autenticação Segura:** Login e Cadastro protegidos com JWT (JSON Web Tokens) e senhas criptografadas (BCrypt).
- **Enquetes de Datas em Tempo Real:** Votação dinâmica para escolha do dia do evento com atualização imediata para todos os usuários conectados na sala (via WebSockets/STOMP).
- **Gestão de Tarefas (Checklist):** Delegação e conclusão de tarefas com atualização instantânea e proteção a nível de domínio (Object-Level Security).
- **Notificações Inteligentes:** Sistema de *in-app notifications* no sino do menu e alertas enviados por E-mail usando o Spring Mail (SMTP).
- **Interface Premium:** Frontend fluido construído com React e Vite, utilizando um Design System puro baseado no efeito visual *Glassmorphism*.

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 21 & Spring Boot 3:** Base sólida com suporte a Records para transferência de dados.
- **Spring Security:** Proteção das rotas API (Stateless) e autorização baseada em cargos (Role-based).
- **Spring Data JPA & Hibernate:** ORM robusto para comunicação com o banco de dados.
- **Spring WebSocket:** Túnel bidirecional em tempo real para os navegadores dos usuários.
- **Spring Mail:** Disparo de e-mails de notificação assíncronos.

### Frontend
- **React + Vite:** Single Page Application (SPA) ultrarrápida.
- **Axios:** Gerenciador de requisições HTTP configurado com Interceptors para injeção automática do Token em todas as chamadas.
- **Context API:** Gerenciamento global do estado de autenticação pelo LocalStorage.
- **Lucide React:** Biblioteca de ícones modernos e minimalistas.

### Infraestrutura
- **PostgreSQL:** Banco de dados relacional oficial do projeto.
- **Docker & Docker Compose:** Todo o ecossistema (banco + API em Java) empacotado em containers isolados para deploy e inicialização em 1 clique.

## 🚀 Como executar o projeto localmente

Para rodar essa aplicação na sua máquina, você precisará ter o **Docker**, o **Docker Compose** e o **Node.js** instalados.

### 1. Subindo o Backend e o Banco de Dados
A raiz do projeto contém um arquivo `docker-compose.yml` que sobe o PostgreSQL e constrói o servidor Java simultaneamente.
```bash
# Na raiz do projeto, execute:
docker-compose up -d --build
```
A API estará rodando internamente em `http://localhost:8080`.

### 2. Rodando o Frontend
O código do React fica dentro da pasta `/frontend`.
```bash
# Entre na pasta do frontend
cd frontend

# Instale as dependências
npm install

# Inicie o servidor de desenvolvimento
npm run dev
```
O Frontend estará rodando em `http://localhost:5173`. O Vite já está configurado com um *Proxy* inteligente para evitar erros de CORS durante o desenvolvimento (ele roteia as chamadas `/api` para a porta `8080` do Java magicamente).

## 🔒 Segurança em Foco

Este projeto demonstra implementações cruciais de segurança de nível empresarial:
1. **Stateless Authentication:** O Backend não guarda a "sessão" do usuário na memória (evitando gasto de RAM). Todas as requisições privadas são validadas descriptografando o Token JWT em tempo real.
2. **Object-Level Security:** Mesmo com o token JWT em mãos, um usuário tentar manipular uma requisição via terminal só terá sucesso se o ID dele bater com o ID do Organizador (ou responsável da tarefa) gravado no banco de dados. 

---
*Projeto desenvolvido como demonstração de excelência em Engenharia de Software Full-Stack.*
