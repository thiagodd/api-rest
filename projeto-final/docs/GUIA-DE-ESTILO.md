# Guia de estilo da API do Bazar

Versão 1.0 (16.02). Este guia reúne as decisões de design da API do Bazar. Toda API nova, e toda mudança numa API
existente, segue estas regras. Uma exceção é possível, mas é decidida numa revisão de API e registrada num ADR
(`docs/adr/`).

## Como ler este guia

Os níveis seguem o RFC 2119, com o esclarecimento do RFC 8174: só valem quando escritos em maiúsculas.

| Nível | RFC 2119 | Significado no Bazar | Exceção |
|---|---|---|---|
| **DEVE** | MUST | Obrigatório | Só com ADR aprovado na revisão |
| **NÃO DEVE** | MUST NOT | Proibido | Só com ADR aprovado na revisão |
| **DEVERIA** | SHOULD | O padrão; não seguir exige um motivo | Motivo escrito no PR |
| **NÃO DEVERIA** | SHOULD NOT | Evitar; seguir exige um motivo | Motivo escrito no PR |
| **PODE** | MAY | Opcional | Nenhuma |

Cada regra tem um identificador estável (`BZR-NOM-01`). O identificador nunca é reaproveitado: uma regra removida
fica marcada como **retirada**. As regras de *lint* (17.04) e os comentários de revisão citam o identificador.

A coluna **Verificação** diz como a regra é conferida: **lint** (Spectral, no contrato OpenAPI), **teste** (na suíte
do projeto) ou **revisão** (por uma pessoa, na revisão de API).

---

## 1. Nomes

| Id | Regra | Verificação | Origem |
|---|---|---|---|
| BZR-NOM-01 | Caminhos **DEVEM** usar substantivos no plural, em *kebab-case*, sem verbos, sem extensão e sem barra final (`/v1/ajustes-estoque`). | lint | 12.02 |
| BZR-NOM-02 | Campos JSON e parâmetros de *query* **DEVEM** usar *camelCase*, com siglas escritas como palavras (`cpf`, `traceId`, `urlImagem`); referências são o recurso + `Id` (`clienteId`). | lint | 12.02 |
| BZR-NOM-03 | Recursos, campos e valores **DEVEM** estar em português, sem acentos (ADR-0001). | revisão | 12.02 |
| BZR-NOM-04 | Os parâmetros transversais **DEVEM** usar os nomes comuns do mercado, em inglês: `q`, `page`, `limit`, `cursor`, `sort`, `fields`, `embed`. | lint | 13.01–13.05 |
| BZR-NOM-05 | Instantes **DEVEM** ter o nome no particípio seguido de `Em` (`criadoEm`, `canceladoEm`); filtros de período, seguido de `Apos` ou `Antes` (`criadoApos`). Datas sem hora **DEVEM** usar `format: date`. | lint | 12.02, 15.01 |
| BZR-NOM-06 | Booleanos **NÃO DEVEM** ter prefixo (`principal`, e não `isPrincipal`). | lint | 12.02 |
| BZR-NOM-07 | Números soltos com unidade **DEVEM** levar a unidade no nome (`precoEmCentavos`, `prazoEmDias`). | revisão | 12.02 |
| BZR-NOM-08 | Valores de enum **DEVEM** estar em `UPPER_SNAKE_CASE`. | lint | 15.04 |

## 2. Recursos e URIs

| Id | Regra | Verificação | Origem |
|---|---|---|---|
| BZR-REC-01 | O modelo de recursos **DEVE** partir dos casos de uso, e não das tabelas. Tabelas de junção **NÃO DEVEM** virar recursos. | revisão | 12.01 |
| BZR-REC-02 | Sub-recursos **DEVEM** ser usados só para composição (o filho não existe sem o pai), com no máximo um nível (`/clientes/{id}/enderecos`). | lint | 12.03 |
| BZR-REC-03 | Todo acesso a um sub-recurso **DEVE** verificar o pertencimento ao pai, e responder `404` quando ele não pertence. | teste | 12.03 |
| BZR-REC-04 | Ações com regras de negócio **DEVEM** ser `POST` num sub-recurso com nome de substantivo (`/pedidos/{id}/cancelamento`), e não `PATCH` de um campo de status. | revisão | 12.04 |
| BZR-REC-05 | A versão maior **DEVE** estar no início do caminho (`/v1`), conforme o ADR-0003. | lint | 18.02 |

