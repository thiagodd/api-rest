package com.bazar.relatorios.dominio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.bazar.compartilhado.operacoes.FilaDeOperacoes;
import com.bazar.compartilhado.operacoes.Operacao;
import com.bazar.relatorios.infraestrutura.ConsultaDeVendas;
import com.bazar.relatorios.infraestrutura.VendasDoProduto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * O relatório de vendas por produto (19.03). A requisição só registra a operação; o arquivo é gerado numa thread do
 * executor de tarefas (@Async), e o ProcessadorDeOperacoes retoma o que tiver ficado para trás.
 */
@Service
public class RelatorioVendasService {

    public static final String TIPO = "RELATORIO_VENDAS";

    // Tempo que uma instância tem para gerar o relatório antes que outra possa retomá-lo.
    public static final Duration PRAZO = Duration.ofMinutes(5);

    private static final Logger log = LoggerFactory.getLogger(RelatorioVendasService.class);

    private final FilaDeOperacoes fila;
    private final ConsultaDeVendas consulta;
    private final JsonMapper json;
    private final Path diretorio;

    public RelatorioVendasService(FilaDeOperacoes fila, ConsultaDeVendas consulta, JsonMapper json,
            @Value("${bazar.relatorios.diretorio}") Path diretorio) {
        this.fila = fila;
        this.consulta = consulta;
        this.json = json;
        this.diretorio = diretorio;
    }

    /** Registra o pedido do relatório. Depois deste insert, o trabalho não se perde, nem com um restart. */
    public UUID solicitar(PeriodoDeVendas periodo) {
        return fila.enfileirar(TIPO, json.writeValueAsString(periodo));
    }

    /**
     * Gera o relatório numa thread do executor de tarefas, sem segurar a requisição. Só gera se conseguir reservar a
     * operação: se o ProcessadorDeOperacoes já a pegou, não há nada a fazer.
     */
    @Async
    public CompletableFuture<Optional<Path>> gerarAsync(UUID operacao) {
        return CompletableFuture.completedFuture(fila.reservar(operacao, PRAZO).map(this::executar));
    }

    /** Gera o arquivo de uma operação já reservada e registra o resultado. Uma falha marca a operação como FALHOU. */
    public Path executar(Operacao operacao) {
        try {
            var periodo = json.readValue(operacao.parametros(), PeriodoDeVendas.class);
            var arquivo = escrever(operacao.id(), consulta.porProduto(periodo.de(), periodo.ate()));
            fila.concluir(operacao.id(), arquivo.toString());
            log.info("Relatório de vendas {} gerado em {}", operacao.id(), arquivo);
            return arquivo;
        }
        catch (RuntimeException ex) {
            fila.falhar(operacao.id(), ex.getMessage());
            throw ex;
        }
    }

    private Path escrever(UUID operacao, Iterable<VendasDoProduto> vendas) {
        try {
            Files.createDirectories(diretorio);
            var arquivo = diretorio.resolve("vendas-" + operacao + ".csv");
            try (var saida = Files.newBufferedWriter(arquivo)) {
                saida.write("produtoId,sku,nomeProduto,pedidos,unidades,receita\n");
                for (var linha : vendas) {
                    saida.write("%s,%s,\"%s\",%d,%d,%s%n".formatted(linha.produtoId(), linha.sku(),
                            linha.nomeProduto().replace("\"", "\"\""), linha.pedidos(), linha.unidades(),
                            linha.receita().toPlainString()));
                }
            }
            return arquivo;
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
