package com.bazar.catalogo.api;

import com.bazar.catalogo.dominio.Produto;
import com.bazar.compartilhado.ConfiguracaoMapStruct;
import org.mapstruct.Mapper;

// Um mapper por versão, sobre o mesmo domínio e o mesmo serviço (18.03).
@Mapper(config = ConfiguracaoMapStruct.class)
interface ProdutoV2Mapper {

    ProdutoV2Response paraResponse(Produto produto);
}