## 3. Identificadores

| Id | Regra | Verificação | Origem |
|---|---|---|---|
| BZR-ID-01 | Ids públicos **DEVEM** ser UUID v7, gerados por `Ids.novo()`. Ids sequenciais **NÃO DEVEM** aparecer na API. | teste | 15.03 |
| BZR-ID-02 | Ids **DEVEM** ser strings no JSON (`format: uuid` no OpenAPI), e o contrato **DEVE** tratá-los como opacos. | lint | 15.03 |
| BZR-ID-03 | Um identificador para pessoas (`BZR-2026-000123`) **PODE** existir, mas **NÃO DEVE** substituir o id nas URIs. | revisão | 15.03 |

## 4. Tipos de dados

| Id | Regra | Verificação | Origem |
|---|---|---|---|
| BZR-TIP-01 | Instantes **DEVEM** ser RFC 3339 em UTC, com `Z` (`2026-10-25T13:30:00Z`). `LocalDateTime` **NÃO DEVE** aparecer no contrato. | lint, teste | 15.01 |
| BZR-TIP-02 | Períodos **DEVEM** ser semiabertos, `[início, fim)`, e documentados como tal. | revisão | 15.01 |
| BZR-TIP-03 | "De que dia é" **DEVE** ser calculado no fuso da loja (`America/Sao_Paulo`), nunca no fuso do servidor. | teste | 15.01 |
| BZR-TIP-04 | Valores monetários **DEVEM** ter a moeda ISO 4217 e ser exatos: `{"valor": "129.90", "moeda": "BRL"}` (`Dinheiro`). `double` e `float` **NÃO DEVEM** representar dinheiro. | lint, teste | 15.02 |
| BZR-TIP-05 | Listas de negócio **DEVERIAM** ser enums extensíveis (`examples` + aviso na descrição). `enum` fechado só para listas completas por natureza. | lint | 15.04 |
| BZR-TIP-06 | Um booleano **NÃO DEVERIA** ser usado quando um terceiro estado é imaginável: use um enum. | revisão | 15.04 |
| BZR-TIP-07 | Números, valores e datas **NÃO DEVEM** ser formatados para exibição (`"R$ 1.299,90"`). | revisão | 15.05 |

## 5. Métodos e respostas

| Id | Regra | Verificação | Origem |
|---|---|---|---|
| BZR-MET-01 | `POST` de criação **DEVE** responder `201` com `Location` e a representação completa, e **DEVERIA** atender `Prefer: return=minimal`. | teste | 14.01 |
| BZR-MET-02 | Campos somente leitura enviados na escrita **DEVEM** ser ignorados. | teste | 14.01 |
| BZR-MET-03 | `PATCH` **DEVE** aceitar JSON Merge Patch (`application/merge-patch+json`) e **PODE** aceitar JSON Patch. O resultado do *patch* **DEVE** ser validado, e não o *patch*. | teste | 14.02 |
| BZR-MET-04 | `DELETE` **DEVE** responder `204`. Um recurso referenciado por outros (produto com pedidos) **DEVE** ser desativado, e não apagado; o `GET` dele continua respondendo. | teste | 14.03 |
| BZR-MET-05 | Lotes parciais **DEVEM** responder `207` sempre, com um resultado por item (índice, status e o Problem Details do item), e **DEVEM** ter um limite documentado. | teste | 14.04 |

## 6. Coleções

| Id | Regra | Verificação | Origem |
|---|---|---|---|
| BZR-COL-01 | Filtros **DEVEM** ser um parâmetro por critério, com o nome do campo da resposta, combinados com E; os valores de uma lista são alternativas (OU). | revisão | 13.01 |
| BZR-COL-02 | Parâmetros de *query* desconhecidos **DEVEM** ser recusados com `400 parametro-desconhecido`. | teste | 12.02, 13.01 |
| BZR-COL-03 | A ordenação **DEVE** usar `sort=campo,direcao`, com uma lista fechada de campos, e **DEVE** terminar pelo id como desempate. | teste | 13.02 |
| BZR-COL-04 | Coleções que crescem sem limite **DEVEM** usar paginação por cursor (`cursor`, `limit`). Offset (`page`, `limit`) **PODE** ser usado em coleções pequenas. | lint (aviso), revisão | 13.03, 13.04 |
| BZR-COL-05 | `limit` **DEVE** ter padrão 20 e máximo 100. Os links de navegação **DEVEM** ir no header `Link`. | teste | 13.03 |
| BZR-COL-06 | Respostas parciais (`fields`) e expansão (`embed`) **PODEM** ser oferecidas; sem o parâmetro, a resposta **DEVE** sair completa. | teste | 13.05 |

