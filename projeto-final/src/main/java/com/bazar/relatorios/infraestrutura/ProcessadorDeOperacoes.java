package com.bazar.relatorios.infraestrutura;

import com.bazar.compartilhado.operacoes.FilaDeOperacoes;
import com.bazar.relatorios.dominio.RelatorioVendasService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * A rede de segurança das operações (19.03): de tempos em tempos, pega o que ficou pendente (o @Async se perdeu num
 * restart, o executor estava cheio) ou o que outra instância deixou pela metade. Com várias instâncias, o SKIP LOCKED
 * da fila garante que cada operação vá para uma só.
 */
@Component
class ProcessadorDeOperacoes {

    private static final Logger log = LoggerFactory.getLogger(ProcessadorDeOperacoes.class);

    private final FilaDeOperacoes fila;
    private final RelatorioVendasService relatorios;

    ProcessadorDeOperacoes(FilaDeOperacoes fila, RelatorioVendasService relatorios) {
        this.fila = fila;
        this.relatorios = relatorios;
    }

    // fixedDelay: a próxima rodada começa um intervalo depois que esta termina; rodadas nunca se sobrepõem.
    @Scheduled(fixedDelayString = "${bazar.operacoes.intervalo}", initialDelayString = "${bazar.operacoes.intervalo}")
    void processarPendentes() {
        // Por enquanto, o relatório de vendas é o único tipo de operação; os reembolsos chegam na 21.02.
        for (var operacao : fila.reservar(5, RelatorioVendasService.PRAZO)) {
            MDC.put("operacao", operacao.id().toString());
            try {
                log.info("Retomando a operação {} (tentativa {})", operacao.id(), operacao.tentativas());
                relatorios.executar(operacao);
            }
            catch (RuntimeException ex) {
                // A operação já foi marcada como FALHOU; a próxima da lista continua.
                log.warn("A operação {} falhou: {}", operacao.id(), ex.getMessage());
            }
            finally {
                MDC.remove("operacao");
            }
        }
    }
}
