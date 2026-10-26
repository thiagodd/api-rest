package com.bazar.pedidos.dominio;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * O histórico de status dos pedidos (19.04), gravado na mesma transação que muda o status.
 */
@Repository
public class HistoricoDoPedido {

    private final JdbcClient jdbc;

    public HistoricoDoPedido(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // O instante volta do banco: o timestamptz guarda microssegundos, e o evento ao vivo precisa ser igual ao que a
    // reconexão lê do histórico.
    public MudancaDeStatus registrar(Pedido pedido, Instant quando) {
        return jdbc.sql("""
                insert into historico_pedido (pedido_id, status, ocorrido_em)
                values (:pedido, :status, :quando)
                returning id, pedido_id, :numero as numero, status, ocorrido_em
                """)
                .param("pedido", pedido.getId()).param("status", pedido.getStatus().name())
                .param("quando", quando.atOffset(ZoneOffset.UTC)).param("numero", pedido.getNumero())
                .query(MudancaDeStatus.class).single();
    }

    /** As mudanças do pedido com id maior que {@code depoisDe}, em ordem. Com 0, o histórico inteiro. */
    public List<MudancaDeStatus> depoisDe(UUID pedidoId, long depoisDe) {
        return jdbc.sql("""
                select h.id, h.pedido_id, p.numero, h.status, h.ocorrido_em
                  from historico_pedido h
                  join pedido p on p.id = h.pedido_id
                 where h.pedido_id = :pedido and h.id > :depoisDe
                 order by h.id
                """)
                .param("pedido", pedidoId).param("depoisDe", depoisDe)
                .query(MudancaDeStatus.class).list();
    }
}
