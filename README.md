# Loja Virtual

Este repositório contém uma aplicação completa de **Loja Virtual**, composta por:

- **Frontend**: SPA em **Angular 19**, empacotada e servida por **NGINX**.
- **Backend**: API RESTful em **Spring Boot 3.5.0** (Java 21), com integração com **Kafka**, **MySQL** e **Flyway**.
- **Banco de Dados**: **MySQL 8.0**.
- **Mensageria**: **Apache Kafka** para eventos de pedidos.
- **Infraestrutura**: **Docker Compose** para orquestração local e **Kubernetes (K8s)** para deploy em ambiente de nuvem ou cluster local.

---

## 📚 Índice

1. [Visão Geral](#visão-geral)
2. [Tecnologias Utilizadas](#tecnologias-utilizadas)
3. [Estrutura de Pastas](#estrutura-de-pastas)
4. [Execução com Docker Compose](#execução-com-docker-compose)
5. [Execução com Kubernetes](#execução-com-kubernetes)
6. [Testes](#testes)
7. [Observabilidade (ELK, Prometheus, Actuator)](#observabilidade)
8. [Configuração de Ambiente](#-configuração-de-ambiente)
9. [Principais Endpoints da API](#principais-endpoints-da-api)
10. [Considerações Finais](#considerações-finais)

---

## 💡 Visão Geral

O projeto tem como objetivo fornecer uma base completa para um sistema de e-commerce com:

- Autenticação JWT
- CRUD de usuários, produtos e pedidos
- Eventos Kafka para processar pedidos
- Observabilidade via Spring Actuator e Prometheus
- Deploy local com Docker Compose ou em cluster com Kubernetes

---

## ⚙️ Tecnologias Utilizadas

### Backend

- Java 21
- Spring Boot 3.5.0
- Spring Security
- Spring Data JPA
- Apache Kafka
- MySQL
- JUnit 5 e Mockito (testes)

### Frontend

- Angular 19
- RxJS
- Karma / Jasmine
- JWT

### Infraestrutura

- Docker
- Docker Compose
- Kubernetes (Minikube)
- ELK Stack (Elasticsearch, Logstash, Kibana)

---

## 📂 Estrutura de Pastas

```
loja-virtual/
├── backend/
|   |── src/
│   ├── pom.xml
│   └── Dockerfile
├── elk                 # Configuração do logstash
├── frontend/
|   ├── src/
│   ├── package.json
│   └── Dockerfile
├── k8s/                # Manifests do Kubernetes
├── docker-compose.yml
├── LICENSE             # MIT LICENSE
├── minikube.sh         # Script para rodar o minikube
└── README.md
```

---

## 🛠️ Execução com Docker Compose

### 4.1 Clone o repositório (caso ainda não tenha feito):

```bash
git clone https://github.com/victormoni/loja-virtual.git
cd loja-virtual
```

### 4.2 Remova versões antigas e garanta que não há containers conflitantes:

```bash
docker compose down --remove-orphans
```

### 4.3 Suba todos os serviços (MySQL, backend e frontend) em modo destacado (detached):

```bash
docker compose up -d --build
```

- `--build` força o rebuild das imagens (`backend` e `frontend`), garantindo que o código mais recente seja empacotado.

### 4.4 Acompanhe os logs (opcional):

```bash
docker compose logs -f
```

### 4.5 Acesse a aplicação:

- Frontend: [http://localhost:4200](http://localhost:4200)
- Backend: [http://localhost:8080](http://localhost:8080)
- MySQL: localhost:3306
- Kafka: localhost:9092
- Actuator: [http://localhost:8080/actuator](http://localhost:8080/actuator)
- Swagger (OpenAPI): [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Kafka UI: [http://localhost:8085/](http://localhost:8085/)
- Kibana: [http://localhost:5601/](http://localhost:5601/)
- H2 Database: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- Jacoco: [file:///C:/<CAMINHO_ATÉ_O_DIRETÓRIO_DO_PROJETO>/loja-virtual/backend/target/site/jacoco/index.html](file:///C:/<caminho_do_projeto>/loja-virtual/backend/target/site/jacoco/index.html)

### 4.6 Parando os Containers

Para parar e remover todos os containers da stack:

```bash
docker compose down
```

Isso encerra todos os serviços e libera as portas (3306, 8080, 4200).

---

## 🚀 Execução com Kubernetes (K8s)

### 5.1 Subir infraestrutura

Execute o script minikube.sh pelo terminal como administrador na pasta raiz do projeto:

```bash
./minikube.sh
```

### 5.2 Rode o minikube tunnel

Execute o comando minikube tunnel em outro terminal como administrador para aloja funcionar, deixe o tunnel aberto enquanto estiver usando.

```bash
minikube tunnel
```

### 5.3 Descubra o EXTERNAL-IP e do Ingress NGINX com o comando: kubectl get svc -n ingress-nginx

- Exemplo

```bash
kubectl get svc -n ingress-nginx

echo "NAME                                 TYPE           CLUSTER-IP      EXTERNAL-IP     PORT(S)                      AGE"
echo "ingress-nginx-controller             LoadBalancer   10.109.168.86   192.168.49.2    80:31945/TCP,443:31383/TCP   5m"
```

### 5.4 Depois acesse no navegador: http://<EXTERNAL-IP>/"

Pegue o EXTERNAL-IP do comando anterior e troque pelo "localhost" nas URLs que vc for usar, lembre-se que dependendo da URL que for usar é necessário colocar a porta da URL:

- Exemplo (Kibana):

[http://192.168.49.2:5601/](http://192.168.49.2:5601/)

---

## 🔧 Testes

### Backend:

- Testes backend com JUnit 5 + Mockito
- Testes de integração com banco H2
- ## Uso do Jacoco para Cobertura de Código

Executar:

```bash
mvn clean verify -Dspring.profiles.active=test
```

### Frontend:

- Testes frontend com Karma + Jasmine

```bash
ng test
```

---

## 📊 Observabilidade

- **Spring Boot Actuator**: health, metrics, info.
- **Prometheus**: coleta de métricas.
- **ELK (Elasticsearch + Logstash + Kibana)**: centralização de logs.

> As configurações de log estão no logback, com possibilidade de direcionamento ao Logstash.

## 🏠 Configuração de Ambiente

### 8.1 Variáveis de Ambiente do Backend

O Spring Boot “pega” as variáveis de conexão ao banco por meio de _environment variables_ no Docker Compose. No `application.properties` temos algo como:

```properties
# Exemplo (em src/main/resources/application.properties):
spring.datasource.url=jdbc:mysql://mysql:3306/ecommerce?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### 8.2 Configurações do Frontend

No projeto Angular, existe um arquivo `environment.ts` (em `frontend/src/environments/`) que define a URL base para a API. Exemplo:

```ts
export const environment = {
  production: false,
  apiUrl: "http://localhost:8080/api",
};
```

E, no `environment.prod.ts` (para build de prod):

```ts
export const environment = {
  production: true,
  apiUrl: "/api",
};
```

> **Importante**: Se você rodar o Angular via `ng serve`, mantenha `apiUrl: 'http://localhost:8080/api'`.  
> Se for buildar para produção (e servir via NGINX), use `/api` ou a rota apropriada que configure no NGINX para redirecionar `/api` ao backend.

---

## 💡 Principais Endpoints da API

- Autenticação:

  - POST `/api/auth/register`
  - POST `/api/auth/login`
  - POST `/api/auth/refresh`

- Usuários:

  - GET `/api/users/me`
  - GET `/api/users` (admin)

- Produtos:

  - GET `/api/products`
  - POST `/api/products` (admin)

- Pedidos:

  - POST `/api/orders`
  - GET `/api/orders`

- Actuator:

  - `/actuator/health`
  - `/actuator/prometheus`

- Swagger:

  - `/swagger-ui.html`
  - `/v3/api-docs`

---

## 📖 Considerações Finais

- JWT implementado para segurança.
- Apache Kafka integrado para eventos.
- Possível deploy local (Docker Compose) ou em K8s.
- Logging pronto para integração com ELK.
- Monitoramento exposto para Prometheus.

---

Obrigado por usar a **Loja Virtual**!  
Se tiver dúvidas ou sugestões, abra uma [issue](https://github.com/victormoni/loja-virtual/issues) ou envie um PR.

**Autor:** Victor Moni
**Licença:** MIT License (consulte o arquivo [LICENSE](LICENSE) para mais detalhes)
