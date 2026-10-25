-- Expand-contract, deploy 4 de 4 (10.02). O deploy 3, sem migration, tirou a coluna antiga da aplicação:
-- nenhuma instância em execução usa mais categoria_pai_id.

alter table categoria drop column categoria_pai_id;
