# Projeto final: API do **Bazar**

> O Bazar é uma loja online fictícia de pequeno porte. Ele é o **domínio único** de toda a documentação: sempre que possível, os exemplos de código de qualquer página usam as entidades e os endpoints daqui. A Parte 16 monta o projeto completo do zero, mas cada parte do Bazar já aparece antes, nas páginas que ensinam o conceito correspondente.

## Por que este domínio

| Necessidade didática | Onde o Bazar oferece |
|---|---|
| CRUD simples para começar | Categorias |
| Coleções grandes, filtros, busca e paginação | Catálogo de produtos |
| Atualização parcial e concorrência | Produto (PATCH), estoque (If-Match) |
| Ações que não são CRUD e máquina de estados | Ciclo de vida do pedido (cancelar, pagar, enviar) |
| Idempotência com dinheiro envolvido | Criação de pedido e de pagamento |
| Consumo de APIs externas com falhas reais | Gateway de pagamento **PagaFácil** e cotação de frete **FreteJá** (fictícios, simulados com WireMock) |
| Operações demoradas | Reembolso e relatório de vendas (202 + polling) |
| Notificação de terceiros | Webhooks para parceiros (ex.: ERP de um lojista) |
| Tempo real | Acompanhamento do pedido via SSE |
| Arquivos | Imagens de produto (upload) e exportação CSV (download/streaming) |
| Dados sensíveis | Cliente (CPF, e-mail, endereço) e LGPD |
| Evolução de contrato | Produto v2 (preço multimoeda) e depreciação da v1 |
| Eventos e consistência eventual | `PedidoCriado`, `PagamentoAprovado` via Outbox |

## Contexto delimitado (módulos)

O Bazar é um **monólito modular** (página 30.01). Os módulos são:

```
bazar
├── catalogo      (categorias, produtos, imagens)
├── estoque       (saldo, reservas, ajustes em lote)
├── clientes      (clientes, endereços)
├── pedidos       (pedidos, itens, máquina de estados)
├── pagamentos    (pagamentos, reembolsos, integração PagaFácil)
├── frete         (cotações, integração FreteJá)
├── integracoes   (assinaturas de webhook, entregas, outbox)
├── relatorios    (jobs assíncronos, exportação)
└── compartilhado (Dinheiro, erros/ProblemDetail, idempotência, paginação)
```

## Entidades

