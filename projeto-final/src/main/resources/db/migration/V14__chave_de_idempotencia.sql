-- A resposta de cada requisição com Idempotency-Key já processada (21.01). O escopo é o método e a rota: a mesma
-- chave em rotas diferentes são chaves diferentes. Uma repetição dentro da validade recebe esta resposta de novo.
create table chave_idempotencia (
    escopo           varchar(200) not null,
    chave            varchar(255) not null,
    hash_requisicao  varchar(64)  not null,
    status_resposta  smallint     not null,
    headers_resposta text         not null,
    corpo_resposta   bytea        not null,
    criada_em        timestamptz  not null,
    expira_em        timestamptz  not null,
    primary key (escopo, chave)
);

create index chave_idempotencia_expira_em_idx on chave_idempotencia (expira_em);
