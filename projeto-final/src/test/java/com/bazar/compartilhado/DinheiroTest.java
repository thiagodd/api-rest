package com.bazar.compartilhado;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class DinheiroTest {

    static final Currency REAL = Currency.getInstance("BRL");

    @Nested
    class Escala {

        // Com a escala da moeda fixada no construtor, 129.9 e 129.90 são o mesmo Dinheiro.
        @Test
        void normalizaParaAsCasasDaMoeda() {
            assertThat(Dinheiro.de("129.9", "BRL")).isEqualTo(Dinheiro.de("129.90", "BRL"));
            assertThat(Dinheiro.de("129.9", "BRL").valor()).hasToString("129.90");
            assertThat(Dinheiro.de("1500", "JPY").valor()).hasToString("1500");
        }

        // BigDecimal.equals compara a escala; compareTo, só o valor. Por isso a escala é fixada.
        @Test
        void bigDecimalSemEscalaFixaDiferenciaZerosAEsquerda() {
            assertThat(new BigDecimal("129.9")).isNotEqualTo(new BigDecimal("129.90"));
            assertThat(new BigDecimal("129.9")).isEqualByComparingTo(new BigDecimal("129.90"));
        }

        @Test
        void recusaMaisCasasDoQueAMoedaTem() {
            assertThatIllegalArgumentException().isThrownBy(() -> Dinheiro.de("129.999", "BRL"))
                    .withMessageContaining("129.999");
            assertThatIllegalArgumentException().isThrownBy(() -> Dinheiro.de("1500.5", "JPY"));
            assertThat(Dinheiro.de("129.900", "BRL").valor()).hasToString("129.90");
        }
    }

    @Test
    void somaEMultiplicaNaMesmaMoeda() {
        var preco = Dinheiro.de("129.90", "BRL");
        assertThat(preco.multiplicar(3).somar(Dinheiro.de("0.30", "BRL"))).isEqualTo(Dinheiro.de("390.00", "BRL"));
        assertThatIllegalArgumentException().isThrownBy(() -> preco.somar(Dinheiro.de("10", "USD")));
    }

    // O que um double faria com o mesmo cálculo.
    @Test
    void doubleErraOsCentavos() {
        assertThat(0.1 + 0.2).isNotEqualTo(0.3);
        assertThat(Dinheiro.de("0.10", "BRL").somar(Dinheiro.de("0.20", "BRL"))).isEqualTo(Dinheiro.de("0.30", "BRL"));
    }

    @Nested
    class Rateio {

        // R$ 10,00 em três partes iguais: 3,34 + 3,33 + 3,33. O centavo que sobra vai para a primeira.
        @Test
        void naoPerdeNemCriaCentavos() {
            var partes = Dinheiro.de("10.00", "BRL").ratear(List.of(1L, 1L, 1L));
            assertThat(partes).containsExactly(Dinheiro.de("3.34", "BRL"), Dinheiro.de("3.33", "BRL"),
                    Dinheiro.de("3.33", "BRL"));
        }

        // Um frete de R$ 15,00 rateado pelo valor de cada item (R$ 129,90, R$ 49,90 e R$ 9,90). As partes exatas são
        // 1027,15, 394,57 e 78,28 centavos: arredondadas para baixo, somam 1499, e o centavo que falta vai para
        // o maior resto (o segundo item).
        @Test
        void rateiaProporcionalmenteAoPeso() {
            var partes = Dinheiro.de("15.00", "BRL").ratear(List.of(12990L, 4990L, 990L));
            assertThat(partes).containsExactly(Dinheiro.de("10.27", "BRL"), Dinheiro.de("3.95", "BRL"),
                    Dinheiro.de("0.78", "BRL"));
            assertThat(partes.stream().map(Dinheiro::valor).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .isEqualByComparingTo("15.00");
        }

        @Test
        void recusaPesosQueNaoSomamNada() {
            assertThatIllegalArgumentException().isThrownBy(() -> Dinheiro.de("10", "BRL").ratear(List.of(0L, 0L)));
            assertThatIllegalArgumentException().isThrownBy(() -> Dinheiro.de("10", "BRL").ratear(List.of()));
        }
    }
}
