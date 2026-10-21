# Preparação para a etapa de Security

## Inclusão no carrinho

`POST /api/v1/carrinhoItem/adicionarItem`

Parâmetros:

- `usuarioId`: usuário do carrinho. Pode ser omitido quando `carrinhoId` é informado.
- `carrinhoId`: carrinho existente. Pode ser omitido quando `usuarioId` é informado.
- `produtoId`: obrigatório, positivo e existente.
- `quantidade`: obrigatória, inteira e maior que zero.

Ao menos um entre `usuarioId` e `carrinhoId` é obrigatório. Se ambos forem informados, devem corresponder. Não há mais usuário de ID fixo. Essa conferência verifica consistência dos parâmetros; autenticação e autorização ainda não estão implementadas.

Exemplo de primeira inclusão: `POST /api/v1/carrinhoItem/adicionarItem?usuarioId=7&produtoId=3&quantidade=1`.

A resposta HTTP 200 contém `data.carrinhoId`, `data.itens` e `data.precoTotal`. Cada item já possui ID persistido. A criação do carrinho e a inclusão são uma transação só. Uma falha de produto, quantidade ou estoque desfaz também a criação. A primeira criação é serializada por usuário; as alterações de itens e o fechamento continuam usando bloqueios no banco. Os dois lados do vínculo usuário/carrinho são sincronizados.

## Usuários e histórico

O e-mail é normalizado removendo espaços nas extremidades e convertendo para minúsculas com `Locale.ROOT`, tanto na entrada do cadastro como na entidade. As consultas de e-mail também reconhecem registros antigos com espaços ou maiúsculas. Cadastro e atualização validam os comprimentos dos campos; a senha não é aparada ou alterada por essa normalização.

Excluir um usuário com pedidos retorna 409. A coleção de pedidos não usa mais cascata de exclusão nem remoção de órfãos. Usuários sem pedidos podem ser excluídos com seus carrinhos. As respostas não contêm senha e os conversores antigos para DTO de criação foram removidos.

Para os dados existentes:

1. Execute as consultas de leitura de `verificar-dados.sql`.
2. Se houver e-mails equivalentes, resolva quais registros representam cada pessoa antes de prosseguir.
3. Revise e execute `normalizar-emails.sql` quando quiser normalizar o legado e criar o índice funcional. O script é transacional, bloqueia alterações durante a operação e interrompe antes de atualizar caso encontre duplicatas ou e-mails vazios. Não apaga usuários.

Essa migração não é executada pela aplicação nem pela atualização do código. Nenhuma alteração foi executada no banco de uso da API durante os testes desta entrega.

## Imagens

- Conteúdo permitido: PNG ou JPEG válido, detectado pelo decoder, independentemente da extensão e do tipo declarado pelo cliente.
- Limites padrão: 5 MB por arquivo, 5 arquivos por envio, 26 MB por requisição multipart e 20 milhões de pixels por imagem.
- Configuração: `UPLOAD_MAX_FILE_SIZE`, `UPLOAD_MAX_FILES`, `UPLOAD_MAX_REQUEST_SIZE` e `UPLOAD_MAX_PIXELS`.
- O lote inteiro é validado antes de salvar. Arquivo inválido não deixa um lote parcialmente persistido.
- Upload recebe o campo multipart `files`; atualização recebe `file`.
- Download atual: `/imagens/imagem/download/{imagemId}`. A rota antiga `dowload` permanece como alias.
- URLs das respostas são calculadas com o prefixo configurado, inclusive para registros antigos cuja URL armazenada esteja incorreta.
- Nomes são reduzidos ao nome do arquivo, sem caminhos ou caracteres de controle. O cabeçalho de download usa o construtor de `ContentDisposition` e UTF-8.
- Tipos antigos ausentes ou inesperados são servidos como `application/octet-stream`.
- Falhas internas preservam a causa nos logs e devolvem mensagem sem detalhes do banco.

O armazenamento continua no banco com o modelo Blob existente; esta etapa não migra os arquivos antigos para outro armazenamento.

## Contratos e erros

As rotas anteriores foram preservadas, incluindo o alias de download. Usuários, categorias, produtos, carrinhos, itens e pedidos são convertidos para DTO dentro das transações, com `open-in-view: false`.

Respostas de sucesso mantêm HTTP 200 e o formato `{ "mensagem": "...", "data": ... }`. Pedidos usam o nome interno `PedidoResponseDTO`; os campos JSON continuam `pedidoId`, `usuarioId`, `pedidoHorario`, `totalPedido`, `status` e `item`.

Erros usam `{ "status": 400, "mensagem": "..." }`. Códigos: 400 para entrada inválida, 404 para recurso inexistente, 405 para método não permitido, 409 para conflito de estoque, concorrência ou vínculo, 413 para tamanho excedido, 415 para conteúdo HTTP incompatível e 500 para falha interna.

## Ambientes

O perfil padrão é `dev`. A configuração local antiga foi preservada em `.env`, ao lado de `pom.xml`, e está ignorada pelo Git. Para uma instalação nova, copie `.env.example` e preencha `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.

`prod` usa `ddl-auto: validate`; prepare o esquema previamente. `test` usa H2 e `create-drop` por padrão. `TEST_DATABASE_URL`, `TEST_DATABASE_USERNAME`, `TEST_DATABASE_PASSWORD` e `TEST_DATABASE_DRIVER` permitem direcionar a suíte para um banco de testes descartável; nunca aponte essas variáveis para um banco com dados que deseja manter.

Contas de exemplo só são criadas em `dev` quando `DEV_SEED_ENABLED=true` e `DEV_SEED_PASSWORD` contém 8 a 255 caracteres. O padrão é desativado. Não há mais criação automática de contas com senha `123` em todos os ambientes. Contas que já existiam não são apagadas nem têm a senha alterada nesta atualização.

## Testes e próxima etapa

Foram executados 63 testes com H2 e 63 com PostgreSQL 16.3 isolado. A suíte inclui requisições HTTP sem transação externa, rollback real, primeira criação concorrente, fechamento concorrente, imagens, normalização, proteção de histórico e erros de cliente.

Hash de senhas, tratamento das senhas antigas, login, JWT, perfis e controle de acesso por proprietário permanecem para a etapa de Spring Security. Também fica pendente a decisão de negócio sobre preço: atualmente a inclusão guarda o preço, a atualização de quantidade atualiza esse preço e o fechamento usa o valor guardado. Essa regra não foi alterada nesta entrega.
