package com.bazar.compartilhado;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import jakarta.persistence.Embeddable;

/**
 * Um valor monetário com a sua moeda (15.02). Nunca use double para dinheiro (02.06).
 * <p>
 * O valor sempre tem a escala da moeda (2 casas no real, 0 no iene): "129.9" vira 129.90, e um valor com mais
 * casas do que a moeda tem ("129.999" em BRL) é recusado, porque arredondar em silêncio muda o preço. Com a escala
 * fixa, o equals do record (que usa o equals do BigDecimal, sensível à escala) dá a resposta certa.
 * <p>
 * Como @Embeddable, vira colunas da tabela de quem o usa (o produto: preco_valor e preco_moeda).
 */
@Embeddable
public record Dinheiro(BigDecimal valor, Currency moeda) {

    public Dinheiro {
        Objects.requireNonNull(valor, "valor");
        Objects.requireNonNull(moeda, "moeda");
        var casas = moeda.getDefaultFractionDigits();
        if (valor.stripTrailingZeros().scale() > casas) {
            throw new IllegalArgumentException(
                    "%s tem mais casas decimais do que %s permite (%d)".formatted(valor.toPlainString(), moeda, casas));
        }
        valor = valor.setScale(casas, RoundingMode.UNNECESSARY);
    }

    public static Dinheiro de(String valor, String codigoMoeda) {
        return new Dinheiro(new BigDecimal(valor), Currency.getInstance(codigoMoeda));
    }

    public static Dinheiro deCentavos(long centavos, String codigoMoeda) {
        var moeda = Currency.getInstance(codigoMoeda);
        return new Dinheiro(BigDecimal.valueOf(centavos, moeda.getDefaultFractionDigits()), moeda);
    }

    public long emCentavos() {
        return valor.movePointRight(moeda.getDefaultFractionDigits()).longValueExact();
    }

    public Dinheiro somar(Dinheiro outro) {
        exigirMesmaMoeda(outro);
        return new Dinheiro(valor.add(outro.valor), moeda);
    }

    public Dinheiro multiplicar(int quantidade) {
        return new Dinheiro(valor.multiply(BigDecimal.valueOf(quantidade)), moeda);
    }

    /**
     * Divide o valor em partes proporcionais aos pesos, sem perder nem criar centavos: a soma das partes é sempre
     * o valor. Cada parte é arredondada para baixo, e os centavos que sobram vão, um a um, para as partes com o maior
     * resto (o método do maior resto). Usado para ratear um frete ou um desconto entre os itens de um pedido.
     */
    public List<Dinheiro> ratear(List<Long> pesos) {
        if (pesos.isEmpty() || pesos.stream().anyMatch(peso -> peso < 0) || pesos.stream().mapToLong(p -> p).sum() == 0) {
            throw new IllegalArgumentException("Os pesos precisam ser positivos e somar mais que zero: " + pesos);
        }
        var total = BigDecimal.valueOf(pesos.stream().mapToLong(p -> p).sum());
        var centavos = BigDecimal.valueOf(emCentavos());
        var partes = new ArrayList<Long>();
        var restos = new ArrayList<BigDecimal>();
        for (var peso : pesos) {
            var exata = centavos.multiply(BigDecimal.valueOf(peso)).divide(total, 10, RoundingMode.DOWN);
            var inteira = exata.setScale(0, RoundingMode.DOWN);
            partes.add(inteira.longValueExact());
            restos.add(exata.subtract(inteira));
        }
        var sobra = emCentavos() - partes.stream().mapToLong(p -> p).sum();
        // Em caso de empate no resto, o primeiro item leva o centavo: o resultado não depende da ordenação do Java.
        IntStream.range(0, pesos.size()).boxed()
                .sorted(Comparator.comparing((Integer i) -> restos.get(i)).reversed().thenComparing(i -> i))
                .limit(sobra)
                .forEach(i -> partes.set(i, partes.get(i) + 1));
        return partes.stream().map(parte -> Dinheiro.deCentavos(parte, moeda.getCurrencyCode())).toList();
    }

    private void exigirMesmaMoeda(Dinheiro outro) {
        if (!moeda.equals(outro.moeda)) {
            throw new IllegalArgumentException("Moedas diferentes: " + moeda + " e " + outro.moeda);
        }
    }
}