| Entidade | Principais campos | Observações didáticas |
|---|---|---|
| **Categoria** | `id`, `nome`, `slug`, `categoriaPaiId?` | Primeiro CRUD (05.04). Hierarquia simples para discutir sub-recursos (12.03). |
| **Produto** | `id` (UUID v7), `sku`, `nome`, `descricao`, `preco` (Dinheiro), `categoriaId`, `status` (`RASCUNHO`, `ATIVO`, `DESCONTINUADO`), `atributos` (mapa), `versao`, `criadoEm`, `atualizadoEm` | Recurso central: paginação, filtros, PATCH, ETag e versionamento v2. |
| **ImagemProduto** | `id`, `produtoId`, `nomeArquivo`, `tipoConteudo`, `tamanhoBytes`, `urlArmazenamento`, `ordem` | Upload multipart (22.01) |
| **Estoque** | `produtoId`, `quantidadeDisponivel`, `quantidadeReservada`, `versao` | Concorrência otimista (10.06, 20.03) e lote (14.04) |
| **Cliente** | `id`, `nome`, `email`, `cpf`, `telefone`, `criadoEm` | Dados sensíveis, mascaramento e BOLA (26.03) |
| **Endereco** | `id`, `clienteId`, `cep`, `logradouro`, `numero`, `complemento`, `cidade`, `uf`, `principal` | Sub-recurso de cliente (12.03) |
| **Pedido** | `id`, `numero` (legível, ex. `BZR-2026-000123`), `clienteId`, `itens`, `enderecoEntrega` (cópia), `valorItens`, `valorFrete`, `valorTotal`, `status`, `versao`, `criadoEm` | Máquina de estados (12.04), idempotência (21.01) |
| **ItemPedido** | `produtoId`, `sku`, `nomeProduto` (cópia), `quantidade`, `precoUnitario`, `subtotal` | Mostra por que se copia o preço no momento da compra |
| **Pagamento** | `id`, `pedidoId`, `valor`, `metodo` (`PIX`, `CARTAO`, `BOLETO`), `status`, `idExterno` (PagaFácil), `criadoEm` | Cliente HTTP externo e resiliência (Parte 10) |
| **Reembolso** | `id`, `pagamentoId`, `valor`, `motivo`, `status` | Operação assíncrona (21.02) |
| **Operacao** | `id`, `tipo`, `status` (`PENDENTE`, `EM_EXECUCAO`, `CONCLUIDA`, `FALHOU`), `progresso`, `resultadoUrl?`, `erro?` (ProblemDetail) | Recurso de status de operações longas (21.02) |
| **CotacaoFrete** | `cepDestino`, `itens`, `opcoes[]` (transportadora, prazo, valor) | Cliente HTTP interface (24.03) |
| **AssinaturaWebhook** | `id`, `parceiro`, `url`, `eventos[]`, `segredo` (só retornado na criação), `ativa` | Webhooks de saída (21.03) |
| **EntregaWebhook** | `id`, `assinaturaId`, `eventoId`, `tentativas`, `ultimoStatusHttp`, `proximaTentativaEm`, `status` | Retentativas com backoff (21.03) |
| **EventoOutbox** | `id`, `tipoAgregado`, `agregadoId`, `tipoEvento`, `payload`, `publicadoEm?` | Padrão Outbox (30.04) |
| **ChaveIdempotencia** | `chave`, `escopo` (cliente + rota), `hashRequisicao`, `statusResposta`, `corpoResposta`, `expiraEm` | Idempotência (21.01) |
| **RelatorioVendas** | `id`, `periodo`, `formato`, `operacaoId`, `arquivo?` | 202 + polling e download por streaming (21.02, 22.02) |

### Máquina de estados do pedido

```
CRIADO ──pagar──▶ PAGAMENTO_PENDENTE ──(webhook aprovado)──▶ PAGO ──enviar──▶ ENVIADO ──▶ ENTREGUE
   │                     │                                    │
   └──cancelar──▶ CANCELADO ◀──cancelar──┘                    └──reembolsar──▶ REEMBOLSADO
```

### Tipos compartilhados

- **Dinheiro**: `{ "valor": "129.90", "moeda": "BRL" }`, com o valor em string decimal e a moeda em ISO 4217 (15.02). Na **v1** o produto expõe `precoEmCentavos` (inteiro, BRL implícito). A **v2** adota `Dinheiro` para permitir várias moedas (18.03).
- **Datas**: sempre `Instant` em UTC no formato RFC 3339 (`2026-10-24T17:30:00Z`). Datas sem horário usam `LocalDate` (15.01).
- **IDs**: UUID v7 nos IDs públicos e identificador legível para humanos (`numero` do pedido) (15.03).
- **Nulos e coleções**: as respostas incluem os campos sem valor como `null` explícito (nunca omitem nem usam `""`), e coleções são sempre um objeto com a lista em `itens` (nunca um array no topo) (02.06).
- **Erros**: `application/problem+json` (RFC 9457) com `type` em `https://api.bazar.example/problemas/<codigo>` e as extensões `codigo`, `erros[]` e `traceId` (08.01, 16.01).

## Endpoints (v1)

Base: `https://api.bazar.example/v1`. As colunas indicam a **página onde o endpoint é construído pela primeira vez** e a **página do projeto final onde ele é consolidado**.

### Catálogo

