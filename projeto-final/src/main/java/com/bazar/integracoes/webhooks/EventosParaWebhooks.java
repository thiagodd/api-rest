package com.bazar.integracoes.webhooks;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.Ids;
import com.bazar.pedidos.dominio.PedidoCriado;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Transforma eventos do domínio em entregas de webhook (21.03).
 * <p>
 * É um @EventListener comum, e não um @TransactionalEventListener: roda na hora do publishEvent, dentro da transação
 * que criou o pedido. A entrega é gravada no mesmo commit do pedido (o padrão outbox). Se o pedido for desfeito, a
 * entrega também é; se o processo cair logo depois do commit, a entrega já está na tabela, e o entregador a envia
 * quando voltar.
 */
@Component
class EventosParaWebhooks {

    static final String PEDIDO_CRIADO = "pedido.criado";

    private final EntregasDeWebhook entregas;
    private final JsonMapper json;
    private final Clock relogio;

    EventosParaWebhooks(EntregasDeWebhook entregas, JsonMapper json, Clock relogio) {
        this.entregas = entregas;
        this.json = json;
        this.relogio = relogio;
    }

    // O formato do Standard Webhooks: type, timestamp e data.
    record Evento(String type, Instant timestamp, Object data) {
    }

    // Um evento "magro": o essencial para o parceiro decidir o que fazer, e o id para buscar o resto na API, com as
    // permissões dele. Nada de dados pessoais do cliente num POST para uma URL de terceiro.
    record DadosDoPedidoCriado(UUID pedidoId, Dinheiro valorTotal) {
    }

    @EventListener
    void aoCriarPedido(PedidoCriado evento) {
        var agora = relogio.instant();
        var corpo = json.writeValueAsString(new Evento(PEDIDO_CRIADO, agora,
                new DadosDoPedidoCriado(evento.pedidoId(), evento.valorTotal())));
        entregas.enfileirar(PEDIDO_CRIADO, Ids.novo(), corpo, agora);
    }
}
