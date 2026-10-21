-- Apenas consultas. Não altera nem exclui dados.
SELECT email, COUNT(*) FROM usuario GROUP BY email HAVING COUNT(*) > 1;
SELECT nome, COUNT(*) FROM categoria GROUP BY nome HAVING COUNT(*) > 1;
SELECT nome, marca, COUNT(*) FROM produto GROUP BY nome, marca HAVING COUNT(*) > 1;
SELECT cart_id, produto_id, COUNT(*) FROM carrinho_item
GROUP BY cart_id, produto_id HAVING COUNT(*) > 1;

SELECT id FROM usuario WHERE email IS NULL;
SELECT id FROM categoria WHERE nome IS NULL OR TRIM(nome) = '';
SELECT id FROM produto
WHERE nome IS NULL OR TRIM(nome) = '' OR marca IS NULL OR TRIM(marca) = ''
   OR price IS NULL OR price <= 0 OR estoque IS NULL OR estoque < 0 OR categoria_id IS NULL;
SELECT id FROM carrinho_item
WHERE quantidade <= 0 OR preco_unico IS NULL OR preco_unico <= 0
   OR cart_id IS NULL OR produto_id IS NULL;


-- Equivalências relevantes para o futuro login (apenas leitura):
SELECT lower(btrim(email)) AS email_normalizado, COUNT(*)
FROM usuario GROUP BY lower(btrim(email)) HAVING COUNT(*) > 1;
SELECT id FROM usuario WHERE email IS NULL OR btrim(email) = '';
SELECT id FROM carrinho WHERE usuario_id IS NULL;
SELECT id FROM usuario WHERE length(primeiro_nome) > 255 OR length(sobrenome) > 255;