| Método e rota | Descrição | Conceitos | Introduzido em | Consolidado em |
|---|---|---|---|---|
| `GET /categorias` | Lista as categorias | GET, 200 | 05.03 | 31.03 |
| `POST /categorias` | Cria uma categoria | 201 + `Location`, validação | 05.04, 07.01 | 31.03 |
| `GET /categorias/{id}` | Detalha uma categoria | 404 com ProblemDetail | 05.04, 08.02 | 31.03 |
| `PUT /categorias/{id}` | Substitui uma categoria | PUT idempotente | 05.04 | 31.03 |
| `DELETE /categorias/{id}` | Remove uma categoria | 204, 409 quando há produtos | 05.04, 14.03 | 31.03 |
| `GET /produtos` | Lista com filtros (`categoria`, `status`, `precoMin`, `precoMax`, `q`), ordenação (`sort`) e paginação (`limit`, `cursor`) | Filtros, keyset, `Link` | 13.01–13.04 | 31.03 |
| `GET /produtos?fields=id,nome,preco` | Resposta parcial | *Sparse fieldsets* | 13.05 | 31.03 |
| `QUERY /produtos` / `POST /produtos/buscas` | Busca complexa no corpo | Método QUERY (RFC 10008) e alternativa com POST | 13.01 | 31.03 |
| `POST /produtos` | Cria um produto | DTO, MapStruct, validação | 06.01, 06.02, 07.02 | 31.03 |
| `GET /produtos/{id}` | Detalha, com `ETag` e `Cache-Control` | Cache e 304 | 20.01, 20.02 | 31.03 |
| `PUT /produtos/{id}` | Substitui, com `If-Match` | 412/428 | 14.02, 20.03 | 31.03 |
| `PATCH /produtos/{id}` | Atualização parcial (`application/merge-patch+json`) | Merge Patch e JSON Patch | 14.02 | 31.03 |
| `DELETE /produtos/{id}` | Descontinua (soft delete) | Soft delete e 204 | 14.03 | 31.03 |
| `POST /produtos/{id}/imagens` | Upload de imagem (multipart) | Limites e validação de tipo | 22.01 | 31.03 |
| `GET /produtos/{id}/imagens/{imagemId}` | Download da imagem | `Resource`, cache | 22.02 | 31.03 |

### Estoque

| Método e rota | Descrição | Conceitos | Introduzido em | Consolidado em |
|---|---|---|---|---|
| `GET /produtos/{id}/estoque` | Saldo atual | Sub-recurso singleton | 12.03 | 31.05 |
| `PUT /produtos/{id}/estoque` | Define o saldo, com `If-Match` | `@Version` → `ETag` | 10.06, 20.03 | 31.05 |
| `POST /ajustes-estoque/lotes` | Ajuste em lote (até 500 itens) | Bulk, resultado por item | 14.04 | 31.05 |

### Clientes

| Método e rota | Descrição | Conceitos | Introduzido em | Consolidado em |
|---|---|---|---|---|
| `POST /clientes` | Cadastro | Validação customizada (CPF) | 07.02 | 31.04 |
| `GET /clientes/{id}` | Detalha (CPF mascarado) | Exposição mínima de dados | 26.03 | 31.04 |
| `PATCH /clientes/{id}` | Atualização parcial | Merge Patch | 14.02 | 31.04 |
| `GET/POST /clientes/{id}/enderecos` | Endereços | Sub-recursos | 12.03 | 31.04 |
| `PUT/DELETE /clientes/{id}/enderecos/{enderecoId}` | Manutenção de endereço | | 12.03 | 31.04 |

### Pedidos e frete

| Método e rota | Descrição | Conceitos | Introduzido em | Consolidado em |
|---|---|---|---|---|
| `POST /fretes/cotacoes` | Cota o frete com a FreteJá | HTTP interface, timeouts, fallback | 24.03, 25.01, 25.04 | 31.05 |
| `POST /pedidos` | Cria o pedido, com header `Idempotency-Key` | Idempotência, transação, reserva de estoque | 21.01 | 31.05 |
| `GET /pedidos` | Lista (`clienteId`, `status`, `criadoApos`) com cursor | Keyset | 13.04 | 31.05 |
| `GET /pedidos/{id}` | Detalha | Projeções, N+1 | 10.04, 10.05 | 31.05 |
| `POST /pedidos/{id}/cancelamento` | Cancela | Ação não CRUD, 409 em estado inválido | 12.04 | 31.05 |

