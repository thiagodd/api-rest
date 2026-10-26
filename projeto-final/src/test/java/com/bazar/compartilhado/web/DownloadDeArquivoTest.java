package com.bazar.compartilhado.web;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import org.springframework.http.ETag;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadDeArquivoTest {

    static final ETag VERSAO = ETag.create("\"01a178bf\"");
    static final Instant MODIFICADO = Instant.parse("2026-10-26T15:05:57.429Z");

    @Test
    void semIfRangeORangeVale() {
        assertThat(DownloadDeArquivo.ifRangeConfere(null, VERSAO, MODIFICADO)).isTrue();
    }

    @Test
    void comAMesmaEtagORangeVale() {
        assertThat(DownloadDeArquivo.ifRangeConfere("\"01a178bf\"", VERSAO, MODIFICADO)).isTrue();
    }

    @Test
    void umaEtagFracaNuncaConfere() {
        // A comparação do If-Range é forte: uma ETag fraca não garante que os bytes são os mesmos.
        assertThat(DownloadDeArquivo.ifRangeConfere("W/\"01a178bf\"", VERSAO, MODIFICADO)).isFalse();
    }

    @Test
    void comAData() {
        assertThat(DownloadDeArquivo.ifRangeConfere("Mon, 26 Oct 2026 15:05:57 GMT", VERSAO, MODIFICADO)).isTrue();
        assertThat(DownloadDeArquivo.ifRangeConfere("Mon, 26 Oct 2026 15:05:56 GMT", VERSAO, MODIFICADO)).isFalse();
        assertThat(DownloadDeArquivo.ifRangeConfere("ontem", VERSAO, MODIFICADO)).isFalse();
    }
}
