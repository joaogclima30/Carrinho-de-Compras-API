# Correções dos itens 4 a 10

## Comportamento

- Quantidades de itens precisam ser maiores que zero. A soma de inclusões também é validada, sem estouro de inteiro.
- Inclusão e atualização rejeitam quantidades superiores ao estoque atual. O carrinho não reserva estoque: o saldo é conferido novamente ao finalizar.
- Pedidos vazios, itens inválidos e estoque insuficiente são rejeitados antes da baixa. A transação engloba a criação do pedido, a baixa e a limpeza do carrinho.
- Alterações do mesmo carrinho são serializadas por bloqueio de banco. O fechamento bloqueia os produtos em ordem de ID e recarrega seus saldos. A versão do produto protege contra atualizações concorrentes feitas por outros fluxos.
- Respostas de produtos, categorias, carrinhos, itens e pedidos usam DTOs, sem expor os relacionamentos bidirecionais JPA ou os blobs de imagens. Consultas e cadastros de usuários usam o DTO de resposta sem senha.
- Produto não propaga exclusão à categoria; item não propaga exclusão ao carrinho. A remoção de órfãos continua no lado do agregado.
- As entidades deixam de gerar `equals`, `hashCode` e `toString` com `@Data`. A identidade do objeto permanece estável quando sua quantidade ou ID muda dentro do conjunto gerenciado pelo JPA.
- Limpar o carrinho remove os itens e zera o total na mesma transação.
- O repositório de categorias persiste somente entidades, sem o método `save` de DTO.
- Produtos e categorias são validados no cadastro e na atualização. As verificações de duplicidade da atualização ignoram o próprio registro.
- Preço precisa ser positivo, com até duas casas decimais; estoque precisa ser informado e não pode ser negativo. Categoria e seus dados são obrigatórios.
- O banco possui restrições únicas para e-mail, nome de categoria, combinação nome/marca de produto e combinação carrinho/produto de item. Violações de integridade e conflitos concorrentes retornam HTTP 409; entradas inválidas retornam HTTP 400.

## Contratos de resposta

As rotas existentes foram mantidas. Clientes que consumiam entidades diretamente devem usar os campos dos DTOs:

- Produto: `id`, `nome`, `marca`, `preco`, `estoque`, `descricao`, `categoria` (`id`, `nome`) e `imagensDto`.
- Carrinho: `carrinhoId`, `itens` e `precoTotal`.
- Item do carrinho: `itemId`, `quantidade`, `precoUnitario` e `produto`.
- Pedido: `pedidoId`, `usuarioId`, `pedidoHorario`, `totalPedido`, `status` e `item`.

O JSON de entrada de produto continua recebendo `categoria: {"nome": "..."}`. IDs e imagens são dados de resposta, não são usados para criar ou atualizar um produto.

## Banco existente

Nenhum teste acessa o PostgreSQL configurado na aplicação. As verificações usam H2 em memória, portanto não substituem um teste com PostgreSQL.

O projeto mantém `ddl-auto: update`. Antes de iniciar a versão nova contra um banco com dados, execute as consultas de leitura em `docs/verificar-dados.sql`. Dados antigos duplicados ou inválidos precisam ser corrigidos para permitir as restrições novas; a aplicação não escolhe registros para apagar automaticamente. A coluna de versão do produto é criada com valor padrão zero.

## Testes

Execute na pasta que contém `pom.xml`:

```powershell
.\mvnw.cmd test
```

O perfil `test` configura H2 com recriação do esquema. A suíte cobre quantidades inválidas, estoque no fechamento, duas compras simultâneas da última unidade, dois fechamentos do mesmo carrinho, remoção após alteração de quantidade/ID, preservação da categoria compartilhada, limpeza persistida, unicidade e respostas HTTP sem ciclos.
