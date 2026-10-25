-- O cancelamento do pedido (12.04): preenchido só quando o status é CANCELADO.

alter table pedido
    add column cancelamento_motivo     varchar(30),
    add column cancelamento_observacao varchar(500),
    add column cancelado_em            timestamptz;
