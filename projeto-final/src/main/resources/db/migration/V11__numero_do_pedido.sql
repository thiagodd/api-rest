-- O número do pedido para pessoas (15.03): BZR-2026-000123. O id (UUID v7) continua sendo o identificador da API;
-- o número é o que o cliente fala no telefone e o que vai na nota fiscal.
create sequence pedido_numero_seq;

alter table pedido add column numero varchar(20);

-- Os pedidos que já existem ganham números na ordem em que foram criados.
with numerados as (
    select id, criado_em, nextval('pedido_numero_seq') as sequencial
      from (select id, criado_em from pedido order by criado_em, id) em_ordem
)
update pedido p
   set numero = 'BZR-' || to_char(n.criado_em at time zone 'America/Sao_Paulo', 'YYYY') || '-'
                || lpad(n.sequencial::text, 6, '0')
  from numerados n
 where n.id = p.id;

alter table pedido alter column numero set not null;
alter table pedido add constraint pedido_numero_unico unique (numero);
