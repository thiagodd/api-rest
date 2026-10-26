package com.bazar.pedidos.api;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.bazar.pedidos.dominio.MudancaDeStatus;
import com.bazar.pedidos.dominio.StatusPedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * As conexões SSE abertas nesta instância, por pedido (19.04). Recebe as mudanças de status depois do commit e as
 * entrega a quem acompanha o pedido. Com várias instâncias, cada uma só conhece as próprias conexões: a mudança
 * precisa chegar a todas (Redis pub/sub, LISTEN/NOTIFY do PostgreSQL ou mensageria), assunto da 30.03.
 */
@Component
class AcompanhamentoDePedidos {

    // Uma conexão aberta para sempre é um recurso preso para sempre: depois deste tempo, o servidor encerra, e o
    // EventSource reconecta sozinho, com o Last-Event-ID.
    static final Duration DURACAO_MAXIMA = Duration.ofMinutes(30);

    // O intervalo que o servidor sugere ao EventSource para reconectar (o campo retry).
    static final Duration RECONEXAO = Duration.ofSeconds(3);

    private static final Logger log = LoggerFactory.getLogger(AcompanhamentoDePedidos.class);

    private final Map<UUID, Set<Inscricao>> inscricoes = new ConcurrentHashMap<>();

    /**
     * Abre o acompanhamento: manda o histórico depois de {@code ultimoVisto} e, se o pedido ainda pode mudar, mantém
     * a conexão aberta para as próximas mudanças.
     */
    SseEmitter acompanhar(UUID pedidoId, List<MudancaDeStatus> historico, StatusPedido atual) {
        var emitter = new SseEmitter(DURACAO_MAXIMA.toMillis());
        var inscricao = new Inscricao(pedidoId, emitter);
        if (!atual.ehFinal()) {
            // Inscreve antes de mandar o histórico: uma mudança que chegue no meio fica guardada e sai depois dele.
            inscricoes.computeIfAbsent(pedidoId, id -> ConcurrentHashMap.newKeySet()).add(inscricao);
            emitter.onCompletion(() -> remover(inscricao));
            emitter.onTimeout(emitter::complete);
            emitter.onError(erro -> remover(inscricao));
        }
        inscricao.comecar(historico);
        if (atual.ehFinal()) {
            // Nada mais vai acontecer com este pedido. O EventSource reconecta mesmo assim; a próxima tentativa,
            // com o Last-Event-ID do último status, recebe 204 e para.
            emitter.complete();
        }
        return emitter;
    }

    // Depois do commit: um status desfeito por rollback nunca é anunciado.
    @TransactionalEventListener
    void aoMudarStatus(MudancaDeStatus mudanca) {
        for (var inscricao : inscricoes.getOrDefault(mudanca.pedidoId(), Set.of())) {
            inscricao.enviar(mudanca);
        }
    }

    // Um comentário a cada 15 s: mantém a conexão viva em proxies que fecham conexões ociosas e revela as que
    // morreram sem aviso (o envio falha, e a inscrição sai).
    @Scheduled(fixedDelay = 15_000)
    void manterVivas() {
        inscricoes.values().forEach(doPedido -> doPedido.forEach(Inscricao::batimento));
    }

    int abertas() {
        return inscricoes.values().stream().mapToInt(Set::size).sum();
    }

    private void remover(Inscricao inscricao) {
        inscricoes.computeIfPresent(inscricao.pedidoId, (id, doPedido) -> {
            doPedido.remove(inscricao);
            return doPedido.isEmpty() ? null : doPedido;
        });
    }

    /** Uma conexão: manda os eventos em ordem, sem repetir nenhum, mesmo com o histórico e as novidades se cruzando. */
    private final class Inscricao {

        private final UUID pedidoId;
        private final SseEmitter emitter;
        private final List<MudancaDeStatus> pendentes = new ArrayList<>();
        private boolean aoVivo;
        private long ultimoEnviado;

        Inscricao(UUID pedidoId, SseEmitter emitter) {
            this.pedidoId = pedidoId;
            this.emitter = emitter;
        }

        synchronized void comecar(List<MudancaDeStatus> historico) {
            historico.forEach(this::enviarAgora);
            pendentes.forEach(this::enviarAgora);
            pendentes.clear();
            aoVivo = true;
        }

        synchronized void enviar(MudancaDeStatus mudanca) {
            if (aoVivo) {
                enviarAgora(mudanca);
            }
            else {
                pendentes.add(mudanca);
            }
        }

        synchronized void batimento() {
            try {
                emitter.send(SseEmitter.event().comment("batimento " + Instant.now()));
            }
            catch (IOException | IllegalStateException ex) {
                // O cliente foi embora: a conexão está morta.
                emitter.completeWithError(ex);
            }
        }

        private void enviarAgora(MudancaDeStatus mudanca) {
            if (mudanca.id() <= ultimoEnviado) {
                return;
            }
            try {
                var evento = SseEmitter.event().id(String.valueOf(mudanca.id())).name("status");
                if (ultimoEnviado == 0) {
                    // Só no primeiro evento: o EventSource guarda o valor para as próximas reconexões.
                    evento.reconnectTime(RECONEXAO.toMillis());
                }
                emitter.send(evento.data(EventoDeStatusResponse.de(mudanca), MediaType.APPLICATION_JSON));
                ultimoEnviado = mudanca.id();
                if (mudanca.status().ehFinal()) {
                    // O último capítulo: o servidor encerra o fluxo.
                    emitter.complete();
                }
            }
            catch (IOException | IllegalStateException ex) {
                log.debug("Conexão do pedido {} encerrada: {}", pedidoId, ex.getMessage());
                emitter.completeWithError(ex);
            }
        }
    }
}
