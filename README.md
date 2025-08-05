# Loja Virtual

Este repositório contém uma aplicação simples de **Loja Virtual**, composta por:

- **Frontend**: SPA em **Angular 20**, empacotada e servida por **NGINX**.
- **Backend**: API RESTful em **Spring Boot 3.5.0** (Java 21).
- **Banco de Dados**: **MySQL 8.0**.
- **Mensageria**: **Apache Kafka** para eventos de pedidos.
- **Infraestrutura**: **Docker Compose** para orquestração local e **Kubernetes (K8s)** para deploy em ambiente de nuvem ou cluster local.

---

## 📚 Índice

1. [Visão Geral](#-visão-geral)
2. [Tecnologias Utilizadas](#️-tecnologias-utilizadas)
3. [Estrutura de Pastas](#-estrutura-de-pastas)
4. [Execução com Docker Compose](#️-execução-com-docker-compose)
5. [Execução com Kubernetes](#-execução-com-kubernetes)
6. [Testes](#-testes)
7. [Considerações Finais](#-considerações-finais)

---

## 💡 Visão Geral

O projeto tem como objetivo fornecer uma base completa para um sistema de e-commerce com:

- Autenticação JWT
- CRUD de usuários, produtos e pedidos
- Eventos Kafka para processar pedidos
- Observabilidade via Spring Actuator
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

- Angular 20
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
ShopEasy/
├── backend/
|   |── src/
│   ├── pom.xml
│   └── Dockerfile
├── elk                 # Configuração do logstash
├── frontend/
|   ├── src/
│   ├── package.json
│   └── Dockerfile
├── k8s/
├── .gitignore
├── docker-compose.yml  # Manifests do Kubernetes
├── LICENSE             # MIT LICENSE
├── minikube.sh         # Script para rodar o minikube
└── README.md
```

---

## 🛠️ Execução com Docker Compose

### Clone o repositório:

```bash
git clone https://github.com/victormoni/ShopEasy.git
cd ShopEasy
```

### Suba todos os serviços em modo destacado (detached mode):

```bash
docker compose up -d
```

### Acesse a aplicação:

- Frontend: [http://localhost](http://localhost)
- Actuator: [http://localhost:8080/actuator](http://localhost:8080/actuator)
- Swagger: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Kafka UI: [http://localhost:8085/](http://localhost:8085/)
- Kibana: [http://localhost:5601/](http://localhost:5601/)
- H2 Database: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- MySQL: [localhost:3306](localhost:3306)

### Parando os Containers

```bash
docker compose down
```

---

## 🚀 Execução com Kubernetes

### Subir infraestrutura

Execute o script minikube.sh pelo terminal como administrador na pasta raiz do projeto:

```bash
./minikube.sh
```

### Rode o minikube tunnel

Execute o comando minikube tunnel em outro terminal como administrador para a loja funcionar, deixe o tunnel aberto enquanto estiver usando.

```bash
minikube tunnel
```

### Descubra o EXTERNAL-IP e do Ingress NGINX com o comando abaixo:

```bash
kubectl get svc -n ingress-nginx

echo "NAME                                 TYPE           CLUSTER-IP      EXTERNAL-IP     PORT(S)                      AGE"
echo "ingress-nginx-controller             LoadBalancer   10.109.168.86   192.168.49.2    80:31945/TCP,443:31383/TCP   5m"
```

### Depois acesse no navegador: http://EXTERNAL-IP/"

Pegue o EXTERNAL-IP do comando anterior e troque pelo "localhost" nas URLs que vc for usar, lembre-se que dependendo da URL que for usar é necessário colocar a porta da URL:

- Exemplo:

[http://192.168.49.2:5601](http://192.168.49.2:5601)

---

## 🔧 Testes

### Backend

- Testes backend com JUnit 5 + Mockito
- Testes de integração com banco H2
- Uso do Jacoco para Cobertura de Código

```bash
mvn clean verify
```

### Frontend:

- Testes frontend com Karma + Jasmine

```bash
ng test
```

---

Obrigado por usar a **ShopEasy**!  
Se tiver dúvidas ou sugestões, abra uma [issue](https://github.com/victormoni/ShopEasy/issues) ou envie um Pull Request.

**Autor:** Victor Moni
**Licença:** MIT License (consulte o arquivo [LICENSE](LICENSE) para mais detalhes)
