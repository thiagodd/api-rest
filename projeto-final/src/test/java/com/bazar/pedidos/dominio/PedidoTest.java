package com.bazar.pedidos.dominio;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.bazar.compartilhado.Dinheiro;
import com.bazar.compartilhado.erros.TipoProblema;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import static com.bazar.catalogo.dominio.ProdutoDeTeste.umProduto;
import static com.bazar.pedidos.dominio.PedidoDeTeste.umPedido;
import static com.bazar.pedidos.dominio.StatusPedido.CANCELADO;
import static com.bazar.pedidos.dominio.StatusPedido.CRIADO;
import static com.bazar.pedidos.dominio.StatusPedido.ENTREGUE;
import static com.bazar.pedidos.dominio.StatusPedido.ENVIADO;
import static com.bazar.pedidos.dominio.StatusPedido.PAGAMENTO_PENDENTE;
import static com.bazar.pedidos.dominio.StatusPedido.PAGO;
import static com.bazar.pedidos.dominio.StatusPedido.REEMBOLSADO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

// Um teste unitário: sem Spring, sem banco. Roda em milissegundos.
class PedidoTest {

    static final Instant AGORA = Instant.parse("2026-10-25T14:00:00Z");

    @Nested
    class Valores {

        @Test
        void somaOsSubtotaisDosItensNoTotal() {
            var pedido = umPedido()
                    .com(umProduto().comSku("LIV-0042").comPreco(12990).build(), 2)
                    .com(umProduto().comSku("LIV-0043").comPreco(5000).build(), 1)
                    .build();

            assertThat(pedido.getValorItens()).isEqualTo(Dinheiro.deCentavos(30980, "BRL"));
            assertThat(pedido.getValorFrete()).isEqualTo(Dinheiro.deCentavos(0, "BRL"));
            assertThat(pedido.getValorTotal()).isEqualTo(Dinheiro.deCentavos(30980, "BRL"));
        }

        @ParameterizedTest(name = "{1} × R$ {0} centavos = R$ {2}")
        @CsvSource(textBlock = """
                1990,  3, 59.70
                1,     1, 0.01
                12990, 100, 12990.00
                """)
        void calculaOSubtotalDeCadaItem(long precoEmCentavos, int quantidade, String subtotal) {
            var pedido = umPedido().com(umProduto().comPreco(precoEmCentavos).build(), quantidade).build();

            assertThat(pedido.getItens()).singleElement()
                    .satisfies(item -> assertThat(item.getSubtotal()).hasToString(subtotal));
        }

        @Test
        void guardaOPrecoDoMomentoDaCompra() {
            var produto = umProduto().comPreco(12990).build();
            var pedido = umPedido().com(produto, 1).build();

            produto.atualizar(produto.getNome(), null, Dinheiro.deCentavos(15990, "BRL"), produto.getCategoriaId(), Map.of());

            assertThat(pedido.getItens().getFirst().getPrecoUnitario()).hasToString("129.90");
            assertThat(pedido.getValorTotal()).isEqualTo(Dinheiro.deCentavos(12990, "BRL"));
        }
    }

    @Nested
    class Transicoes {

        // As ações do pedido e o status a que cada uma leva.
        enum Acao {
            INICIAR_PAGAMENTO(Pedido::iniciarPagamento, PAGAMENTO_PENDENTE),
            CONFIRMAR_PAGAMENTO(Pedido::confirmarPagamento, PAGO),
            ENVIAR(Pedido::enviar, ENVIADO),
            CONFIRMAR_ENTREGA(Pedido::confirmarEntrega, ENTREGUE),
            CANCELAR(pedido -> pedido.cancelar(MotivoCancelamento.CLIENTE_DESISTIU, null, AGORA), CANCELADO),
            REEMBOLSAR(Pedido::reembolsar, REEMBOLSADO);

            final Consumer<Pedido> executar;
            final StatusPedido destino;

            Acao(Consumer<Pedido> executar, StatusPedido destino) {
                this.executar = executar;
                this.destino = destino;
            }
        }

