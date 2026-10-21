-- PostgreSQL: migração manual e transacional. Não é executada pela aplicação.
-- Execute primeiro verificar-dados.sql e resolva duplicatas segundo a regra do negócio.
BEGIN;
LOCK TABLE usuario IN SHARE ROW EXCLUSIVE MODE;
DO $$
BEGIN
    IF EXISTS (
        SELECT lower(btrim(email)) FROM usuario
        GROUP BY lower(btrim(email)) HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'Existem e-mails equivalentes. Resolva as duplicatas antes de normalizar';
    END IF;
    IF EXISTS (SELECT 1 FROM usuario WHERE email IS NULL OR btrim(email) = '') THEN
        RAISE EXCEPTION 'Existem usuarios sem e-mail válido';
    END IF;
END $$;
UPDATE usuario SET email = lower(btrim(email)) WHERE email IS DISTINCT FROM lower(btrim(email));
CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_email_normalizado ON usuario (lower(btrim(email)));
COMMIT;