## 7. Erros

| Id | Regra | Verificação | Origem |
|---|---|---|---|
| BZR-ERR-01 | Todo erro **DEVE** ser Problem Details (`application/problem+json`), com `type`, `title`, `status`, `codigo` e `traceId`. | teste | 08.01, 08.02 |
| BZR-ERR-02 | Todo `type` **DEVE** estar no catálogo (`TipoProblema`), com status e ação esperada do cliente, e a URI **DEVE** abrir a documentação. | teste | 16.01 |
| BZR-ERR-03 | O status **DEVE** seguir o critério: forma ou tipo errados → `400`; valores fora das regras em qualquer estado → `422` com `erros`; conflito com o estado atual → `409` (ADR-0002). | revisão, teste | 16.01 |
| BZR-ERR-04 | Erros **NÃO DEVEM** conter SQL, *stack traces*, nomes de classes ou o valor recebido. | teste | 08.02, 16.01 |
| BZR-ERR-05 | O status ou o significado de um tipo publicado **NÃO DEVE** mudar. Tipos novos **PODEM** ser acrescentados. | revisão | 16.01 |

## 8. Idioma

| Id | Regra | Verificação | Origem |
|---|---|---|---|
| BZR-IDI-01 | Textos para pessoas (`title`, `detail`, mensagens de campo) **DEVEM** seguir o `Accept-Language`, entre `pt-BR` (padrão) e `en`. O contrato (campos, enums, códigos) **NÃO DEVE** ser traduzido. | teste | 15.05 |
| BZR-IDI-02 | Respostas localizadas **DEVEM** ter `Content-Language` e `Vary: Accept-Language`. | teste | 15.05 |

## 9. Evolução

| Id | Regra | Verificação | Origem |
|---|---|---|---|
| BZR-EVO-01 | O servidor **DEVE** ignorar campos desconhecidos no corpo das escritas, e **DEVE** recusar campos desconhecidos no corpo das buscas. | teste | 06.03, 13.01 |
| BZR-EVO-02 | A documentação **DEVE** pedir aos clientes que ignorem campos novos e tratem valores de enum e `codigo` desconhecidos (*tolerant reader*). | revisão | 15.04, 16.01 |
| BZR-EVO-03 | Mudanças incompatíveis **NÃO DEVEM** entrar numa versão publicada. O que é incompatível está na 18.01. | lint | 18.01 |
| BZR-EVO-04 | Uma versão maior nova **DEVE** existir só para os recursos com mudanças incompatíveis, e só a versão maior vai no caminho (ADR-0003). | revisão | 18.02 |
| BZR-EVO-05 | No máximo duas versões maiores de um recurso **PODEM** estar em produção. A anterior **DEVE** ser depreciada no lançamento da nova e desligada com pelo menos seis meses de aviso. | revisão | 18.02, 18.04 |

---

## Processo

1. **Antes do código**, o contrato (OpenAPI) da mudança vai num PR, com o *lint* (17.04) rodando no CI.
2. **Revisão de API**: uma pessoa de outra equipe revisa o contrato com este guia aberto. Comentários citam o
   identificador da regra.
3. **Exceções**: uma regra **DEVE** só é quebrada com um ADR aprovado na revisão. Uma regra **DEVERIA**, com o motivo
   escrito no PR.
4. **Mudanças no guia** são PRs neste arquivo, com um ADR quando mudam uma decisão anterior.

## Decisões registradas

| ADR | Decisão |
|---|---|
| [ADR-0001](adr/0001-portugues-nos-nomes.md) | Recursos, campos e valores em português |
| [ADR-0002](adr/0002-criterio-400-422-409.md) | Critério 400 × 422 × 409, e o 422 apesar do Zalando |
| [ADR-0003](adr/0003-versao-maior-no-caminho.md) | Versão maior no caminho, por recurso, no máximo duas em produção |