Hipermídia (decisão de 03.04): a representação do pedido traz `_links` no formato HAL com as ações disponíveis no estado atual (`bazar:pagar`, `bazar:cancelar`, `bazar:reembolsar`, *CURIE* `bazar` → `https://api.bazar.example/rels/{rel}`). Os demais recursos não têm `_links`; a paginação e a depreciação usam o header `Link`.
| `GET /pedidos/{id}/eventos` | Acompanhamento em tempo real (`text/event-stream`) | SSE, `Last-Event-ID` | 19.04 | 31.07 |

### Pagamentos

| Método e rota | Descrição | Conceitos | Introduzido em | Consolidado em |
|---|---|---|---|---|
| `POST /pedidos/{id}/pagamentos` | Inicia o pagamento na PagaFácil (`Idempotency-Key`) | RestClient, retry seguro, circuit breaker | 24.02, 25.02, 25.03 | 31.06 |
| `GET /pedidos/{id}/pagamentos` | Lista os pagamentos do pedido | | 12.03 | 31.06 |
| `POST /integracoes/pagafacil/notificacoes` | **Recebe** o webhook do gateway (assinatura HMAC) | Validação de assinatura, deduplicação | 21.03 | 31.06 |
| `POST /pagamentos/{id}/reembolsos` | Solicita um reembolso → `202 Accepted` | Operação longa | 21.02 | 31.06 |
| `GET /operacoes/{id}` | Status da operação (`Retry-After`) | Polling, 303 ao concluir | 21.02 | 31.06 |

### Integrações (webhooks de saída)

| Método e rota | Descrição | Conceitos | Introduzido em | Consolidado em |
|---|---|---|---|---|
| `POST /assinaturas-webhook` | Parceiro registra a URL e os eventos (o segredo só é retornado aqui) | Segredos, validação de URL (SSRF) | 21.03, 26.05 | 31.07 |
| `GET /assinaturas-webhook`, `GET/DELETE /assinaturas-webhook/{id}` | Gestão das assinaturas | | 21.03 | 31.07 |
| `POST /assinaturas-webhook/{id}/testes` | Dispara um evento de teste | Ação não CRUD | 21.03 | 31.07 |
| `GET /assinaturas-webhook/{id}/entregas` | Histórico de entregas e tentativas | Observabilidade para o parceiro | 21.03 | 31.07 |

Eventos enviados: `pedido.criado`, `pedido.pago`, `pedido.cancelado`, `pedido.enviado` e `reembolso.concluido`, no formato CloudEvents (JSON) e assinados conforme o Standard Webhooks.

### Relatórios

| Método e rota | Descrição | Conceitos | Introduzido em | Consolidado em |
|---|---|---|---|---|
| `POST /relatorios/vendas` | Solicita o relatório do período → 202 + `Location: /operacoes/{id}` | `@Async`, operação longa | 19.03, 21.02 | 31.08 |
| `GET /relatorios/{id}` | Metadados do relatório | | 21.02 | 31.08 |
| `GET /relatorios/{id}/arquivo` | CSV por streaming (`Content-Disposition`), com suporte a `Range` | `StreamingResponseBody`, 206 | 22.02 | 31.08 |

### Versão 2

| Método e rota | Mudança | Introduzido em | Consolidado em |
|---|---|---|---|
| `GET/POST/PUT/PATCH /v2/produtos...` | `precoEmCentavos` (inteiro, BRL) → `preco` (`Dinheiro`, multimoeda) | 18.03 | 31.09 |
| `/v1/produtos...` | Passa a responder com `Deprecation`, `Sunset` e `Link` (`rel="deprecation"`) | 18.04 | 31.09 |

