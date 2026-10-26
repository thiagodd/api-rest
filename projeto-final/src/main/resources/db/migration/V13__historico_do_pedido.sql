-- As mudanças de status de cada pedido, em ordem (19.04). O id crescente é o id dos eventos do acompanhamento em
-- tempo real: o cliente que reconecta manda o último que viu (Last-Event-ID) e recebe só os seguintes.

create table historico_pedido (
    id          bigint       generated always as identity primary key,
    pedido_id   uuid         not null references pedido (id),
    status      varchar(30)  not null,
    ocorrido_em timestamptz  not null
);

create index historico_pedido_pedido_id_idx on historico_pedido (pedido_id, id);

-- Os pedidos que já existiam ganham o histórico que dá para reconstruir.
insert into historico_pedido (pedido_id, status, ocorrido_em)
select id, 'CRIADO', criado_em from pedido order by criado_em, id;

insert into historico_pedido (pedido_id, status, ocorrido_em)
select id, 'CANCELADO', cancelado_em from pedido where status = 'CANCELADO' order by cancelado_em, id;
