package com.bazar.exemplos;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * O lado de quem consome a API do Bazar (15.04): um cliente escrito quando o pedido tinha sete status recebe uma
 * resposta de uma versão mais nova, com um status (EM_SEPARACAO) e um campo (previsaoDeEntrega) que ele não conhece.
 */
class LeitorToleranteTest {

    private static final String PEDIDO_DA_VERSAO_NOVA = """
            { "id": "01a1746b-9ed6-72ba-a153-07ea1e406ddc", "numero": "BZR-2026-000001",
              "status": "EM_SEPARACAO", "previsaoDeEntrega": "2026-10-30" }
            """;

    // O enum do cliente, copiado do contrato quando ele foi escrito.
    enum StatusDoPedido {
        CRIADO, PAGAMENTO_PENDENTE, PAGO, ENVIADO, ENTREGUE, CANCELADO, REEMBOLSADO,
        @JsonEnumDefaultValue
        DESCONHECIDO
    }

    record Pedido(UUID id, String numero, StatusDoPedido status) {
    }

    // No Jackson 3, um campo desconhecido já é ignorado por padrão (no Jackson 2, era erro). Um valor desconhecido
    // de enum continua sendo erro.
    @Test
    void oPadraoDoJackson3IgnoraOCampoNovoMasQuebraComOStatusNovo() {
        var padrao = JsonMapper.builder().build();

        assertThat(padrao.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
        assertThatThrownBy(() -> padrao.readValue(PEDIDO_DA_VERSAO_NOVA, Pedido.class))
                .isInstanceOf(InvalidFormatException.class)
                .hasMessageContaining("EM_SEPARACAO");
    }

    @Test
    void oLeitorToleranteIgnoraOCampoNovoETrataOStatusNovo() {
        var tolerante = JsonMapper.builder()
                // Explícito: o cliente não depende do padrão da versão do Jackson.
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .build();

        var pedido = tolerante.readValue(PEDIDO_DA_VERSAO_NOVA, Pedido.class);

        assertThat(pedido.numero()).isEqualTo("BZR-2026-000001");
        assertThat(pedido.status()).isEqualTo(StatusDoPedido.DESCONHECIDO);
    }

    // Quando o valor precisa ser mostrado ou devolvido à API, o cliente guarda o texto e interpreta só o que conhece.
    record PedidoComStatusOriginal(UUID id, String status) {

        StatusDoPedido statusConhecido() {
            try {
                return StatusDoPedido.valueOf(status);
            } catch (IllegalArgumentException desconhecido) {
                return StatusDoPedido.DESCONHECIDO;
            }
        }
    }

    @Test
    void guardarOTextoPreservaOValorOriginal() {
        var tolerante = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

        var pedido = tolerante.readValue(PEDIDO_DA_VERSAO_NOVA, PedidoComStatusOriginal.class);

        assertThat(pedido.status()).isEqualTo("EM_SEPARACAO");
        assertThat(pedido.statusConhecido()).isEqualTo(StatusDoPedido.DESCONHECIDO);
    }
}
