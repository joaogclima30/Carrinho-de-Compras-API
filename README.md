# 🛒 Carrinho de Compras API

API REST de um sistema de **carrinho de compras**, desenvolvida com Java e Spring Boot.

O projeto foi desenvolvido com foco no aprendizado de construção de APIs backend, trabalhando desde a modelagem das entidades e operações CRUD até gerenciamento de carrinho, pedidos e autenticação utilizando **Spring Security e JWT**.

---

## 🚀 Tecnologias utilizadas

* **Java**
* **Spring Boot**
* **Spring Data JPA**
* **Spring Security**
* **JWT (JSON Web Token)**
* **PostgreSQL**
* **Maven**
* **REST API**
* **Postman**

---

## 📌 Funcionalidades

### 👤 Usuários

* Cadastro de usuários
* Consulta de usuários
* DTOs para comunicação com a API
* Autenticação de usuários
* Login utilizando JWT

### 📦 Produtos

* Cadastro de produtos
* Consulta de produtos
* Atualização de produtos
* Remoção de produtos
* Associação de produtos com categorias
* Gerenciamento de imagens dos produtos

### 🗂️ Categorias

* Cadastro de categorias
* Consulta de categorias
* Atualização de categorias
* Remoção de categorias

### 🛒 Carrinho

* Criação e gerenciamento de carrinhos
* Adição de produtos ao carrinho
* Remoção de produtos
* Atualização da quantidade de produtos
* Consulta dos itens do carrinho

### 📋 Pedidos

* Criação de pedidos
* Gerenciamento de pedidos
* Associação de pedidos aos usuários
* Consulta de pedidos

### 🔐 Segurança

A API utiliza **Spring Security** para proteção dos endpoints e **JWT** para autenticação.

O fluxo de autenticação funciona basicamente da seguinte forma:

```text
Usuário
   │
   ▼
Login
   │
   ▼
Spring Security
   │
   ▼
JWT
   │
   ▼
Requisições autenticadas
   │
   ▼
Endpoints protegidos
```

---

## 🏗️ Estrutura do projeto

O projeto segue uma organização baseada na separação de responsabilidades entre as principais camadas da aplicação.

```text
src
└── main
    └── java
        └── ...
            ├── controller
            ├── service
            ├── repository
            ├── model
            ├── dto
            └── security
```

### Controller

Responsável por receber as requisições HTTP e disponibilizar os endpoints da API.

### Service

Concentra as regras de negócio da aplicação.

### Repository

Responsável pela comunicação com o banco de dados através do Spring Data JPA.

### Model

Contém as entidades utilizadas pela aplicação.

### DTO

Objetos utilizados para transportar dados entre as camadas da aplicação e controlar os dados expostos pela API.

### Security

Responsável pela configuração do Spring Security, autenticação dos usuários e utilização de JWT.

---

## 🔗 Principais recursos da API

A API possui recursos relacionados a:

```text
/users
/products
/categories
/carts
/cart-items
/orders
```

Os endpoints permitem realizar operações de gerenciamento dos produtos, categorias, carrinhos, itens do carrinho, pedidos e usuários.

---

## 🔐 Autenticação

A autenticação da aplicação é realizada através de **Spring Security + JWT**.

Após realizar o login, o usuário recebe um token que deve ser utilizado nas requisições que necessitam de autenticação.

Exemplo:

```http
Authorization: Bearer <token>
```

---

## 🗄️ Banco de dados

O projeto utiliza **PostgreSQL** para persistência dos dados.

Entre as principais informações armazenadas estão:

* Usuários
* Produtos
* Categorias
* Imagens dos produtos
* Carrinhos
* Itens do carrinho
* Pedidos

---

## 🧪 Testando a API

A API pode ser testada utilizando ferramentas como:

* Postman
* Insomnia
* Bruno

Exemplo de fluxo para testar a aplicação:

```text
1. Criar usuário
       ↓
2. Fazer login
       ↓
3. Obter JWT
       ↓
4. Autenticar requisições
       ↓
5. Criar categorias
       ↓
6. Criar produtos
       ↓
7. Adicionar produtos ao carrinho
       ↓
8. Alterar quantidade
       ↓
9. Criar pedido
```

---

## 📚 O que foi desenvolvido neste projeto

Este projeto aborda conceitos importantes do desenvolvimento backend com Java e Spring Boot, incluindo:

* Criação de APIs REST
* Arquitetura em camadas
* Spring Data JPA
* Mapeamento objeto-relacional
* CRUD
* Relacionamentos entre entidades
* DTOs
* Services
* Repositories
* Controllers
* Gerenciamento de carrinho
* Gerenciamento de pedidos
* Spring Security
* Autenticação
* JWT
* Testes de endpoints
* Organização de uma aplicação backend

---

## 🎯 Objetivo

O principal objetivo deste projeto é colocar em prática conceitos utilizados no desenvolvimento de aplicações backend com **Java e Spring Boot**, construindo uma API de e-commerce com gerenciamento de produtos, categorias, usuários, carrinho e pedidos, além de autenticação e autorização.

---

## 👨‍💻 Autor

**João Gabriel de Carvalho Lima**

Desenvolvedor Backend Java

* GitHub: https://github.com/joaogclima30
* LinkedIn: https://www.linkedin.com/in/joaolima3008

---

O conteúdo da aula aborda a construção progressiva do backend, passando por entidades, CRUD, produtos, categorias, imagens, carrinho, pedidos, usuários, DTOs, Spring Security e JWT.
