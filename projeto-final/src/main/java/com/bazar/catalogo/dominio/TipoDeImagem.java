package com.bazar.catalogo.dominio;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Os formatos de imagem aceitos, reconhecidos pelos primeiros bytes do arquivo (a "assinatura" ou magic number), e
 * não pelo Content-Type nem pela extensão, que o cliente escolhe à vontade (22.01).
 */
public enum TipoDeImagem {

    JPEG("image/jpeg", "jpg", "ffd8ff"),
    PNG("image/png", "png", "89504e470d0a1a0a");

    /** Quantos bytes ler do começo do arquivo para reconhecer o formato. */
    public static final int BYTES_DA_ASSINATURA = 8;

    private final String tipoConteudo;
    private final String extensao;
    private final byte[] assinatura;

    TipoDeImagem(String tipoConteudo, String extensao, String assinatura) {
        this.tipoConteudo = tipoConteudo;
        this.extensao = extensao;
        this.assinatura = HexFormat.of().parseHex(assinatura);
    }

    public static Optional<TipoDeImagem> reconhecer(byte[] inicio) {
        return Arrays.stream(values())
                .filter(tipo -> inicio.length >= tipo.assinatura.length
                        && Arrays.equals(inicio, 0, tipo.assinatura.length, tipo.assinatura, 0, tipo.assinatura.length))
                .findFirst();
    }

    public String tipoConteudo() {
        return tipoConteudo;
    }

    public String extensao() {
        return extensao;
    }
}
