package com.bazar.clientes.api;

import java.util.UUID;

import com.bazar.clientes.dominio.Cliente;
import com.bazar.clientes.dominio.ClienteRepository;
import com.bazar.compartilhado.Canal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@TesteWebDeClientes
class EnderecoControllerTest {

    static final String PAULISTA = """
            { "cep": "01310100", "logradouro": "Avenida Paulista", "numero": "1578", "cidade": "São Paulo", "uf": "SP" }
            """;
    static final String COPACABANA = """
            { "cep": "22070011", "logradouro": "Avenida Atlântica", "numero": "1702", "complemento": "apto 801",
              "cidade": "Rio de Janeiro", "uf": "RJ", "principal": true }
            """;

    @Autowired
    MockMvcTester mvc;

    @Autowired
    ClienteRepository clientes;

    UUID ana;

    @BeforeEach
    void cadastrarACliente() {
        ana = novoCliente("Ana Souza");
    }

    @Test
    void oPrimeiroEnderecoEhOPrincipal() {
        var resposta = mvc.post().uri("/v1/clientes/{clienteId}/enderecos", ana)
                .contentType(MediaType.APPLICATION_JSON).content(PAULISTA).exchange();

        assertThat(resposta).hasStatus(HttpStatus.CREATED)
                .bodyJson().isLenientlyEqualTo("""
                        { "cep": "01310100", "numero": "1578", "complemento": null, "principal": true }
                        """);
        assertThat(resposta.getResponse().getHeader("Location"))
                .matches("http://localhost/v1/clientes/" + ana + "/enderecos/[0-9a-f-]{36}");
    }

    @Test
    void umNovoPrincipalDesmarcaOAnterior() {
        var paulista = adicionar(ana, PAULISTA);
        var copacabana = adicionar(ana, COPACABANA);

        assertThat(mvc.get().uri("/v1/clientes/{clienteId}/enderecos", ana))
                .hasStatusOk()
                .bodyJson().isEqualTo("""
                        { "itens": [ { "id": "%s", "principal": false }, { "id": "%s", "principal": true } ] }
                        """.formatted(paulista, copacabana), JsonCompareMode.LENIENT);
    }

    @Test
    void removerOPrincipalPromoveOMaisAntigo() {
        var paulista = adicionar(ana, PAULISTA);
        var copacabana = adicionar(ana, COPACABANA);

        assertThat(mvc.delete().uri("/v1/clientes/{clienteId}/enderecos/{id}", ana, copacabana))
                .hasStatus(HttpStatus.NO_CONTENT);

        assertThat(mvc.get().uri("/v1/clientes/{clienteId}/enderecos/{id}", ana, paulista))
                .bodyJson().extractingPath("$.principal").isEqualTo(true);
    }

    @Test
    void naoDesmarcaOPrincipalDiretamente() {
        var paulista = adicionar(ana, PAULISTA);

        assertThat(mvc.put().uri("/v1/clientes/{clienteId}/enderecos/{id}", ana, paulista)
                .contentType(MediaType.APPLICATION_JSON).content(PAULISTA))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.codigo").isEqualTo("ENDERECO_PRINCIPAL_OBRIGATORIO");
    }

    @Test
    void recusaODecimoPrimeiroEndereco() {
        for (int i = 0; i < 10; i++) {
            adicionar(ana, PAULISTA);
        }

        assertThat(mvc.post().uri("/v1/clientes/{clienteId}/enderecos", ana)
                .contentType(MediaType.APPLICATION_JSON).content(PAULISTA))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson().isLenientlyEqualTo("""
                        { "codigo": "LIMITE_DE_ENDERECOS", "limite": 10 }
                        """);
    }

    @Test
    void validaCepEUf() {
        assertThat(mvc.post().uri("/v1/clientes/{clienteId}/enderecos", ana).header("Accept-Language", "pt-BR")
                .contentType(MediaType.APPLICATION_JSON)
                .content(PAULISTA.replace("01310100", "01310-100").replace("\"SP\"", "\"sp\"")))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .bodyJson().isLenientlyEqualTo("""
                        { "erros": [
                            { "campo": "cep", "mensagem": "deve ter 8 dígitos, sem hífen (ex.: 01310100)" },
                            { "campo": "uf", "mensagem": "deve ser a sigla da UF em maiúsculas (ex.: SP)" } ] }
                        """);
    }

    // A verificação de pertencimento (prévia de 26.03): o id do endereço existe, mas é de outra cliente.
    @Nested
    class EnderecoDeOutroCliente {

        UUID bia;
        UUID enderecoDaAna;

        @BeforeEach
        void cadastrarOutraCliente() {
            bia = novoCliente("Bia Lima");
            enderecoDaAna = adicionar(ana, PAULISTA);
        }

        @Test
        void naoAparecePelaUriDeOutroCliente() {
            assertThat(mvc.get().uri("/v1/clientes/{clienteId}/enderecos/{id}", bia, enderecoDaAna)
                    .header("Accept-Language", "pt-BR"))
                    .hasStatus(HttpStatus.NOT_FOUND)
                    .bodyJson().isLenientlyEqualTo("""
                            { "type": "https://api.bazar.example/problemas/endereco-nao-encontrado",
                              "detail": "Não existe endereço com id %s para este cliente." }
                            """.formatted(enderecoDaAna));
        }

        @Test
        void naoPodeSerAlteradoNemRemovido() {
            assertThat(mvc.put().uri("/v1/clientes/{clienteId}/enderecos/{id}", bia, enderecoDaAna)
                    .contentType(MediaType.APPLICATION_JSON).content(COPACABANA))
                    .hasStatus(HttpStatus.NOT_FOUND);
            assertThat(mvc.delete().uri("/v1/clientes/{clienteId}/enderecos/{id}", bia, enderecoDaAna))
                    .hasStatus(HttpStatus.NOT_FOUND);

            assertThat(mvc.get().uri("/v1/clientes/{clienteId}/enderecos/{id}", ana, enderecoDaAna))
                    .hasStatusOk()
                    .bodyJson().extractingPath("$.logradouro").isEqualTo("Avenida Paulista");
        }
    }

    @Test
    void clienteInexistenteEh404MesmoNaListagem() {
        assertThat(mvc.get().uri("/v1/clientes/{clienteId}/enderecos", UUID.randomUUID()))
                .hasStatus(HttpStatus.NOT_FOUND)
                .bodyJson().extractingPath("$.codigo").isEqualTo("CLIENTE_NAO_ENCONTRADO");
    }

    private UUID novoCliente(String nome) {
        return clientes.save(Cliente.novo(nome, "cliente@example.com", "52998224725", null, Canal.WEB)).id();
    }

    private UUID adicionar(UUID clienteId, String corpo) {
        var resposta = mvc.post().uri("/v1/clientes/{clienteId}/enderecos", clienteId)
                .contentType(MediaType.APPLICATION_JSON).content(corpo).exchange();
        assertThat(resposta).hasStatus(HttpStatus.CREATED);
        var location = resposta.getResponse().getHeader("Location");
        return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
    }
}
