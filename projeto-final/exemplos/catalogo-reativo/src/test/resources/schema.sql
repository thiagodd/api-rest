-- Só as colunas que o exemplo lê: o esquema completo é o das migrações do Bazar.
create table produto (
    id            uuid           primary key,
    sku           varchar(8)     not null unique,
    nome          varchar(120)   not null,
    descricao     varchar(2000),
    preco_valor   numeric(12, 2) not null,
    preco_moeda   varchar(3)     not null,
    categoria_id  uuid           not null,
    status        varchar(20)    not null,
    atualizado_em timestamptz    not null
);

insert into produto values
    ('bcd399f5-d6d1-4032-9c80-10ca4c363d31', 'LIV-0042', 'Clean Code (2ª ed.)', null, 139.90, 'BRL',
     '0192f3a0-5555-7000-8000-000000000003', 'ATIVO', '2026-10-25T04:14:00Z'),
    ('01a177a2-4072-7904-b8db-45de372442b0', 'LIV-0077', 'Java Concurrency in Practice', null, 189.90, 'BRL',
     '0192f3a0-5555-7000-8000-000000000003', 'ATIVO', '2026-10-26T09:54:44Z');
