-- Estoque de cada produto (10.02). A entidade e as regras de reserva chegam em 10.06.

create table estoque (
    produto_id            uuid    primary key references produto (id),
    quantidade_disponivel integer not null default 0 check (quantidade_disponivel >= 0),
    quantidade_reservada  integer not null default 0 check (quantidade_reservada >= 0),
    versao                bigint  not null default 0
);
