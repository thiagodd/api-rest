-- Paginação por cursor (13.04): cada listagem tem um índice na ordem dela, para que o banco vá direto à posição
-- do cursor e leia só a página.

-- Os pedidos de um cliente, dos mais recentes para os mais antigos.
create index pedido_cliente_criado_em_id_idx on pedido (cliente_id, criado_em desc, id desc);

-- Os produtos na ordenação padrão (sort=nome). As outras ordenações usam o índice do sku (unique), ou ordenam o
-- resultado dos filtros; um índice para cada combinação custaria mais nas escritas do que economizaria.
create index produto_nome_id_idx on produto (nome, id);
