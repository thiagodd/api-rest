package com.bazar.catalogo.api;

import com.bazar.catalogo.dominio.Produto;
import com.bazar.catalogo.dominio.ProdutoResumo;
import com.bazar.compartilhado.ConfiguracaoMapStruct;
import com.bazar.compartilhado.DinheiroMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ConfiguracaoMapStruct.class, uses = DinheiroMapper.class)
interface ProdutoMapper {

    @Mapping(target = "preco", source = "precoEmCentavos")
    Produto paraProduto(CriarProdutoRequest request);

    @Mapping(target = "precoEmCentavos", source = "preco")
    @Mapping(target = "categoria", ignore = true)
    ProdutoResponse paraResponse(Produto produto);

    @Mapping(target = "precoEmCentavos", source = "preco")
    ProdutoResumoResponse paraResumo(ProdutoResumo resumo);
}
