-- Catálogo: categorias e produtos (10.02).
-- Os tipos seguem o mapeamento das entidades; o Hibernate só confere (ddl-auto: validate).

create table categoria (
    id               uuid         primary key,
    nome             varchar(60)  not null,
    slug             varchar(60)  not null unique,
    categoria_pai_id uuid         references categoria (id),
    versao           bigint       not null
);

create table produto (
    id            uuid           primary key,
    sku           varchar(8)     not null unique,
    nome          varchar(120)   not null,
    descricao     varchar(2000),
    preco_valor   numeric(12, 2) not null check (preco_valor > 0),
    preco_moeda   varchar(3)     not null,
    categoria_id  uuid           not null references categoria (id),
    status        varchar(20)    not null check (status in ('RASCUNHO', 'ATIVO', 'DESCONTINUADO')),
    atributos     jsonb          not null default '{}',
    versao        bigint         not null,
    criado_em     timestamptz    not null,
    atualizado_em timestamptz    not null
);

-- O PostgreSQL não cria índice para chaves estrangeiras. Este atende à contagem de produtos
-- por categoria (o 409 ao remover uma categoria) e, depois, ao filtro por categoria (13.01).
create index produto_categoria_id_idx on produto (categoria_id);
