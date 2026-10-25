package com.bazar.pedidos.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.bazar.compartilhado.erros.CursorInvalidoException;
import com.bazar.compartilhado.web.CodificadorDeCursor;
import org.jspecify.annotations.Nullable;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;

/**
 * A posição na listagem dos pedidos de um cliente (13.04): o (criadoEm, id) de um pedido já visto e a direção.
 * O cliente recebe isto codificado e opaco; o clienteId vai junto para que um cursor não sirva em outra consulta.
 *
 * @param paraTras true para a página anterior: os pedidos que vêm antes deste, na ordem da listagem
 */
record CursorDePedidos(UUID clienteId, Instant criadoEm, UUID id, boolean paraTras) {

    static KeysetScrollPosition posicao(@Nullable String cursor, UUID clienteId) {
        if (cursor == null) {
            return ScrollPosition.keyset();
        }
        var lido = CodificadorDeCursor.decodificar(cursor, CursorDePedidos.class);
        if (!lido.clienteId().equals(clienteId)) {
            throw new CursorInvalidoException();
        }
        // As chaves que PedidoService.listarDoCliente lê: as propriedades da ordenação.
        var chaves = Map.of("criadoEm", lido.criadoEm(), "id", lido.id());
        return lido.paraTras() ? ScrollPosition.backward(chaves) : ScrollPosition.forward(chaves);
    }

    static String de(UUID clienteId, Map<String, ?> chaves, boolean paraTras) {
        return CodificadorDeCursor.codificar(new CursorDePedidos(clienteId, (Instant) chaves.get("criadoEm"),
                (UUID) chaves.get("id"), paraTras));
    }
}
