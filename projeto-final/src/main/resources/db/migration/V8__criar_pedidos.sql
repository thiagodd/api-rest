-- Pedidos e itens (10.03).

create table pedido (
    id          uuid           primary key,
    -- Sem chave estrangeira: os clientes ainda não estão no banco.
    cliente_id  uuid           not null,
    status      varchar(30)    not null,
    moeda       varchar(3)     not null,
    valor_itens numeric(12, 2) not null,
    valor_frete numeric(12, 2) not null,
    valor_total numeric(12, 2) not null,
    versao      bigint         not null,
    criado_em   timestamptz    not null
);

create table item_pedido (
    id             uuid           primary key,
    pedido_id      uuid           not null references pedido (id),
    posicao        integer        not null,
    produto_id     uuid           not null references produto (id),
    sku            varchar(8)     not null,
    nome_produto   varchar(120)   not null,
    quantidade     integer        not null check (quantidade > 0),
    preco_unitario numeric(12, 2) not null,
    subtotal       numeric(12, 2) not null,
    unique (pedido_id, posicao)
);

create index item_pedido_produto_id_idx on item_pedido (produto_id);
