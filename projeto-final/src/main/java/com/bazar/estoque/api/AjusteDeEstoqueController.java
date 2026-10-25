package com.bazar.estoque.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import com.bazar.compartilhado.erros.CampoInvalidoException;
import com.bazar.compartilhado.erros.ConflitoDeVersaoException;
import com.bazar.compartilhado.erros.Problemas;
import com.bazar.compartilhado.erros.RecursoNaoEncontradoException;
import com.bazar.estoque.dominio.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ajuste de estoque em lote, para integrações como o ERP de um lojista (14.04). O lote é parcial: cada item é
 * aplicado na sua própria transação, e um item que falha não desfaz os outros. A resposta é sempre 207, com o
 * resultado de cada item; um lote que nem pode começar (vazio, grande demais, com produto repetido) é um 422.
 */
@RestController
@RequestMapping("/v1/ajustes-estoque/lotes")
@Tag(name = "estoque", description = "O estoque dos produtos")
public class AjusteDeEstoqueController {

    private final EstoqueService servico;
    private final MessageSource mensagens;

    public AjusteDeEstoqueController(EstoqueService servico, MessageSource mensagens) {
        this.servico = servico;
        this.mensagens = mensagens;
    }

    @PostMapping
    @Operation(operationId = "ajustarEstoqueEmLote", summary = "Define o estoque de vários produtos")
    public ResponseEntity<ResultadoDoLote> ajustar(@Valid @RequestBody AjusteDeEstoqueEmLoteRequest lote) {
        exigirProdutosDistintos(lote);
        var resultados = new ArrayList<ResultadoDoLote.Item>();
        for (var i = 0; i < lote.itens().size(); i++) {
            resultados.add(ajustar(i, lote.itens().get(i)));
        }
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(ResultadoDoLote.de(resultados));
    }

    // O controller não é transacional: cada chamada ao serviço é uma transação, confirmada ou desfeita sozinha.
    private ResultadoDoLote.Item ajustar(int indice, AjusteDeEstoqueEmLoteRequest.Item item) {
        try {
            var estoque = servico.ajustarDisponivel(item.produtoId(), item.quantidadeDisponivel());
            return new ResultadoDoLote.Item(indice, item.produtoId(), HttpStatus.OK.value(),
                    EstoqueResponse.de(estoque), null);
        } catch (RecursoNaoEncontradoException ex) {
            return falha(indice, item,
                    Problemas.de(ex, mensagens, LocaleContextHolder.getLocale()));
        } catch (OptimisticLockingFailureException ex) {
            return falha(indice, item, Problemas.de(new ConflitoDeVersaoException(), mensagens, LocaleContextHolder.getLocale()));
        }
    }

    private static ResultadoDoLote.Item falha(int indice, AjusteDeEstoqueEmLoteRequest.Item item,
            ProblemDetail problema) {
        return new ResultadoDoLote.Item(indice, item.produtoId(), problema.getStatus(), null, problema);
    }

    // Dois ajustes do mesmo produto no mesmo lote: qual vale? Em vez de escolher em silêncio, o lote é recusado.
    private static void exigirProdutosDistintos(AjusteDeEstoqueEmLoteRequest lote) {
        var vistos = new HashSet<UUID>();
        for (var i = 0; i < lote.itens().size(); i++) {
            if (!vistos.add(lote.itens().get(i).produtoId())) {
                throw new CampoInvalidoException("itens[" + i + "].produtoId", "bazar.lote.produto-repetido");
            }
        }
    }
}
