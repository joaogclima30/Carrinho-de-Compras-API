# Carrinho de Compras API

API REST de um sistema de carrinho de compras, desenvolvida com **Java 21 e Spring Boot** para estudar desenvolvimento Backend Java: modelagem de entidades, regras de negócio, persistência, autenticação JWT e testes automatizados.

O projeto permite gerenciar produtos e categorias, adicionar itens a carrinhos, gerar pedidos com baixa de estoque e armazenar imagens de produtos.

## Tecnologias

| Tecnologia | Uso no projeto |
|---|---|
| Java 21 | Linguagem e runtime |
| Spring Boot 4.1.0 | Configuração e execução da aplicação |
| Spring Web MVC | Endpoints REST e validação das requisições |
| Spring Security 7.1.0 | Autenticação e autorização |
| JJWT 0.11.5 | Geração e validação de tokens JWT |
| BCrypt | Hash e verificação de senhas |
| Spring Data JPA / Hibernate | Persistência e mapeamento das entidades |
| PostgreSQL | Banco de dados da aplicação |
| Bean Validation | Validação dos DTOs de entrada |
| Lombok | Redução de código repetitivo |
| Maven Wrapper | Build e execução de testes |
| JUnit, Mockito e MockMvc | Testes unitários e de integração |
| H2 e Docker | Bancos isolados para testes |

## Funcionalidades

- **Usuários:** cadastro, consulta, atualização e exclusão; normalização de email e armazenamento de senha com BCrypt.
- **Autenticação:** login por email e senha, emissão de JWT e leitura do token enviado como Bearer Token.
- **Produtos:** cadastro, atualização, exclusão e consultas por ID, nome, marca e categoria; escrita restrita a administradores.
- **Categorias:** cadastro, atualização, exclusão e consulta, com validação de nomes duplicados.
- **Carrinho:** vínculo com usuário, inclusão de produtos, atualização de quantidades, remoção de itens e cálculo do total.
- **Pedidos:** conversão do carrinho em pedido, preservação do preço unitário dos itens e baixa transacional do estoque.
- **Imagens:** upload, atualização, exclusão e download de PNG/JPEG, com validação do conteúdo e limites de tamanho e dimensões.
- **Concorrência:** bloqueios no checkout e controle de versão de produtos com `@Version`.
- **Respostas:** DTOs de entrada e saída e tratamento centralizado de exceções.

