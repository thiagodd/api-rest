package com.bazar.compartilhado.limites;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * As políticas de rate limiting do Bazar (23.01), em bazar.limites. Cada uma é um token bucket: até capacidade
 * requisições de uma vez, reabastecido aos poucos, à razão de capacidade por janela.
 *
 * @param geral vale para todas as rotas da API
 * @param exportacao vale, além da geral, para a exportação do catálogo (22.02), que custa um cursor no banco
 */
@ConfigurationProperties("bazar.limites")
public record PoliticasDeLimite(Politica geral, Politica exportacao) {

    public record Politica(long capacidade, Duration janela) {
    }
}
