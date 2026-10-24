package com.bazar.catalogo.api;

import com.bazar.catalogo.dominio.CategoriaRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

// Testes da camada web: a explicação completa está em 11.03.
@WebMvcTest(CategoriaController.class)
@Import(CategoriaRepository.class)
class CategoriaControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void listaAsCategoriasDentroDeItens() {
        assertThat(mvc.get().uri("/v1/categorias"))
                .hasStatusOk()
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .extractingPath("$.itens[*].nome")
                .asArray()
                .containsExactly("Eletrônicos", "Livros", "Programação");
    }

    @Test
    void filtraPeloNomeEPelasRaizes() {
        assertThat(mvc.get().uri("/v1/categorias?q=liv"))
                .bodyJson().extractingPath("$.itens[*].slug").asArray().containsExactly("livros");
        assertThat(mvc.get().uri("/v1/categorias?apenasRaizes=true"))
                .bodyJson().extractingPath("$.itens[*].slug").asArray().containsExactly("eletronicos", "livros");
    }

    @Test
    void detalhaUmaCategoriaComCategoriaPaiNulaExplicita() {
        assertThat(mvc.get().uri("/v1/categorias/0192f3a0-5555-7000-8000-000000000001"))
                .hasStatusOk()
                .bodyJson()
                .isLenientlyEqualTo("""
                        { "id": "0192f3a0-5555-7000-8000-000000000001", "nome": "Livros",
                          "slug": "livros", "categoriaPaiId": null }
                        """);
    }

    @Test
    void responde404ParaUmIdInexistente() {
        assertThat(mvc.get().uri("/v1/categorias/0192f3a0-5555-7000-8000-000000000999"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void responde400ParaUmIdQueNaoEUuid() {
        assertThat(mvc.get().uri("/v1/categorias/abc")).hasStatus(HttpStatus.BAD_REQUEST);
    }
}