        // A tabela de transições, copiada da especificação (PROJETO-FINAL.md), e não do StatusPedido:
        // se o teste lesse a regra do próprio código, um erro na regra passaria nos dois.
        static final Map<StatusPedido, Set<Acao>> PERMITIDAS = Map.of(
                CRIADO, Set.of(Acao.INICIAR_PAGAMENTO, Acao.CANCELAR),
                PAGAMENTO_PENDENTE, Set.of(Acao.CONFIRMAR_PAGAMENTO, Acao.CANCELAR),
                PAGO, Set.of(Acao.ENVIAR, Acao.REEMBOLSAR),
                ENVIADO, Set.of(Acao.CONFIRMAR_ENTREGA));

        static Stream<Arguments> transicoesPermitidas() {
            return PERMITIDAS.entrySet().stream()
                    .flatMap(e -> e.getValue().stream().map(acao -> arguments(e.getKey(), acao)));
        }

        static Stream<Arguments> transicoesProibidas() {
            return Arrays.stream(StatusPedido.values())
                    .flatMap(origem -> Arrays.stream(Acao.values())
                            .filter(acao -> !PERMITIDAS.getOrDefault(origem, Set.of()).contains(acao))
                            .map(acao -> arguments(origem, acao)));
        }

        @Test
        void comecaComoCriado() {
            assertThat(umPedido().build().getStatus()).isEqualTo(CRIADO);
        }

        @ParameterizedTest(name = "{0} --{1}--> permitido")
        @MethodSource("transicoesPermitidas")
        void mudaOStatusNasTransicoesPermitidas(StatusPedido origem, Acao acao) {
            var pedido = umPedido().noStatus(origem).build();

            acao.executar.accept(pedido);

            assertThat(pedido.getStatus()).isEqualTo(acao.destino);
        }

        @ParameterizedTest(name = "{0} --{1}--> proibido")
        @MethodSource("transicoesProibidas")
        void recusaAsOutrasTransicoesSemMudarOStatus(StatusPedido origem, Acao acao) {
            var pedido = umPedido().noStatus(origem).build();

            var erro = catchThrowableOfType(TransicaoInvalidaException.class, () -> acao.executar.accept(pedido));

            assertThat(erro).isNotNull();
            assertThat(erro.getTipo()).isEqualTo(TipoProblema.TRANSICAO_INVALIDA);
            assertThat(erro.getExtensoes()).containsEntry("statusAtual", origem).containsEntry("statusDesejado", acao.destino);
            assertThat(pedido.getStatus()).isEqualTo(origem);
        }

        @ParameterizedTest
        @EnumSource(names = { "ENTREGUE", "CANCELADO", "REEMBOLSADO" })
        void estadosFinaisNaoSaemParaNenhumOutro(StatusPedido finalizado) {
            assertThat(StatusPedido.values()).noneMatch(finalizado::podeIrPara);
        }
    }

    @Nested
    class Cancelamento {

        @Test
        void registraOMotivoEOMomento() {
            var pedido = umPedido().build();

            pedido.cancelar(MotivoCancelamento.OUTRO, "Comprei em outra loja", AGORA);

            assertThat(pedido.getStatus()).isEqualTo(CANCELADO);
            assertThat(pedido.getCancelamento()).isNotNull()
                    .returns(MotivoCancelamento.OUTRO, c -> c.motivo())
                    .returns("Comprei em outra loja", c -> c.observacao())
                    .satisfies(c -> assertThat(c.canceladoEm()).isNotNull());
        }

        @Test
        void umCancelamentoRecusadoNaoDeixaRastro() {
            var pedido = umPedido().noStatus(PAGO).build();

            catchThrowableOfType(TransicaoInvalidaException.class,
                    () -> pedido.cancelar(MotivoCancelamento.CLIENTE_DESISTIU, null, AGORA));

            assertThat(pedido.getStatus()).isEqualTo(PAGO);
            assertThat(pedido.getCancelamento()).isNull();
        }
    }
}
