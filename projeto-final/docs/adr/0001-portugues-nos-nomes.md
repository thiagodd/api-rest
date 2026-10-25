# ADR-0001: Recursos, campos e valores em português

- **Status:** aceita
- **Data:** 2026-10-25 (decidida em 12.02, registrada em 16.02)
- **Regras afetadas:** BZR-NOM-03, BZR-NOM-04

## Contexto

O Zalando (regra 103) e o Google (AIP-140) exigem inglês americano nos nomes. O Bazar é um marketplace brasileiro: os
lojistas, os parceiros que integram com a API e a equipe falam português, e o vocabulário do negócio (nota fiscal, CPF,
CEP, boleto, Pix) não tem tradução exata.

## Decisão

Recursos, campos e valores de enum ficam em português, sem acentos (`/v1/pedidos`, `precoEmCentavos`, `CANCELADO`).
Os nomes definidos por padrões externos continuam como estão (`type`, `title` e `detail` do Problem Details,
`Idempotency-Key`, `next` no `Link`), e os parâmetros transversais usam os nomes comuns do mercado (`page`, `limit`,
`cursor`, `sort`, `fields`, `embed`, `q`).

## Consequências

- O contrato usa o vocabulário de quem o lê, e os termos do domínio não passam por tradução.
- O contrato mistura duas línguas nos nomes padronizados e nos parâmetros transversais.
- Ferramentas e guias externos (linters, geradores de SDK) assumem inglês; as regras de *lint* do Bazar (17.04)
  precisam conhecer o vocabulário em português.
- Uma API pública para o exterior exigiria rever esta decisão, com uma nova versão maior.
