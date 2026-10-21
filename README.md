# Carrinho de Compras API

API de estudo com Java 21, Spring Boot 4.1, Spring Data JPA, Bean Validation e PostgreSQL, acompanhando o tutorial de carrinho de compras.

## Executar localmente

1. Abra a pasta `carrinhoAPI`, onde está `pom.xml`.
2. Copie `.env.example` para `.env` e preencha os dados do seu PostgreSQL. O arquivo `.env` está no `.gitignore`.
3. Execute `./mvnw.cmd spring-boot:run`, ou rode `CarrinhoApiApplication` no IntelliJ com o diretório de trabalho apontando para `carrinhoAPI`.

Na instalação local que recebeu esta atualização, a configuração anterior do banco foi preservada em `.env`. Variáveis de ambiente do processo têm precedência. A API usa por padrão a porta 8080 e o prefixo `/api/v1`.

## Estado atual

- CRUD de usuários, produtos e categorias; upload, download e manutenção de imagens PNG/JPEG.
- Carrinho vinculado a usuário, inclusão atômica de itens e total persistido.
- Pedidos com baixa transacional de estoque, validação do saldo e bloqueios para concorrência.
- Usuários com pedidos não podem ser excluídos.
- DTOs montados nas transações dos services, com `open-in-view` desativado.
- Erros HTTP com `{ "status": 400, "mensagem": "..." }`.

**Autenticação, autorização, hash de senhas e JWT serão implementados na próxima etapa.** Os IDs enviados atualmente identificam o recurso e o usuário de teste; não comprovam identidade ou permissão. As senhas antigas e novas ainda precisam do tratamento dessa próxima etapa.

## Usar o Postman

Importe `carrinhoAPI/postman/Ecommerce-Api.postman_collection.json`. São 35 operações e 3 casos de validação. As requisições de cadastro preenchem automaticamente os IDs nas variáveis da coleção.

Para o fluxo principal, execute: cadastrar usuário → cadastrar categoria → cadastrar produto → adicionar item → consultar carrinho → finalizar pedido. Selecione arquivos manualmente nas requisições de imagem. As exclusões são operações separadas e podem retornar 409 quando houver vínculos.

## Testes

Dentro de `carrinhoAPI`:

```powershell
.\mvnw.cmd clean test
```

O perfil `test` usa H2 em memória, sem gerar usuários de exemplo. Para executar a mesma suíte em um PostgreSQL 16.3 temporário, com Docker em funcionamento:

```powershell
.\docs\testar-postgres.ps1
```

O script cria banco e porta exclusivos, usa `create-drop` somente nesse banco e remove o container ao terminar. A atualização foi validada com 63 testes no H2 e os mesmos 63 no PostgreSQL 16.3.

## Configuração e contratos

- `dev`: atualização automática do esquema, SQL desativado por padrão e geração opcional de contas de exemplo.
- `prod`: validação do esquema, sem alteração automática; credenciais fornecidas externamente.
- `test`: banco de testes descartável, com esquema recriado pela suíte.

Consulte `carrinhoAPI/docs/PREPARACAO_SECURITY.md` para contratos, mudanças e preparação dos dados existentes. As consultas em `verificar-dados.sql` são somente de leitura; `normalizar-emails.sql` é uma migração manual separada.
