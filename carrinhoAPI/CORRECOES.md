# Correções aplicadas

As correções iniciais de estoque, quantidades, cascatas JPA, DTOs, igualdade das entidades, limpeza do carrinho e unicidade foram preservadas.

A atualização de preparação para Security também corrigiu o usuário fixo, o parâmetro de carrinho ignorado, a criação não atômica, as imagens, o cadastro de e-mail, a exclusão de histórico e a dependência de open-in-view. Foram separados os ambientes, atualizado o Postman e removidos os conversores e a dependência ModelMapper sem uso.

Detalhes e contratos: [docs/PREPARACAO_SECURITY.md](docs/PREPARACAO_SECURITY.md).

Validação desta atualização: 63 testes passaram no H2 e os mesmos 63 no PostgreSQL 16.3, em banco descartável.
