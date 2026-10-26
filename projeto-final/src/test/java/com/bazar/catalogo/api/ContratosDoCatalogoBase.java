package com.bazar.catalogo.api;

import java.util.UUID;

import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.DinheiroMapperImpl;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.compartilhado.web.AplicadorDePatch;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.bazar.catalogo.dominio.ProdutoDeTeste.umProduto;
import static org.mockito.Mockito.when;

/**
 * A base dos testes que o Spring Cloud Contract gera a partir de src/test/resources/contracts (18.05). Os testes
 * gerados herdam daqui: o slice web real (controller, Jackson, tratador de erros, versionamento) com o serviço
 * substituído pelos dados que cada contrato espera.
 */
@WebMvcTest(ProdutoController.class)
@Import({ ProdutoMapperImpl.class, DinheiroMapperImpl.class, AplicadorDePatch.class })
public abstract class ContratosDoCatalogoBase {

    static final UUID EXISTENTE = UUID.fromString("01a17566-c2c2-750b-a6fd-e44a3e704746");
    static final UUID INEXISTENTE = UUID.fromString("01a17566-0000-7000-8000-000000000000");

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProdutoService produtos;

    @BeforeEach
    void configurar() {
        RestAssuredMockMvc.mockMvc(mvc);
        when(produtos.detalhar(EXISTENTE)).thenReturn(umProduto().comPreco(12990).comVersao(0).build());
        when(produtos.detalhar(INEXISTENTE))
                .thenThrow(new RecursoNaoEncontradoException(TipoProblema.PRODUTO_NAO_ENCONTRADO, INEXISTENTE));
    }
}
