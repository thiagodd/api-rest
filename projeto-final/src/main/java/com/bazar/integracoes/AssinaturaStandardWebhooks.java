package com.bazar.integracoes;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * A assinatura HMAC do Standard Webhooks (21.03): HMAC-SHA256 de "webhook-id.webhook-timestamp.corpo", com o segredo
 * "whsec_" + base64. O header webhook-signature leva "v1," + o HMAC em base64; pode trazer várias, separadas por
 * espaço, durante a troca de um segredo.
 */
public final class AssinaturaStandardWebhooks {

    private static final String PREFIXO = "whsec_";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final byte[] chave;

    public AssinaturaStandardWebhooks(String segredo) {
        this.chave = Base64.getDecoder().decode(segredo.startsWith(PREFIXO) ? segredo.substring(PREFIXO.length()) : segredo);
    }

    /** 32 bytes aleatórios: o tamanho da saída do SHA-256. */
    public static String novoSegredo() {
        var bytes = new byte[32];
        ALEATORIO.nextBytes(bytes);
        return PREFIXO + Base64.getEncoder().encodeToString(bytes);
    }

    public String assinar(String id, long timestamp, byte[] corpo) {
        return "v1," + Base64.getEncoder().encodeToString(hmac(id, timestamp, corpo));
    }

    /**
     * Confere a assinatura e o horário. Um timestamp fora da tolerância é recusado mesmo com a assinatura certa: é uma
     * notificação antiga sendo reenviada por alguém que a capturou (replay).
     */
    public boolean verificar(String id, String timestamp, byte[] corpo, String assinaturas, Instant agora,
            Duration tolerancia) {
        long segundos;
        try {
            segundos = Long.parseLong(timestamp);
        }
        catch (NumberFormatException ex) {
            return false;
        }
        if (Duration.between(Instant.ofEpochSecond(segundos), agora).abs().compareTo(tolerancia) > 0) {
            return false;
        }
        var esperada = hmac(id, segundos, corpo);
        for (var assinatura : assinaturas.split(" ")) {
            if (assinatura.startsWith("v1,")) {
                try {
                    // Comparação em tempo constante: um equals comum para no primeiro byte diferente, e o tempo de
                    // resposta revelaria quantos bytes já estão certos.
                    if (MessageDigest.isEqual(esperada, Base64.getDecoder().decode(assinatura.substring(3)))) {
                        return true;
                    }
                }
                catch (IllegalArgumentException ex) {
                    // Base64 inválido: não é esta.
                }
            }
        }
        return false;
    }

    private byte[] hmac(String id, long timestamp, byte[] corpo) {
        try {
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(chave, "HmacSHA256"));
            mac.update((id + "." + timestamp + ".").getBytes(StandardCharsets.UTF_8));
            return mac.doFinal(corpo);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
