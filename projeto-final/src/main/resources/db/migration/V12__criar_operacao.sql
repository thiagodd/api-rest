-- Operações demoradas executadas fora da requisição (19.03). A linha é a garantia de que o trabalho não se perde:
-- se a instância cair no meio, outra retoma a operação quando o prazo de reserva vencer.

create table operacao (
    id            uuid         primary key,
    tipo          varchar(40)  not null,
    status        varchar(20)  not null,
    -- Os parâmetros da operação, em JSON: cada tipo tem os seus.
    parametros    text         not null,
    tentativas    integer      not null default 0,
    -- Até quando a instância que reservou a operação tem exclusividade sobre ela.
    reservada_ate timestamptz,
    resultado     text,
    erro          text,
    criada_em     timestamptz  not null,
    atualizada_em timestamptz  not null
);

-- O processador só procura operações que ainda podem rodar.
create index operacao_a_executar_idx on operacao (criada_em) where status in ('PENDENTE', 'EM_EXECUCAO');
