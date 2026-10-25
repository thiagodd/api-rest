-- Expand-contract, deploy 1 de 4 (10.02): renomear categoria.categoria_pai_id para pai_id.
-- Só acrescenta. A versão anterior da aplicação, que ainda não conhece pai_id, continua funcionando.

alter table categoria add column pai_id uuid references categoria (id);
