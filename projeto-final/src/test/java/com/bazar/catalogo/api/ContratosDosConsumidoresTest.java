package com.bazar.catalogo.api;

import java.util.List;
import java.util.UUID;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.spring7.PactVerificationSpring7Provider;
import au.com.dius.pact.provider.spring.spring7.Spring7MockMvcTestTarget;
import com.bazar.catalogo.dominio.ProdutoService;
import com.bazar.compartilhado.DinheiroMapperImpl;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.compartilhado.erros.TipoProblema;
import com.bazar.compartilhado.web.AplicadorDePatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.bazar.catalogo.dominio.ProdutoDeTeste.umProduto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Verifica no Bazar os contratos que os consumidores publicaram (18.05). Cada interação de cada pact vira uma
 * execução deste @TestTemplate: o Pact monta a requisição, o MockMvc a entrega ao controller real, e a resposta é
 * comparada com o que o consumidor disse que usa. Os pacts vêm de src/test/resources/pacts; num pipeline real, do
 * broker (@PactBroker).
 */
@Provider("bazar-api")
@PactFolder("pacts")
@WebMvcTest(ProdutoController.class)
@Import({ ProdutoMapperImpl.class, DinheiroMapperImpl.class, AplicadorDePatch.class })
class ContratosDosConsumidoresTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ProdutoService produtos;

    @BeforeEach
    void alvo(PactVerificationContext contexto) {
        contexto.setTarget(new Spring7MockMvcTestTarget(mvc, List.of(), List.of(), List.of(), false, null));
    }

    @TestTemplate
    @ExtendWith(PactVerificationSpring7Provider.class)
    void verificaCadaInteracao(PactVerificationContext contexto) {
        contexto.verifyInteraction();
    }

    // Os estados que os consumidores pedem com given(...). Cada um prepara o dublê do serviço.
    @State("existe o produto LIV-0042")
    void existeOProduto() {
        when(produtos.detalhar(any(UUID.class))).thenReturn(umProduto().comSku("LIV-0042").comPreco(12990).comVersao(0).build());
    }

    @State("não existe o produto")
    void naoExisteOProduto() {
        when(produtos.detalhar(any(UUID.class))).thenAnswer(chamada -> {
            throw new RecursoNaoEncontradoException(TipoProblema.PRODUTO_NAO_ENCONTRADO, chamada.getArgument(0));
        });
    }
}
