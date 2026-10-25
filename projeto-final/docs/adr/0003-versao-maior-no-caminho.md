# ADR-0003: Versão maior no caminho

- **Status:** aceita
- **Data:** 2026-10-25 (`/v1` desde a 05.01; estratégia fixada em 18.02)
- **Regras afetadas:** BZR-REC-05, BZR-EVO-03, BZR-EVO-04, BZR-EVO-05
- **Alternativas consideradas:** header (`API-Version`), parâmetro de *query*, parâmetro de media type
  (`application/json;v=2`), versões por data (Stripe), evolução sem versão (Zalando)

## Contexto

O Bazar evolui por mudanças compatíveis (18.01) e só precisa de uma versão nova para o que não pode ser compatível,
como a saída do `precoEmCentavos`. Os clientes são o aplicativo e o site do Bazar, os ERPs dos lojistas e parceiros
que integram por conta própria, muitos com `curl` e planilhas. A API passa por uma CDN e por um gateway.

O Zalando (regras 113 a 115) recomenda evitar versões e, quando inevitável, usar o media type, e proíbe a versão no
caminho. O Azure usa um parâmetro de *query* obrigatório com datas. A Stripe usa datas, fixadas por conta e trocadas
por header.

## Decisão

- A versão maior vai no início do caminho: `/v1/produtos`, `/v2/produtos`. Só a maior: mudanças compatíveis não mudam
  a URL, e o `info.version` do contrato (`1.4.0`) registra as menores para o changelog.
- A granularidade é o recurso: a `/v2` existe só para os recursos que tiveram uma mudança incompatível. O resto
  continua só em `/v1`.
- No máximo duas versões maiores de um recurso em produção. A anterior é depreciada no lançamento da nova e desligada
  com pelo menos seis meses de aviso (18.04).

## Consequências

- A versão aparece em logs, métricas, links, no `curl` e na barra do navegador, sem headers. A CDN usa a URL como
  chave de cache e o gateway roteia por prefixo, sem `Vary` nem regras por header.
- Um mesmo produto tem duas URLs (`/v1/produtos/{id}` e `/v2/produtos/{id}`). Links guardados pelos clientes apontam
  para uma versão, e os links HAL (12.04) de uma resposta v1 apontam para a v1.
- Divergimos do Zalando: integradores acostumados ao media type versionado precisam ler este ADR.
- Versão por recurso evita copiar a API inteira, mas o cliente precisa saber qual recurso está em qual versão. O guia
  de primeiros passos (17.05) lista as versões vigentes.