As operações estão implementadas, mas algumas regras de segurança e persistência ainda precisam dos ajustes descritos em [Pendências conhecidas](#pendências-conhecidas).

## Organização

```text
Carrinho-de-Compras-API/
├── README.md
└── carrinhoAPI/
    ├── pom.xml
    ├── mvnw / mvnw.cmd
    ├── .env.example
    ├── docs/
    ├── postman/
    └── src/
        ├── main/
        │   ├── java/io/github/joaogclima30/carrinhoAPI/
        │   │   ├── controller/   # Endpoints HTTP
        │   │   ├── service/      # Regras de negócio e transações
        │   │   ├── repository/   # Acesso ao banco
        │   │   ├── model/        # Entidades e relacionamentos JPA
        │   │   ├── dto/          # Contratos de entrada e saída
        │   │   ├── mapper/       # Conversão para DTOs
        │   │   ├── validator/    # Validações de negócio
        │   │   ├── security/     # Configuração de segurança e JWT
        │   │   ├── exceptions/   # Exceções e tratamento de erros
        │   │   └── data/         # Inicialização de dados de desenvolvimento
        │   └── resources/
        └── test/
```

O fluxo principal de uma requisição é **controller → service → repository → banco**. Os services delimitam as transações e montam os DTOs antes de retornar, com `open-in-view` desativado.

## Executar localmente

### Pré-requisitos

- JDK 21 instalado e `JAVA_HOME` configurado.
- PostgreSQL em execução e um banco criado para o projeto.
- Git para clonar o repositório.
- Docker somente se quiser executar os testes em PostgreSQL descartável.

Não é necessário instalar Maven separadamente: o projeto inclui o Maven Wrapper. Na primeira execução, ele pode baixar o Maven e as dependências.

### 1. Clonar o repositório

```bash
git clone https://github.com/joaogclima30/Carrinho-de-Compras-API.git
cd Carrinho-de-Compras-API/carrinhoAPI
```

### 2. Configurar o ambiente

Copie `.env.example` para `.env` **dentro de `carrinhoAPI`, ao lado do `pom.xml`**.

No PowerShell:

```powershell
Copy-Item .env.example .env
```

No Linux/macOS:

```bash
cp .env.example .env
```

Preencha os dados do seu PostgreSQL. Exemplo com valores que devem ser substituídos:

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/carrinhoAPI
DB_USERNAME=seu_usuario_postgres
DB_PASSWORD=sua_senha_local
DEV_SEED_ENABLED=false
```

O banco indicado em `DB_URL` precisa existir antes da inicialização. O Hibernate cria ou atualiza tabelas; ele não cria o banco PostgreSQL.

O arquivo `.env` é ignorado pelo Git. Variáveis de ambiente do processo têm precedência sobre seus valores, inclusive quando definidas na configuração de execução do IntelliJ.

### 3. Iniciar a aplicação

No PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

No Linux/macOS:

```bash
./mvnw spring-boot:run
```

No IntelliJ, execute `CarrinhoApiApplication` com o diretório de trabalho apontando para `carrinhoAPI`.

A URL base padrão é:

```text
http://localhost:8080/api/v1
```

### Perfis

| Perfil | Comportamento |
|---|---|
| `dev` | Perfil padrão; usa `ddl-auto: update` e permite inicialização opcional de contas de desenvolvimento. |
| `test` | Usa H2 por padrão, seed desligado e `create-drop` somente no banco de testes; aceita PostgreSQL isolado pelas variáveis `TEST_DATABASE_*`. |
| `prod` | Usa `ddl-auto: validate`; exige que o schema já esteja preparado. Esse perfil, sozinho, não resolve as pendências de segurança da aplicação. |

Para selecionar um perfil na execução pelo Maven:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

`ddl-auto: create` recria as tabelas mapeadas e perde seus dados. Não é necessário utilizá-lo para executar o projeto normalmente.

## Contas de desenvolvimento

Para criar contas de exemplo, habilite o seed no ambiente local e escolha uma senha:

```dotenv
DEV_SEED_ENABLED=true
DEV_SEED_PASSWORD=defina_uma_senha_de_desenvolvimento
```

Com o perfil `dev`, o `DataInitializer` cria as roles e as contas que ainda não existem:

| Contas | Role |
|---|---|
| `usuario0@gmail.com` até `usuario5@gmail.com` | `ROLE_USER` |
| `admin0@gmail.com` até `admin2@gmail.com` | `ROLE_ADMIN` |

As novas contas usam a senha definida em `DEV_SEED_PASSWORD`. Contas já existentes são preservadas: mudar a variável não redefine suas senhas.

O cadastro público de usuário é outra operação; a atribuição automática de sua role padrão ainda é uma pendência. As contas administrativas de exemplo servem para testar os endpoints administrativos durante o desenvolvimento.

## Autenticação JWT

### Fazer login

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin0@gmail.com",
  "senha": "sua_senha_de_desenvolvimento"
}
```

No Postman, configure **Body → raw → JSON** e **Authorization → No Auth** para o login.

Uma autenticação válida retorna HTTP `200`:

```json
{
  "mensagem": "Login feito com Sucesso",
  "data": {
    "id": 1,
    "token": "<token_jwt_retornado_pela_api>"
  }
}
```

O ID é ilustrativo: use os valores retornados na sua execução. Email inexistente ou senha incorreta retornam HTTP `401` com a mesma mensagem genérica.

### Acessar uma rota com token

Nas próximas requisições, envie:

```http
Authorization: Bearer <token_jwt_retornado_pela_api>
```

No Postman, selecione **Authorization → Bearer Token** e cole apenas o valor de `data.token`, sem aspas. Para cadastrar, atualizar ou excluir produtos, utilize o token de um administrador.

O token tem validade configurada de uma hora. Após expirar, faça login novamente. A senha original é enviada no login; o hash BCrypt armazenado no banco não deve ser enviado como senha. A comunicação com JWT permanece stateless: as próximas requisições precisam enviar seu próprio token.

**Configuração pendente:** o segredo JWT ainda está definido diretamente no `application.yaml` local. Ele precisa ser externalizado e revisado antes de publicar a configuração; definir uma variável `JWT_SECRET` sem adaptar essa propriedade não altera o comportamento atual.

## Exemplo: cadastrar um produto

```http
POST /api/v1/produtos/salvar
Authorization: Bearer <token_de_administrador>
Content-Type: application/json
```

```json
{
  "nome": "Relógio inteligente",
  "marca": "Marca exemplo",
  "preco": 199.90,
  "estoque": 10,
  "descricao": "Produto utilizado para testar a API",
  "categoria": {
    "nome": "Eletrônicos"
  }
}
```

`preco` e `estoque` são números. `categoria` é um objeto com `nome`. O campo `versao` é gerenciado pelo Hibernate e não precisa ser enviado no cadastro.

## Endpoints principais

Todos os caminhos abaixo são relativos ao prefixo padrão `/api/v1`. A tabela resume os contratos atuais; ela não representa uma matriz completa de autorização.

| Método | Caminho | Operação |
|---|---|---|
| `POST` | `/auth/login` | Autenticar e receber JWT |
| `POST` | `/usuarios` | Cadastrar usuário |
| `GET` | `/usuarios/{usuarioId}/listarUsuario` | Consultar usuário |
| `PUT` | `/usuarios/{usuarioId}/atualizar` | Atualizar usuário |
| `DELETE` | `/usuarios/{usuarioId}/deletar` | Excluir usuário sem pedidos |
| `GET` | `/produtos/all` | Listar produtos |
| `GET` | `/produtos/{produtoId}/listarPorId` | Consultar produto |
| `POST` | `/produtos/salvar` | Cadastrar produto — administrador |
| `PUT` | `/produtos/{produtoId}/atualizar` | Atualizar produto — administrador |
| `DELETE` | `/produtos/{produtoId}/deletar` | Excluir produto — administrador |
| `GET` | `/categorias/todas` | Listar categorias |
| `POST` | `/categorias/salvar` | Cadastrar categoria |
| `POST` | `/carrinhoItem/adicionarItem` | Adicionar produto ao carrinho |
| `PUT` | `/carrinhoItem/{carrinhoId}/item/{produtoId}/atualizar` | Atualizar quantidade |
| `DELETE` | `/carrinhoItem/{carrinhoId}/item/{produtoId}/remover` | Remover item |
| `GET` | `/carrinhos/{carrinhoId}/listarCarrinho` | Consultar carrinho |
| `DELETE` | `/carrinhos/{carrinhoId}/limparCarrinho` | Limpar carrinho |
| `GET` | `/carrinhos/{carrinhoId}/total` | Consultar total |
| `POST` | `/pedidos/{usuarioId}` | Finalizar pedido |
| `GET` | `/pedidos/{pedidoId}` | Consultar pedido |
| `GET` | `/pedidos/usuario/{usuarioId}` | Listar pedidos do usuário |
| `POST` | `/imagens/upload` | Enviar imagens de um produto |
| `GET` | `/imagens/imagem/download/{imagemId}` | Baixar imagem |

A inclusão no carrinho recebe `produtoId` e `quantidade` como query params. Na implementação atual, também recebe `usuarioId` e/ou `carrinhoId`; a identificação exclusiva pelo token e a validação de propriedade ainda precisam ser concluídas. Enviar um ID não comprova permissão para operar o recurso.

O upload usa `multipart/form-data`, campo `files` e parâmetro `produtoId`. São aceitos PNG/JPEG válidos, com limites padrão de 5 MB por arquivo, cinco arquivos por envio e 20 milhões de pixels por imagem.

## Testar com Postman

Importe a [coleção disponível no repositório](carrinhoAPI/postman/Ecommerce-Api.postman_collection.json).

A coleção contém operações de catálogo, usuários, imagens, carrinho e pedidos. O fluxo de login e a configuração de Bearer Token podem precisar ser adicionados manualmente, pois a coleção foi criada antes das alterações recentes de autenticação.

Fluxo sugerido para testes locais:

1. Iniciar a aplicação e preparar as contas de desenvolvimento.
2. Fazer login como administrador e copiar o token.
3. Cadastrar categoria e produto; anotar os IDs retornados.
4. Fazer login com a conta usada para testar o carrinho.
5. Adicionar, consultar e alterar os itens do carrinho.
6. Finalizar o pedido e conferir estoque e histórico.

Alguns cenários desse fluxo ainda possuem defeitos registrados pelos testes. Sucesso em uma requisição manual não comprova que todas as regras de autorização estejam corretas.

## Testes automatizados

Os testes incluem regras de negócio, cálculos monetários, persistência, concorrência, login, JWT, autorização e cenários de erro. Os testes web utilizam MockMvc; os testes de auditoria de segurança exercitam a cadeia real de filtros.

### H2 em memória

Dentro de `carrinhoAPI`:

```powershell
.\mvnw.cmd clean test
```

O perfil `test` desativa o seed e a importação de `.env`. Por padrão usa H2 em memória. Caso tenha definido variáveis `TEST_DATABASE_*` no processo, revise-as antes de executar: elas substituem o datasource padrão de testes e nunca devem apontar para seu banco de desenvolvimento.

### PostgreSQL temporário

Com Docker em execução, no PowerShell:

```powershell
.\docs\testar-postgres.ps1
```

O script cria um container PostgreSQL 16.3 com banco e porta exclusivos, executa a suíte e remove o container ao terminar. Não utiliza a base de desenvolvimento.

### Última auditoria — 24/10/2026

| Ambiente | Casos descobertos | Aprovados | Reprovados | Ignorados |
|---|---:|---:|---:|---:|
| H2 em memória | 126 | 93 | 32 | 1 |
| PostgreSQL 16.3 isolado | 126 | 93 | 33 | 0 |

O teste ignorado no H2 verifica o ciclo de vida de Large Objects do PostgreSQL. A suíte ainda não está verde: os cenários reprovados registram comportamentos que precisam ser corrigidos. Um mesmo problema pode causar mais de uma reprovação.

Os resultados se referem ao código local auditado, incluindo arquivos de teste e alterações de autenticação que ainda precisam ser revisados e publicados no Git. Eles não certificam versões anteriores ou futuras do repositório.

Os relatórios de uma nova execução ficam em `carrinhoAPI/target/surefire-reports`.

## Pendências conhecidas

As próximas correções estão priorizadas em torno de segurança e consistência:

- Completar a autorização de operações de usuários, pedidos, categorias e imagens.
- Impedir consulta ou alteração de recursos pertencentes a outro usuário.
- Unificar a inclusão no carrinho em uma única transação, utilizando a identidade autenticada.
- Externalizar o segredo JWT e padronizar as respostas de erro do filtro.
- Corrigir o retorno de cadastro duplicado, o limite de senha para BCrypt e a atribuição de `ROLE_USER` no cadastro público.
- Garantir unicidade dos nomes de roles e revisar os vínculos do histórico de pedidos.
- Liberar o conteúdo das imagens ao excluir seus registros no PostgreSQL.
- Reduzir N+1 nas listagens, adicionar paginação e definir uma política consistente para mudanças de preço no carrinho.

As migrations devem preservar dados existentes. Apagar o schema ou recriar o banco não substitui essas correções.

## Aprendizados do projeto

O desenvolvimento permite estudar como uma requisição percorre as camadas da aplicação e como decisões de implementação afetam seu comportamento:

- DTOs controlam os campos aceitos e devolvidos pela API.
- Transações precisam abranger toda a operação de negócio para evitar efeitos parciais.
- Autenticação identifica o usuário; autorização determina quais recursos ele pode acessar.
- Constraints do banco complementam as validações da aplicação.
- Locks e controle de versão tratam conflitos entre operações concorrentes.
- Testes com PostgreSQL podem revelar comportamentos que o H2 não reproduz.

O projeto foi desenvolvido acompanhando um tutorial de Backend Java, com adaptações e revisões adicionais. As pendências e os testes fazem parte da evolução da implementação.

## Autor

[João Gabriel — GitHub](https://github.com/joaogclima30)
