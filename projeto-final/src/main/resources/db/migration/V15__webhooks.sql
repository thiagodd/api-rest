-- Webhooks (21.03). Saída: quem quer ser avisado (assinatura_webhook) e cada aviso a entregar (entrega_webhook), que
-- é gravado na mesma transação do evento: um outbox. Entrada: as notificações da PagaFácil já recebidas, para
-- reconhecer uma repetição pelo webhook-id.

create table assinatura_webhook (
    id            uuid          primary key,
    parceiro      varchar(100)  not null,
    url           varchar(2000) not null,
    -- Os tipos de evento, separados por vírgula: pedido.criado
    eventos       varchar(500)  not null,
    -- O segredo do HMAC. Precisa ser lido para assinar, então não pode ser um hash; em produção, cifrado (26.x).
    segredo       varchar(100)  not null,
    ativa         boolean       not null,
    criada_em     timestamptz   not null,
    desativada_em timestamptz
);

create table entrega_webhook (
    id                   uuid         primary key,
    assinatura_id        uuid         not null references assinatura_webhook (id),
    evento_id            uuid         not null,
    tipo_evento          varchar(60)  not null,
    corpo                text         not null,
    status               varchar(20)  not null check (status in ('PENDENTE', 'ENTREGUE', 'FALHOU')),
    tentativas           integer      not null default 0,
    ultimo_status_http   integer,
    ultimo_erro          varchar(500),
    proxima_tentativa_em timestamptz  not null,
    criada_em            timestamptz  not null,
    entregue_em          timestamptz
);

-- O entregador só procura entregas pendentes que já podem ser tentadas.
create index entrega_webhook_a_enviar_idx on entrega_webhook (proxima_tentativa_em) where status = 'PENDENTE';
create index entrega_webhook_assinatura_idx on entrega_webhook (assinatura_id, criada_em desc);

create table notificacao_recebida (
    webhook_id    varchar(255) not null,
    origem        varchar(40)  not null,
    corpo         text         not null,
    recebida_em   timestamptz  not null,
    processada_em timestamptz,
    primary key (origem, webhook_id)
);
