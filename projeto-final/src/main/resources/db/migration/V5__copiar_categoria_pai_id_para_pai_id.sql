-- Expand-contract, deploy 2 de 4 (10.02): todas as instâncias já escrevem nas duas colunas,
-- então as linhas antigas podem ser copiadas sem que novas divergências apareçam depois.

update categoria
   set pai_id = categoria_pai_id
 where pai_id is distinct from categoria_pai_id;