### Operação (não faz parte do contrato público)

| Rota | Uso | Página |
|---|---|---|
| `/actuator/health/liveness`, `/actuator/health/readiness` | Probes do Kubernetes | 27.03, 29.04 |
| `/actuator/prometheus` | Métricas | 27.05 |
| `/actuator/info`, `/actuator/sbom` | Informações e SBOM | 27.02 |
| `/v3/api-docs`, `/swagger-ui.html` | Contrato e UI (só fora de produção ou protegidos) | 17.02 |

## Dependências externas simuladas

| Sistema | Papel | Como é simulado |
|---|---|---|
| **PagaFácil** | Gateway de pagamento: cria cobrança e notifica o resultado por webhook. Às vezes demora ou retorna 503. | WireMock (testes e Docker Compose) com cenários de latência, 503 e 429 |
| **FreteJá** | Cotação de frete. Instável de propósito. | WireMock |
| **PostgreSQL** | Banco principal | Testcontainers e Docker Compose |
| **Redis** | Cache e armazenamento de chaves de idempotência e de rate limiting | Testcontainers e Docker Compose |
| **Kafka** ou **RabbitMQ** (opcional) | Publicação dos eventos do Outbox | Testcontainers (página 30.03, complementar) |
| **Armazenamento de arquivos** | Imagens e relatórios | Sistema de arquivos local, com nota sobre S3 e URLs pré-assinadas |
| **Provedor de identidade** | Emite os JWT | Keycloak em contêiner, ou um emissor de testes (detalhes na doc de segurança) |
| **Stack de observabilidade** | Traces, métricas e logs | Grafana LGTM (`grafana/otel-lgtm:0.38.0`) via Docker Compose |

## Roteiro do projeto final (Parte 16)

| Página | Entrega | O que é aplicado |
|---|---|---|
| 31.01 | Visão, requisitos não funcionais (SLOs), decisões arquiteturais (ADRs) | 16.02, 27.01, 30.01 |
| 31.02 | Contrato OpenAPI 3.1 completo do Bazar, lint com Spectral e mocks | 17.01–17.04 |
| 31.03 | Módulo de catálogo | Partes 3, 4, 6 e 9 |
| 31.04 | Módulo de clientes | 07.02, 26.03, 26.04 |
| 31.05 | Estoque, frete e pedidos | 10.03, 10.06, 12.04, 21.01, 24.03 |
| 31.06 | Pagamentos e reembolsos | Partes 9 e 10 |
| 31.07 | Eventos, Outbox, webhooks e SSE | 19.04, 21.03, 30.04 |
| 31.08 | Relatórios assíncronos e exportação | 19.03, 21.02, 22.02 |
| 31.09 | v2 do produto e depreciação da v1 | Parte 7 |
| 31.10 | Observabilidade, testes de carga, contêiner e deploy | Partes 12, 13 e 14 |
| 31.11 | Revisão final com o checklist de API sênior | Apêndice A |

## Regras para os exemplos ao longo da doc

1. Use **sempre** os nomes deste arquivo: `Produto`, `Pedido`, `precoEmCentavos`, `PagaFácil` etc. Não invente variações.
2. Escreva código e nomes de domínio em **português**: classes, campos e rotas. Termos técnicos consagrados ficam em inglês: `Controller`, `Repository`, `DTO`, `Request`/`Response` como sufixo de DTO.
   - Exemplo: `ProdutoController`, `CriarProdutoRequest`, `ProdutoResponse`, `ProdutoRepository`.
3. Use o pacote base `com.bazar` e organize **por módulo** (`com.bazar.catalogo`, `com.bazar.pedidos`...).
4. Quando um exemplo precisar simplificar algo que o projeto final faz de outro jeito, inclua uma nota "No projeto final isso evolui para... (página X)".
