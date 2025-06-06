# Loja Virtual

Este repositório contém uma aplicação completa de **Loja Virtual**, composta por:

- **Frontend**: SPA em **Angular 19**, empacotada e servida por **NGINX**.
- **Backend**: API RESTful em **Spring Boot** (Java 21), conectada a um banco de dados **MySQL**.
- **Banco de Dados**: **MySQL 8.0** (rodando em container Docker).

Abaixo estão as instruções para configurar, executar e testar localmente toda a stack.

---

## Índice

1. [Visão Geral do Projeto](#visão-geral-do-projeto)
2. [Tecnologias Utilizadas](#tecnologias-utilizadas)
3. [Pré-requisitos](#pré-requisitos)
4. [Estrutura de Pastas](#estrutura-de-pastas)
5. [Configuração de Ambiente](#configuração-de-ambiente)
   - 5.1. Variáveis de Ambiente do Backend
   - 5.2. Configurações do Frontend
6. [Executando com Docker Compose](#executando-com-docker-compose)
7. [Instalação Manual (sem Docker)](#instalação-manual-sem-docker)
   - 7.1. Backend (Spring Boot)
   - 7.2. Frontend (Angular 19)
   - 7.3. Banco de Dados MySQL
8. [Executando Testes](#executando-testes)
9. [Endpoints Principais](#endpoints-principais)
10. [Considerações Finais](#considerações-finais)

---

## Visão Geral do Projeto

A **Loja Virtual** é um sistema de e-commerce simplificado, dividido em duas partes:

1. **Backend (API REST)**

   - Desenvolvido em Spring Boot 3.5.0
   - Autenticação e autorização via JWT
   - Endpoints para gerenciar usuários, autenticação, produtos e pedidos
   - Camada de persistência usando Spring Data JPA e MySQL

2. **Frontend (SPA em Angular 19)**

   - Desenvolvido em Angular 19
   - Consome a API Spring Boot para operações de login, cadastro e CRUD de produtos
   - Empacotado em um container NGINX para servir o build de produção

3. **Banco de Dados (MySQL)**
   - Versão 8.0
   - Container Docker rodando uma instância MySQL com schema `ecommerce`
   - Usuário `root` (senha configurável via `docker-compose`)

---

## Tecnologias Utilizadas

- **Backend**

  - Java 21
  - Spring Boot 3.5.0 (Spring Web, Spring Data JPA, Spring Security)
  - Hibernate ORM 6.6.15
  - MySQL Connector/J 8.0
  - JWT (JSON Web Tokens) para autenticação

- **Frontend**

  - Angular 19
  - TypeScript
  - RxJS
  - NGINX (versão Alpine) para servir o build

- **Infraestrutura & Build**
  - Docker 24.x
  - Docker Compose 2.x
  - Maven 3.9.x (para o backend)
  - Node.js 18.x + npm (para o frontend)

---

## Pré-requisitos

Antes de rodar o projeto, tenha instalado em sua máquina:

1. **Git** (para clonar o repositório)
2. **Docker & Docker Compose** (versões recentes)
   - No Windows/Mac: Docker Desktop com suporte a WSL2 (caso use Windows).
   - No Linux: Docker Engine + Docker Compose Plugin.
3. **Java JDK 21** (caso queira executar o backend sem Docker)
4. **Maven 3.9.x** (caso queira buildar/executar manualmente o backend)
5. **Node.js 18.x** e **npm** (caso queira buildar/executar manualmente o frontend)

> **Observação**: Se você pretende rodar tudo via Docker Compose, Java, Maven, Node e npm não são obrigatórios localmente — apenas Docker e Compose.

---

## Estrutura de Pastas

Ao clonar o projeto, você verá:

```
loja-virtual/
├── backend/                       # Código-fonte do backend (Spring Boot)
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/                      # Código-fonte do frontend (Angular 19)
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── docker-compose.yml             # Orquestração dos containers (db, backend, frontend)
└── README.md                      # Este arquivo
```

- **backend/**  
  Contém todo o projeto Spring Boot:

  - `src/main/java/...` → pacotes de controllers, serviços, repositórios, configurações de segurança.
  - `src/main/resources/application.properties` → configurações de conexão MySQL (ajustáveis por variáveis de ambiente no Docker Compose).
  - `src/test/java/...` → testes de integração (usando H2 ou Testcontainers se configurado).
  - `Dockerfile` → imagem que empacota o JAR executável e roda no Tomcat embutido.

- **frontend/**  
  Contém todo o projeto Angular 19:

  - `src/app/` → componentes, serviços, módulos e rotas.
  - `angular.json`, `tsconfig.json`, `package.json` → configurações do Angular.
  - `Dockerfile` → etapa de build (Node) + etapa de runtime (NGINX) que copia o `dist/` para `/usr/share/nginx/html`.

- **docker-compose.yml**  
  Orquestra:
  - Serviço **db**: roda MySQL 8.0, inicializa o schema `ecommerce`.
  - Serviço **backend**: builda a imagem Spring Boot e expõe a porta 8080.
  - Serviço **frontend**: builda o Angular e expõe a porta 4200 (mapeada para o NGINX interno 80).

---

## Configuração de Ambiente

### 5.1. Variáveis de Ambiente do Backend

O Spring Boot “pega” as variáveis de conexão ao banco por meio de _environment variables_ no Docker Compose. No `application.properties` temos algo como:

```properties
# Exemplo (em src/main/resources/application.properties):
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

No `docker-compose.yml`, o serviço **backend** sobrescreve essas variáveis:

```yaml
services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/ecommerce?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: 1234
      SPRING_DATASOURCE_DRIVER_CLASS_NAME: com.mysql.cj.jdbc.Driver
    ports:
      - "8080:8080"
    depends_on:
      db:
        condition: service_healthy
    networks:
      - app-network
```

#### Variáveis importantes:

- `SPRING_DATASOURCE_URL`: URL JDBC para conectar ao container `db`.
- `SPRING_DATASOURCE_USERNAME`: usuário do MySQL (ex.: `root`).
- `SPRING_DATASOURCE_PASSWORD`: senha do MySQL (ex.: `1234`).
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`: driver JDBC (padrão `com.mysql.cj.jdbc.Driver`).

Você pode alterar esses valores diretamente no `docker-compose.yml` se quiser outro usuário/senha ou outra porta.

---

### 5.2. Configurações do Frontend

No projeto Angular, existe um arquivo `environment.ts` (em `frontend/src/environments/`) que define a URL base para a API. Exemplo:

```ts
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: "http://localhost:8080/api",
};
```

E, no `environment.prod.ts` (para build de prod):

```ts
// src/environments/environment.prod.ts
export const environment = {
  production: true,
  apiUrl: "/api", // Quando servido pelo NGINX, as chamadas serão relativas
};
```

> **Importante**: Se você rodar o Angular via `ng serve`, mantenha `apiUrl: 'http://localhost:8080/api'`.  
> Se for buildar para produção (e servir via NGINX), use `/api` ou a rota apropriada que configure no NGINX para redirecionar `/api` ao backend.

---

## Executando com Docker Compose

### Passo a Passo

1. **Clone o repositório** (caso ainda não tenha feito):

   ```bash
   git clone https://github.com/victormoni/loja-virtual.git
   cd loja-virtual
   ```

2. **Remova versões antigas e garanta que não há containers conflitantes**:

   ```bash
   docker compose down --remove-orphans
   ```

3. **Suba todos os serviços** (MySQL, backend e frontend) em modo destacado (detached):

   ```bash
   docker compose up -d --build
   ```

   - `--build` força o rebuild das imagens (`backend` e `frontend`), garantindo que o código mais recente seja empacotado.

4. **Acompanhe os logs (opcional)**:

   ```bash
   docker compose logs -f
   ```

   Em breve, você deverá ver algo como:

   - O MySQL inicializando e ficando “ready for connections”.
   - O backend Spring Boot conectando ao banco e iniciando Tomcat em `Port 8080`.
   - O frontend NGINX servindo o build Angular em `Port 80` dentro do container, mapeado para a porta `4200` do host.

5. **Acesse a aplicação**:
   - **Frontend**: abra o navegador em
     ```
     http://localhost:4200
     ```
   - **Backend (API)**: você pode testar via Postman ou curl, por exemplo:
     ```
     GET http://localhost:8080/actuator/health
     ```
     deve retornar status 200 e JSON com o estado de saúde do Spring (`{"status":"UP"}`).

### Parando os Containers

Para parar e remover todos os containers da stack:

```bash
docker compose down
```

Isso encerra todos os serviços e libera as portas (3306, 8080, 4200).

---

## Instalação Manual (sem Docker)

Caso você prefira executar cada parte separadamente (por exemplo, para depuração local), siga os passos abaixo.

### 7.1. Backend (Spring Boot)

1. **Configurar MySQL localmente**

   - Instale o MySQL 8.0 no seu computador ou em outra máquina acessível.
   - Crie um banco de dados chamado `ecommerce`:
     ```sql
     CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     ```
   - Certifique-se de que exista um usuário `root` (ou outro de sua preferência) com senha. Exemplo:
     ```sql
     ALTER USER 'root'@'localhost' IDENTIFIED BY '1234';
     ```

2. **Ajustar `application.properties`**  
   Em `backend/src/main/resources/application.properties`, configure:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=1234
   ```

3. **Buildar e rodar com Maven**  
   Abra um terminal na pasta `backend/` e execute:

   ```bash
   mvn clean package -DskipTests
   java -jar target/ecommerce-0.0.1-SNAPSHOT.jar
   ```

   Ou, se quiser rodar diretamente sem empacotar:

   ```bash
   mvn spring-boot:run
   ```

4. **Verificar se o backend subiu**  
   Acesse no navegador ou via curl:
   ```
   http://localhost:8080/actuator/health
   ```
   Deve retornar status `UP`.

---

### 7.2. Frontend (Angular 19)

1. **Instalar dependências Node.js**  
   Abra um terminal na pasta `frontend/` e execute:

   ```bash
   npm install
   ```

2. **Rodar o servidor de desenvolvimento**  
   Para modo dev com hot reload:

   ```bash
   ng serve --open
   ```

   - O comando `ng serve` levantará o servidor em `http://localhost:4200`.
   - Ele “escutará” mudanças nos arquivos e recarregará automaticamente o navegador.

3. **Build de produção**  
   Quando quiser gerar a versão de produção (minificada, otimizada):
   ```bash
   ng build --configuration production
   ```
   - Isso criará a pasta `frontend/dist/frontend/`, contendo `index.html`, `main.js`, estilos, etc.
   - Você pode servir esses arquivos com um servidor HTTP (por exemplo, NGINX, Apache, ou `http-server` do npm).

---

### 7.3. Banco de Dados MySQL

Caso não use Docker, instale e configure manualmente:

1. **Instalação**

   - **Windows**: baixe o MySQL Installer em [dev.mysql.com](https://dev.mysql.com/downloads/installer/) e siga os passos.
   - **macOS**: use Homebrew:
     ```bash
     brew install mysql@8.0
     brew services start mysql@8.0
     ```
   - **Linux (Ubuntu/Debian)**:
     ```bash
     sudo apt update
     sudo apt install mysql-server
     sudo systemctl start mysql
     ```

2. **Criar usuário/banco**  
   Acesse via client CLI ou Workbench:

   ```sql
   CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'root'@'localhost' IDENTIFIED BY '1234';
   GRANT ALL PRIVILEGES ON ecommerce.* TO 'root'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. **Ajustar o `application.properties` do backend** (conforme mostrado no tópico 7.1).

---

## Executando Testes

### 8.1. Testes de Unidade/Integração do Backend

O projeto backend já inclui testes de integração em `src/test/java/com/victormoni/ecommerce/integration/AuthIntegrationTest.java`. Eles validam fluxos de registro, login e obtenção de dados do usuário autenticado.

#### Com Maven

1. **Certifique-se de ter configurado H2 para teste**

   - Em `src/test/resources/application-test.properties`, estão apontando para H2 (in-memory).
   - As classes de teste usam `@ActiveProfiles("test")`, de modo que o Spring Boot roda H2 sem tentar se conectar ao MySQL real.

2. **Executar todos os testes**:

   ```bash
   cd backend
   mvn test
   ```

3. **Executar testes específicos**:
   ```bash
   mvn -Dtest=AuthIntegrationTest test
   ```
   Ou:
   ```bash
   mvn -Dtest=AuthIntegrationTest#shouldRegisterAndLoginSuccessfully test
   ```

### 8.2. Testes do Frontend

Se você escreveu testes unitários ou de integração para componentes Angular (ex.: com Jasmine/Karma ou Cypress), basta rodar:

1. **Testes unitários (Karma/Jasmine)**:

   ```bash
   cd frontend
   ng test
   ```

   - Isso abrirá o navegador com o runner de testes Karma.

2. **Testes end-to-end (Protractor/Cypress)**:
   - Caso tenha configurado Protractor, rode:
     ```bash
     ng e2e
     ```
   - Se usar Cypress, então:
     ```bash
     npm run cypress:open
     ```

_(Se não houver testes configurados para o frontend, ignore esta seção.)_

---

## Endpoints Principais

A API REST do backend expõe, entre outros:

- **Autenticação / Registro**

  - `POST /api/auth/register`
    - Request (JSON):
      ```json
      {
        "username": "joao",
        "password": "senha123",
        "role": "USER"
      }
      ```
    - Response: 200 OK (ou 4xx em caso de erro).
  - `POST /api/auth/login`
    - Request (JSON):
      ```json
      {
        "username": "joao",
        "password": "senha123"
      }
      ```
    - Response (JSON):
      ```json
      {
        "accessToken": "<jwt>",
        "refreshToken": "<jwt_refresh>"
      }
      ```

- **Refreshing Token**

  - `POST /api/auth/refresh`
    - Request (JSON):
      ```json
      {
        "refreshToken": "<jwt_refresh>"
      }
      ```
    - Response (JSON):
      ```json
      {
        "accessToken": "<novo_jwt_access>",
        "refreshToken": "<mesmo_refresh>"
      }
      ```

- **Usuário Logado**

  - `GET /api/users/me`
    - Header: `Authorization: Bearer <accessToken>`
    - Response (JSON):
      ```json
      {
        "id": 5,
        "username": "joao",
        "role": "USER"
      }
      ```

- **Produtos**
  - `GET /api/products` → listar todos (público)
  - `GET /api/products/{id}` → obter produto por ID (público)
  - `POST /api/products` → criar produto (somente ADMIN)
  - `PUT /api/products/{id}` → atualizar produto (ADMIN)
  - `DELETE /api/products/{id}` → deletar produto (ADMIN)

_(Documentação OpenAPI disponível em `/swagger-ui.html` e `/v3/api-docs` se você habilitou o Swagger.)_

---

## Considerações Finais

- **Banco em Produção**:  
  Em um ambiente real, nunca use `MYSQL_ALLOW_EMPTY_PASSWORD` nem exponha `root` sem senha.  
  Configure um usuário específico de aplicação com direitos limitados e armazene as credenciais em variáveis de ambiente seguras (Vault, AWS Secrets, Azure Key Vault, etc.).

- **HTTPS no Frontend**:  
  Para produção, configure o NGINX (ou outra camada reverse proxy) para servir via HTTPS e redirecionar o tráfego API para o backend adequadamente.

- **Ambientes Diferentes**:

  - `environment.ts` (desenvolvimento): aponta para `http://localhost:8080/api`.
  - `environment.prod.ts` (produção): se `apiUrl = '/api'`, configure o NGINX para rotear `/api` ao backend.

- **Possíveis Expansões**:
  - Adicionar paginação nos endpoints de produtos.
  - Implementar upload de imagens, carrinho de compras e checkout.
  - Deploy em Kubernetes ou outro serviço de orquestração.

---

Obrigado por usar a **Loja Virtual**!  
Se tiver dúvidas ou sugestões, abra uma [issue](https://github.com/victormoni/loja-virtual/issues) ou envie um PR.

**Licença:** MIT License (consulte o arquivo [LICENSE](LICENSE) para mais detalhes)  
**Autor:** Victor Moni
