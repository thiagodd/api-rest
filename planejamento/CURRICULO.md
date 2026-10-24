# Currículo: APIs REST do básico ao avançado com Spring Boot

> Documento central das fases 2 (site) e 3 (conteúdo).
> Versões: Java 25 LTS, Spring Boot 4.1.2, Spring Framework 7.0.x (ver `VERSOES.md`).
> Domínio dos exemplos: **Bazar** (ver `PROJETO-FINAL.md`).
> Verificado em 24/10/2026.

## Como ler este currículo

**Estrutura:** Parte → Módulo → Página. Cada página tem um ID `MM.PP` (módulo.página) que é usado nos pré-requisitos.

**Nível**
- **Básico**: precisa saber para fazer qualquer API.
- **Intermediário**: separa um CRUD de uma API profissional.
- **Avançado**: é o que se espera de quem projeta e opera APIs em produção.

**Prioridade**
- **Essencial**: não pule.
- **Importante**: pule só se tiver pressa; volte depois.
- **Complementar**: pode pular sem perder a sequência; nenhuma página essencial depende dela.

**Marcações**
- **"a verificar"**: informação que não consegui confirmar em fonte oficial. Todos os itens desse tipo foram resolvidos em 24/10/2026 (ver `VERIFICACOES.md`). Use a marcação de novo se surgir dúvida na fase 3.
- **🔒**: o assunto é aprofundado na documentação de segurança. Aqui fica só o essencial para APIs.

**Regra didática:** nenhuma página usa um conceito que ainda não foi explicado. Quando uma página precisa *mencionar* algo que vem depois, ela usa um aviso "visto em detalhes na página X" e não depende disso para funcionar.

## Visão geral

| Parte | Módulos | Páginas |
|---|---|---|
| 1. Começando e fundamentos de HTTP | M01–M02 | 10 |
| 2. REST e estilos de API | M03–M04 | 5 |
| 3. Construindo APIs com Spring Boot | M05–M09 | 16 |
| 4. Persistência para APIs | M10 | 6 |
| 5. Testes de API | M11 | 5 |
| 6. Design de APIs profissional | M12–M16 | 20 |
| 7. Contratos, documentação e ciclo de vida | M17–M18 | 10 |
| 8. Concorrência e execução assíncrona | M19 | 6 |
| 9. Padrões avançados de HTTP | M20–M23 | 11 |
| 10. Consumindo outras APIs | M24–M25 | 9 |
| 11. Segurança essencial para APIs 🔒 | M26 | 5 |
| 12. Observabilidade | M27 | 7 |
| 13. Performance | M28 | 5 |
| 14. Produção e deploy | M29 | 6 |
| 15. Arquitetura | M30 | 7 |
| 16. Projeto final: Bazar | M31 | 11 |
| Apêndices | M32 | 4 |
| **Total** | **32 módulos** | **143 páginas** |

## Trilhas sugeridas

- **Trilha mínima (só Essenciais):** permite construir e operar uma API profissional, cobrindo cerca de 60% das páginas.
- **Trilha completa:** todas as páginas, na ordem.
- **Leitor com pressa e já experiente em CRUD:** comece por 02.02, 02.03, 08.01, a Parte 6 e a Parte 9.

---

# Parte 1: Começando e fundamentos de HTTP

## M01: Começando

#### 01.01 · Como usar esta documentação
`Básico` · `Essencial`
- **Por que importa:** explica a organização, os níveis e as trilhas para o leitor não se perder em 140 páginas. Também apresenta o Bazar, que acompanha todos os exemplos.
- **Pré-requisitos:** nenhum.
- **Tópicos:**
  - Público-alvo e o que se espera ao final ("projeto e opero APIs de nível profissional")
  - Níveis, prioridades e trilhas
  - O que **não** está aqui: segurança a fundo 🔒, com link para a outra doc
  - Apresentação do Bazar: domínio, módulos e sistemas externos simulados
  - Como os exemplos estão organizados (repositório, uma *branch* ou *tag* por página: decisão da fase 2)
  - Convenções de nomes em português e inglês nos exemplos
- **Diagramas:**
  - Mapa da documentação (partes e dependências)
  - Visão geral do Bazar (módulos e sistemas externos)
- **Código:** nenhum.
- **Referências:** `PROJETO-FINAL.md`.

#### 01.02 · Preparando o ambiente
`Básico` · `Essencial`
- **Por que importa:** garante que todos os exemplos rodem igual na máquina do leitor.
- **Pré-requisitos:** 01.01
- **Tópicos:**
  - Instalar o JDK 25 (SDKMAN, distribuições Temurin, Liberica etc.)
  - Maven Wrapper
  - IDE: IntelliJ IDEA ou VS Code com extensões Java/Spring
  - Docker e Docker Compose (necessários a partir da Parte 4)
  - Ferramentas para chamar APIs: `curl`, HTTPie, Bruno/Insomnia/Postman, arquivos `.http` do IntelliJ
  - `jq` para ler JSON no terminal
- **Diagramas:** nenhum.
- **Código:**
  - `java -version`
  - `curl -i` e `http -v` contra uma API pública
  - Arquivo `.http` de exemplo
- **Referências:** https://adoptium.net · https://sdkman.io · https://httpie.io/docs/cli · https://docs.docker.com/compose/

## M02: HTTP essencial

#### 02.01 · Anatomia de uma mensagem HTTP
`Básico` · `Essencial`
- **Por que importa:** tudo em APIs REST é HTTP. Quem não lê uma requisição crua não consegue depurar problemas de integração.
- **Pré-requisitos:** 01.02
- **Tópicos:**
  - Cliente e servidor
  - URI (RFC 3986): esquema, host, porta, caminho, *query*, fragmento, codificação percentual
  - Linha de requisição, linha de status, campos de cabeçalho, corpo
  - Recurso × representação (primeira menção; aprofundado em 03.02)
  - Mensagens são texto no HTTP/1.1 e *frames* binários no HTTP/2 e HTTP/3, mas a semântica é a mesma (RFC 9110 separa semântica de sintaxe)
  - Como ver a mensagem real: `curl -v`, DevTools do navegador
- **Diagramas:**
  - Requisição e resposta lado a lado, com cada parte anotada
  - Decomposição de uma URI do Bazar (`https://api.bazar.example/v1/produtos?categoria=livros&limit=20`)
- **Código:** `curl -v` em `GET /v1/produtos` mostrando a saída comentada.
- **Referências:** https://www.rfc-editor.org/rfc/rfc9110 · https://www.rfc-editor.org/rfc/rfc3986 · https://developer.mozilla.org/pt-BR/docs/Web/HTTP/Messages

#### 02.02 · Métodos HTTP: semântica, segurança e idempotência
`Básico` · `Essencial`
- **Por que importa:** escolher o método errado quebra caches, *retries* automáticos e proxies. Idempotência é a base de toda a confiabilidade da Parte 9.
- **Pré-requisitos:** 02.01
- **Tópicos:**
  - GET, HEAD, POST, PUT, PATCH (RFC 5789), DELETE, OPTIONS
  - TRACE e CONNECT (só para saber que existem)
  - Propriedades: **seguro** (*safe*), **idempotente** e **cacheável**, em uma tabela por método
  - Idempotência significa efeito no servidor, não resposta igual (DELETE repetido pode dar 404 e continua idempotente)
  - Por que POST não é idempotente e as consequências para *retries*
  - GET com corpo: por que não usar
  - **Método QUERY (RFC 10008, jun/2026)**: consulta segura e idempotente com corpo; estado do suporte em servidores, proxies e no Spring 7.1 (suporte mínimo)
  - Métodos e CORS *preflight* (só a menção; ver 26.02)
- **Diagramas:**
  - Tabela-matriz método × propriedades
  - Sequência de um *retry* após *timeout* com PUT (seguro) e com POST (pedido duplicado)
- **Código:** sequência de `curl` no Bazar mostrando GET, POST, PUT e DELETE em `/v1/categorias`, sem Spring ainda; o servidor de exemplo vem pronto.
- **Referências:** RFC 9110 §9 https://www.rfc-editor.org/rfc/rfc9110#section-9 · RFC 5789 https://www.rfc-editor.org/rfc/rfc5789 · RFC 10008 https://www.rfc-editor.org/rfc/rfc10008.html

#### 02.03 · Status codes em profundidade
`Básico` · `Essencial`
- **Por que importa:** o status code é o primeiro (e às vezes o único) dado que clientes, gateways, monitoramento e *retries* leem. "200 com `{"erro": ...}`" é um dos erros mais comuns de iniciantes.
- **Pré-requisitos:** 02.02
- **Tópicos:**
  - As cinco classes de status
  - **2xx:** 200, 201 (com `Location`), 202, 204, 206
  - **3xx:** 301, 302, 303, 304, 307, 308
    - Diferença entre 302/303/307 e por que 303 aparece em operações assíncronas
  - **4xx:** 400, 401 × 403, 404, 405 (com `Allow`), 406, 409, 410, 412, 413, 415, 422 (agora "Unprocessable Content" na RFC 9110), 428, 429 (RFC 6585)
  - **5xx:** 500, 502, 503 (com `Retry-After`), 504
  - Quais status são "retentáveis"
  - 404 × 403 para esconder a existência de um recurso
  - Fluxograma de decisão de status
- **Diagramas:**
  - Fluxograma "qual status devolver?"
  - Mapa de status por fase do processamento: roteamento → autenticação → validação → regra de negócio → servidor
- **Código:** exemplos de respostas cruas do Bazar (201 com `Location`, 409 em categoria com produtos, 422 em pedido com quantidade 0).
- **Referências:** RFC 9110 §15 https://www.rfc-editor.org/rfc/rfc9110#section-15 · RFC 6585 https://www.rfc-editor.org/rfc/rfc6585 · https://developer.mozilla.org/pt-BR/docs/Web/HTTP/Status

#### 02.04 · Headers essenciais
`Básico` · `Essencial`
- **Por que importa:** metadados como localização, cache, *retries* e paginação viajam em headers. Uma API que ignora headers reinventa tudo no corpo.
- **Pré-requisitos:** 02.03
- **Tópicos:**
  - Headers de representação: `Content-Type`, `Content-Length`, `Content-Encoding`, `Content-Language`
  - De requisição: `Accept*`, `Authorization` (só a menção 🔒), `User-Agent`, `Host`
  - De resposta: `Location`, `Retry-After`, `Allow`, `Vary`
  - **`Link` (RFC 8288)** para paginação e relações
  - **`Prefer` / `Preference-Applied` (RFC 7240)**: `return=minimal` × `return=representation`, `respond-async`
  - Headers customizados: por que não usar o prefixo `X-` (RFC 6648)
  - **Structured Field Values (RFC 9651)**: a sintaxe moderna usada por `Deprecation`, `RateLimit` e outros
  - Headers hop-by-hop × end-to-end; diferenciação de maiúsculas e minúsculas
  - Limites de tamanho de headers nos servidores
- **Diagramas:** tabela "header → quem envia → quando → página onde é aprofundado".
- **Código:** respostas cruas do Bazar com `Location`, `Link` de paginação e `Retry-After`.
- **Referências:** RFC 9110 · RFC 8288 https://www.rfc-editor.org/rfc/rfc8288 · RFC 7240 https://www.rfc-editor.org/rfc/rfc7240 · RFC 6648 https://www.rfc-editor.org/rfc/rfc6648 · RFC 9651 https://www.rfc-editor.org/rfc/rfc9651

#### 02.05 · Content negotiation e media types
`Básico` · `Essencial`
- **Por que importa:** explica por que o Spring às vezes devolve 406 ou 415 e como uma mesma URI pode servir JSON, CSV ou uma versão diferente do recurso.
- **Pré-requisitos:** 02.04
- **Tópicos:**
  - Media types: tipo, subtipo e parâmetros (`charset`)
  - Sufixos `+json` (`application/problem+json`, `application/merge-patch+json`)
  - `Accept` com *q-values*
  - Negociação proativa × reativa
  - 406 × 415
  - `Accept-Language` e `Accept-Encoding`
  - Vendor media types (`application/vnd.bazar.produto.v2+json`), com menção ao versionamento em 18.02
  - Por que o `Vary` é obrigatório quando a resposta depende de headers
- **Diagramas:** sequência de negociação: cliente pede CSV, servidor responde CSV ou 406.
- **Código:** `curl` com `Accept: text/csv` e `Accept: application/json` em `/v1/relatorios/{id}/arquivo`. A implementação vem em 09.03 e 22.02.
- **Referências:** RFC 9110 §12 https://www.rfc-editor.org/rfc/rfc9110#section-12 · RFC 6838 https://www.rfc-editor.org/rfc/rfc6838 · IANA Media Types https://www.iana.org/assignments/media-types/

#### 02.06 · JSON para APIs
`Básico` · `Essencial`
- **Por que importa:** JSON parece trivial, mas números grandes, `null` × ausente e convenções de nomes causam bugs de integração silenciosos.
- **Pré-requisitos:** 02.05
- **Tópicos:**
  - RFC 8259: tipos e codificação UTF-8 obrigatória
  - Números: precisão IEEE 754 no JavaScript (inteiros acima de 2⁵³ − 1), por que IDs `long` viajam como string
  - `null` × campo ausente × string vazia (semântica diferente; crucial para PATCH em 14.02)
  - Objetos no topo × arrays no topo (evolução)
  - camelCase × snake_case: decisão do Bazar (camelCase) e coerência
  - JSON Schema 2020-12 (só a menção; aprofundado em 17.01)
  - NDJSON / JSON Lines para *streaming* (Complementar)
- **Diagramas:** nenhum (tabelas comparativas bastam).
- **Código:** exemplos de JSON bom e ruim de `Produto`; demonstração em JS do problema de precisão com `9007199254740993`.
- **Referências:** RFC 8259 https://www.rfc-editor.org/rfc/rfc8259 · https://json-schema.org/draft/2020-12 · https://github.com/ndjson/ndjson-spec

#### 02.07 · Cookies, estado e sessões
`Intermediário` · `Importante`
- **Por que importa:** a restrição *stateless* do REST costuma ser mal entendida. Saber onde o estado mora evita APIs que não escalam horizontalmente.
- **Pré-requisitos:** 02.04
- **Tópicos:**
  - Estado da aplicação (no cliente) × estado do recurso (no servidor)
  - Cookies (RFC 6265): `Set-Cookie`, `HttpOnly`, `Secure`, `SameSite`
  - Sessão no servidor × token; por que APIs públicas evitam cookies
  - Quando cookies fazem sentido: BFF (29.05) e SPA no mesmo domínio 🔒
  - *Sticky sessions* e escala horizontal
- **Diagramas:** duas instâncias atrás de um balanceador, comparando sessão em memória (quebra) com estado no cliente ou em repositório compartilhado.
- **Código:** `curl` mostrando `Set-Cookie`; nenhum código Spring.
- **Referências:** RFC 6265 https://www.rfc-editor.org/rfc/rfc6265 · RFC 6265bis (`draft-ietf-httpbis-rfc6265bis-22`, aprovado pelo IESG e na fila do RFC Editor em out/2026) https://datatracker.ietf.org/doc/draft-ietf-httpbis-rfc6265bis/ · Fielding cap. 5.1.3

#### 02.08 · HTTP/1.1, HTTP/2 e HTTP/3
`Intermediário` · `Importante`
- **Por que importa:** influencia a latência, o custo de conexões entre serviços e decisões como "agrupar chamadas ou não".
- **Pré-requisitos:** 02.01
- **Tópicos:**
  - HTTP/1.1 (RFC 9112): *keep-alive*, *pipelining* (morto), bloqueio *head-of-line*
  - HTTP/2 (RFC 9113): multiplexação, compressão HPACK, *streams*, h2c × h2 com TLS
  - HTTP/3 (RFC 9114): QUIC sobre UDP, 0-RTT e seus riscos de *replay*
  - TLS em visão geral 🔒
  - Como habilitar HTTP/2 no Spring Boot (`server.http2.enabled`)
  - Onde o HTTP/3 normalmente termina: no CDN ou no *load balancer*
  - Impacto para APIs: menos motivo para "batch endpoints" só por latência
- **Diagramas:**
  - Linha do tempo de requisições: HTTP/1.1 com 6 conexões × HTTP/2 com uma conexão multiplexada
  - Pilha de protocolos (TCP+TLS × QUIC)
- **Código:** `curl --http2 -v` e `curl --http3` (se disponível); propriedade `server.http2.enabled=true`.
- **Referências:** RFC 9112 https://www.rfc-editor.org/rfc/rfc9112 · RFC 9113 https://www.rfc-editor.org/rfc/rfc9113 · RFC 9114 https://www.rfc-editor.org/rfc/rfc9114 · https://docs.spring.io/spring-boot/how-to/webserver.html

---

# Parte 2: REST e estilos de API

## M03: REST de verdade

#### 03.01 · As restrições de Fielding
`Intermediário` · `Essencial`
- **Por que importa:** REST é um estilo arquitetural com restrições precisas, não "JSON sobre HTTP". Entender as restrições explica *por que* as boas práticas existem.
- **Pré-requisitos:** 02.07
- **Tópicos:**
  - Origem: a dissertação de Roy Fielding (2000), capítulo 5
  - As seis restrições: cliente-servidor, *stateless*, *cache*, interface uniforme, sistema em camadas, *code-on-demand* (opcional)
  - Os quatro elementos da interface uniforme:
    - identificação de recursos
    - manipulação por representações
    - mensagens autodescritivas
    - HATEOAS
  - O que cada restrição traz (escala, evolução independente, visibilidade) e quanto custa
  - O que a maioria das "APIs REST" realmente é (e por que isso tudo bem, desde que seja consciente)
- **Diagramas:** as restrições como camadas sucessivas, como na figura do cap. 5 da dissertação, redesenhada.
- **Código:** nenhum.
- **Referências:** https://ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm · https://roy.gbiv.com/untangled/2008/rest-apis-must-be-hypertext-driven

#### 03.02 · Recursos, representações e a interface uniforme
`Intermediário` · `Essencial`
- **Por que importa:** pensar em recursos (e não em tabelas ou métodos Java) é o que diferencia um design REST de um RPC disfarçado.
- **Pré-requisitos:** 03.01
- **Tópicos:**
  - Recurso como conceito; URI como identificador; representação como "foto" do estado
  - Um recurso pode ter várias representações
  - Recursos não são tabelas: recursos agregados, recursos de processo (um "cancelamento"), recursos calculados (uma "cotação")
  - Mensagens autodescritivas na prática: `Content-Type` correto e status corretos
- **Diagramas:** o mesmo Pedido visto como linha de tabela, objeto Java e representação JSON (três camadas desacopladas).
- **Código:** JSON de `Pedido` e uma representação CSV do mesmo relatório.
- **Referências:** Fielding cap. 5.2 · RFC 9110 §3

#### 03.03 · O modelo de maturidade de Richardson
`Básico` · `Importante`
- **Por que importa:** é um vocabulário comum para avaliar APIs existentes e conversar com o time.
- **Pré-requisitos:** 03.02
- **Tópicos:**
  - Os níveis 0 (POX/RPC), 1 (recursos), 2 (verbos e status) e 3 (hipermídia)
  - Exemplos de cada nível com o mesmo caso do Bazar
  - Críticas ao modelo
  - Onde o mercado está (nível 2) e quando subir para o 3
- **Diagramas:** escada de quatro níveis com a mesma operação "cancelar pedido" em cada um.
- **Código:** requisições cruas de cada nível.
- **Referências:** https://martinfowler.com/articles/richardsonMaturityModel.html

#### 03.04 · HATEOAS e hipermídia
`Intermediário` · `Complementar`
- **Por que importa:** links na resposta permitem ao cliente descobrir ações válidas (por exemplo, "pode cancelar?") sem duplicar regras de negócio, mas têm custo. Saber quando vale a pena é sinal de maturidade.
- **Pré-requisitos:** 03.03
- **Tópicos:**
  - Links, relações (`rel`) e o registro IANA de *link relations*
  - Formatos: HAL, HAL-FORMS; menção a JSON:API e Siren
  - Spring HATEOAS 3.1 apresentado só conceitualmente; a implementação fica em 12.04
  - *Affordances* (ações disponíveis conforme o estado)
  - Custos: tamanho do payload, complexidade, clientes que ignoram links
  - Alternativa leve: só o header `Link` e os links de paginação
- **Diagramas:** pedido `CRIADO` com links `pagar`/`cancelar` × pedido `PAGO` com o link `reembolsar`.
- **Código:** respostas HAL cruas (JSON) de um pedido em estados diferentes; sem Spring ainda.
- **Referências:** https://docs.spring.io/spring-hateoas/docs/current/reference/html/ · https://datatracker.ietf.org/doc/html/draft-kelly-json-hal · https://www.iana.org/assignments/link-relations/

## M04: Estilos de API

#### 04.01 · REST, RPC, GraphQL, gRPC, WebSocket, SSE e webhooks: quando usar cada um
`Intermediário` · `Importante`
- **Por que importa:** um sênior escolhe o estilo pelo problema, não pela moda, e consegue justificar a escolha.
- **Pré-requisitos:** 03.03
- **Tópicos:**
  - RPC (JSON-RPC) e gRPC: contratos Protobuf, HTTP/2, *streaming*, desempenho, comunicação interna
  - GraphQL: consultas do cliente, *over/under-fetching*, N+1 no servidor, *cache* difícil
  - WebSocket: bidirecional, estado de conexão
  - SSE: do servidor para o cliente, simples, sobre HTTP
  - Webhooks: o servidor chama o cliente
  - Long polling
  - Tabela comparativa: acoplamento, *cache*, ferramentas, navegador, evolução
  - Combinações comuns: REST público + gRPC interno + webhooks
- **Diagramas:**
  - Quadro comparativo
  - Árvore de decisão "qual estilo?"
  - Sequências lado a lado: polling × SSE × webhook para "pedido foi pago"
- **Código:** a mesma consulta "produto por id" em REST, GraphQL (query) e gRPC (`.proto`), só para comparar a forma.
- **Referências:** https://graphql.org/learn/ · https://grpc.io/docs/what-is-grpc/introduction/ · https://html.spec.whatwg.org/multipage/server-sent-events.html · RFC 6455 (WebSocket)

---

# Parte 3: Construindo APIs com Spring Boot

## M05: Primeiro projeto

#### 05.01 · Spring Boot 4 em uma página
`Básico` · `Essencial`
- **Por que importa:** o leitor sabe o básico de Spring Boot, mas a versão 4 mudou coisas importantes (Jackson 3, modularização, null safety). Esta página nivela o conhecimento.
- **Pré-requisitos:** 01.02
- **Tópicos:**
  - Inversão de controle e injeção de dependências (revisão rápida); autoconfiguração; *starters*
  - Spring Boot 4.x e Spring Framework 7.x
    - baseline Java 17
    - Jakarta EE 11
    - Jackson 3 como padrão
    - JSpecify (`@Nullable`, `@NullMarked`)
    - modularização dos *starters* e da autoconfiguração: `spring-boot-starter-webmvc` substitui `spring-boot-starter-web` (depreciado); testes com `spring-boot-starter-webmvc-test`; `RestClient` em `spring-boot-starter-restclient`
  - Remoções: Undertow, suporte a `javax.*`
  - Ciclo de lançamentos e suporte (nov/maio) e onde consultar
- **Diagramas:** linha do tempo Boot 2 → 3 → 4 com as mudanças-chave.
- **Código:** `pom.xml` mínimo com o *parent* 4.1.2; `@SpringBootApplication`; `package-info.java` com `@NullMarked`.
- **Referências:** https://docs.spring.io/spring-boot/ · https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes · https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes · https://jspecify.dev/

#### 05.02 · Criando o projeto Bazar
`Básico` · `Essencial`
- **Por que importa:** a estrutura de pacotes definida aqui se mantém até o projeto final. Organizar por módulo de negócio evita o "pacote `service` com 80 classes".
- **Pré-requisitos:** 05.01
- **Tópicos:**
  - start.spring.io: dependências iniciais
  - Estrutura de pacotes por módulo (`com.bazar.catalogo`) × por camada; camadas dentro do módulo (api, domínio, infraestrutura)
  - `application.yml`
  - Executar com `./mvnw spring-boot:run`
  - DevTools
  - Suporte a Docker Compose no desenvolvimento (`spring-boot-docker-compose`), já preparado para a Parte 4
- **Diagramas:** árvore de pacotes do Bazar; camadas e dependências permitidas entre elas.
- **Código:** estrutura inicial, `application.yml` e `compose.yaml` vazio.
- **Referências:** https://start.spring.io · https://docs.spring.io/spring-boot/reference/features/dev-services.html (página "Development-time Services", que cobre Docker Compose e Testcontainers)

#### 05.03 · Primeiro endpoint
`Básico` · `Essencial`
- **Por que importa:** é a porta de entrada para entender o que o Spring faz por baixo quando um método vira um endpoint.
- **Pré-requisitos:** 05.02, 02.03
- **Tópicos:**
  - `@RestController` = `@Controller` + `@ResponseBody`
  - `@RequestMapping`, `@GetMapping`, `@PathVariable`, `@RequestParam` (obrigatório, padrão, opcional), `@RequestHeader`
  - Retornar um objeto × retornar `ResponseEntity`
  - Conversão automática para JSON (detalhada em 09.03)
  - Testar com `curl`
- **Diagramas:** fluxo simplificado "requisição → controller → JSON". A versão completa fica em 09.01.
- **Código:** `CategoriaController` com `GET /v1/categorias` e `GET /v1/categorias/{id}` usando uma lista em memória.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller.html

#### 05.04 · Um CRUD com status corretos
`Básico` · `Essencial`
- **Por que importa:** a maioria dos CRUDs devolve 200 para tudo. Aqui o leitor aplica 201, 204 e 404 corretamente desde o primeiro dia.
- **Pré-requisitos:** 05.03
- **Tópicos:**
  - POST com `@RequestBody` → 201 com `Location` (`ServletUriComponentsBuilder`)
  - PUT → 200 ou 204; DELETE → 204
  - GET de um id inexistente → 404 (`ResponseEntity.notFound()` e `ResponseStatusException`)
  - `HEAD` e `OPTIONS` automáticos no Spring
  - Limitações desta versão (sem validação, erros feios, entidade exposta), que motivam os módulos seguintes
- **Diagramas:** tabela "operação → método → URI → status de sucesso → status de erro".
- **Código:** CRUD completo de `Categoria` em memória; sessão de `curl` mostrando cada status.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/responseentity.html · RFC 9110 §15.3.2 (201)

## M06: Modelando entrada e saída

#### 06.01 · DTOs com records e por que não expor entidades
`Básico` · `Essencial`
- **Por que importa:** expor a entidade acopla o contrato ao banco, vaza campos sensíveis e abre espaço para *mass assignment*.
- **Pré-requisitos:** 05.04
- **Tópicos:**
  - DTO de entrada × DTO de saída (`CriarProdutoRequest`, `AtualizarProdutoRequest`, `ProdutoResponse`)
  - Records do Java (construtor compacto, imutabilidade)
  - Um DTO por caso de uso × DTO genérico
  - Onde fica a conversão
  - *Mass assignment* / *over-posting* (preparando 26.03)
  - Records aninhados
- **Diagramas:** fronteira da API: DTO ↔ domínio ↔ persistência, com setas de conversão.
- **Código:** records do `Produto`; controller recebendo `CriarProdutoRequest` e retornando `ProdutoResponse`.
- **Referências:** JEP 395 (Records) https://openjdk.org/jeps/395 · https://martinfowler.com/eaaCatalog/dataTransferObject.html

#### 06.02 · Mapeamento: manual ou MapStruct
`Básico` · `Importante`
- **Por que importa:** o mapeamento manual cresce rápido e esconde bugs. O MapStruct gera código verificado em compilação, sem reflexão.
- **Pré-requisitos:** 06.01
- **Tópicos:**
  - Mapeamento manual com métodos estáticos (`ProdutoResponse.de(produto)`)
  - MapStruct 1.6.3:
    - configuração com o processador de anotações
    - `componentModel = "spring"`
    - `@Mapping`
    - `unmappedTargetPolicy = ERROR`
    - mapeamento de records
    - `@MappingTarget` para atualização
    - *mappers* compostos
  - Quando não usar MapStruct (poucos DTOs)
  - Lombok: por que esta doc não usa (records cobrem quase tudo)
- **Diagramas:** nenhum.
- **Código:** `ProdutoMapper` com conversão de `precoEmCentavos`; teste mostrando erro de compilação quando um campo não é mapeado.
- **Referências:** https://mapstruct.org/documentation/stable/reference/html/

#### 06.03 · Jackson 3 no Spring Boot 4
`Intermediário` · `Essencial`
- **Por que importa:** a serialização define o contrato real da API. No Boot 4 o Jackson mudou de pacote e de padrões, e muitos tutoriais antigos deixaram de funcionar.
- **Pré-requisitos:** 06.01, 02.06
- **Tópicos:**
  - Jackson 3: pacote `tools.jackson`, `JsonMapper` imutável e construído por *builder*
  - Anotações que continuam em `com.fasterxml.jackson.annotation`
  - Diferenças de padrão em relação ao Jackson 2:
    - `java.time` embutido no databind, sem módulo separado
    - `WRITE_DATES_AS_TIMESTAMPS=false`
    - `FAIL_ON_UNKNOWN_PROPERTIES=false`
    - `FAIL_ON_NULL_FOR_PRIMITIVES=true`
    - `FAIL_ON_TRAILING_TOKENS=true`
    - `SORT_PROPERTIES_ALPHABETICALLY=true` (muda a ordem dos campos no JSON!)
    - enums lidos e escritos via `toString()`
    - exceções não checadas (`JacksonException`)
    - profundidade máxima de aninhamento 500
    - `spring.jackson.use-jackson2-defaults=true` restaura os padrões do Boot 3
  - Configuração via `spring.jackson.*`: `spring.jackson.json.read.*`/`json.write.*` (específicas de JSON, Boot 4.0) e `spring.jackson.read.*`/`write.*` (comuns a vários formatos, Boot 4.1)
  - `JsonMapperBuilderCustomizer` (substitui `Jackson2ObjectMapperBuilderCustomizer`); para trocar o mapper, defina um bean `JsonMapper` (não `ObjectMapper`); `@JacksonComponent`/`@JacksonMixin` substituem `@JsonComponent`/`@JsonMixin`
  - Falhar em propriedades desconhecidas: prós e contras, e o *tolerant reader* (15.04 e 18.01)
  - Inclusão de `null` (`NON_NULL`, `NON_ABSENT`); naming strategy
  - `@JsonProperty`, `@JsonIgnore`, `@JsonCreator`
  - Serializadores customizados (`ValueSerializer`, Jackson 3)
  - Polimorfismo com `@JsonTypeInfo` e cuidados de segurança
  - Jackson 2 como *fallback* depreciado
- **Diagramas:** pipeline objeto → `JsonMapper` → bytes, com pontos de customização.
- **Código:** `application.yml` com as configurações do Bazar; serializador de `Dinheiro` (antecipando 15.02); teste de serialização com `@JsonTest`.
- **Referências:** https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0 · https://docs.spring.io/spring-boot/reference/features/json.html · Spring Framework 7.0 Release Notes

## M07: Validação

#### 07.01 · Bean Validation essencial
`Básico` · `Essencial`
- **Por que importa:** validar na borda impede dados inválidos de chegarem ao domínio e devolve erros úteis ao cliente.
- **Pré-requisitos:** 06.01
- **Tópicos:**
  - Jakarta Bean Validation 3.1 / Hibernate Validator 9.1
  - `spring-boot-starter-validation`
  - Constraints comuns: `@NotNull`, `@NotBlank`, `@Size`, `@Positive`, `@Email`, `@Pattern`
  - `@Valid` em `@RequestBody`
  - Validação de `@PathVariable` e `@RequestParam`: validação de métodos embutida no Spring MVC desde a 6.1, `HandlerMethodValidationException`
  - Validação em records e em objetos aninhados (`@Valid` em cascata)
  - Validação sintática (na borda) × regra de negócio (no domínio)
  - Erro padrão antes da Parte 8
- **Diagramas:** onde cada tipo de validação acontece (borda × domínio × banco).
- **Código:** `CriarProdutoRequest` com constraints; resposta 400 padrão do Spring.
- **Referências:** https://jakarta.ee/specifications/bean-validation/3.1/ · https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html · https://docs.jboss.org/hibernate/validator/9.1/reference/en-US/html_single/

#### 07.02 · Validação avançada
`Intermediário` · `Importante`
- **Por que importa:** regras reais (CPF, intervalos de datas, campos dependentes) não cabem nas anotações prontas.
- **Pré-requisitos:** 07.01
- **Tópicos:**
  - Validadores customizados (`ConstraintValidator`): `@Cpf`, `@Sku`
  - Validação entre campos (*cross-field*): `precoMin <= precoMax`, por meio de constraint de classe
  - Grupos de validação (`@Validated(OnCreate.class)`) e quando preferir DTOs separados
  - Validação de coleções e elementos (`List<@Valid ItemPedidoRequest>`)
  - Mensagens customizadas e i18n (`ValidationMessages.properties`, `MessageSource`)
  - Validação no serviço (`@Validated` em beans)
  - Quanto validar: borda × domínio
- **Diagramas:** nenhum.
- **Código:** `@Cpf` com o algoritmo; `@IntervaloPrecoValido`; `CriarPedidoRequest` com itens validados; testes unitários dos validadores.
- **Referências:** Hibernate Validator Reference (custom constraints) · Bean Validation 3.1 spec

## M08: Tratamento de erros

#### 08.01 · Problem Details (RFC 9457)
`Básico` · `Essencial`
- **Por que importa:** um formato padrão de erro permite que clientes tratem falhas de forma genérica. A RFC 9457 é o padrão que o Spring adota.
- **Pré-requisitos:** 02.05, 07.01
- **Tópicos:**
  - Campos `type`, `title`, `status`, `detail`, `instance` e o significado exato de cada um
  - Media type `application/problem+json`
  - Extensões (`codigo`, `erros[]`, `traceId`)
  - `type` como URI estável e documentável (`about:blank`)
  - O que mudou da RFC 7807 para a 9457: registro de tipos, múltiplos problemas
  - Comparação com os formatos de erro do Google (AIP-193) e da Microsoft
- **Diagramas:** anatomia de um ProblemDetail do Bazar anotado campo a campo.
- **Código:** exemplos JSON para 404, 409 e 422 com lista de erros de validação.
- **Referências:** https://www.rfc-editor.org/rfc/rfc9457 · https://google.aip.dev/193 · https://github.com/microsoft/api-guidelines/blob/vNext/azure/Guidelines.md (seção de erros)

#### 08.02 · ProblemDetail no Spring: tratamento global de erros
`Intermediário` · `Essencial`
- **Por que importa:** centralizar o tratamento de erros elimina `try/catch` nos controllers e garante respostas consistentes, sem vazar *stack traces*.
- **Pré-requisitos:** 08.01
- **Tópicos:**
  - `ProblemDetail`, `ErrorResponse`, `ErrorResponseException`
  - `spring.mvc.problemdetails.enabled`
  - `@RestControllerAdvice` estendendo `ResponseEntityExceptionHandler`
  - Exceções de domínio (`RecursoNaoEncontradoException`, `RegraDeNegocioException`) e a hierarquia
  - Mapear `MethodArgumentNotValidException` e `HandlerMethodValidationException` para `erros[]`
  - Mensagens via `MessageSource` (códigos `problemDetail.*`)
  - Erro 500 genérico: logar e devolver só o `traceId`
  - Erros fora do Spring MVC (filtros, 404 de rota): `BasicErrorController` / `ErrorAttributes`
  - Mudança no Spring 7.1: `instance` deixa de ser preenchido automaticamente
- **Diagramas:** fluxo da exceção: controller → `HandlerExceptionResolver` → `@ControllerAdvice` → ProblemDetail.
- **Código:** `TratadorGlobalDeErros` do Bazar; `RecursoNaoEncontradoException`; teste verificando `application/problem+json`.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html · https://docs.spring.io/spring-boot/reference/web/servlet.html (error handling) · Spring Framework 7.1 Release Notes

## M09: Spring MVC por dentro

#### 09.01 · O caminho de uma requisição no Spring MVC
`Intermediário` · `Essencial`
- **Por que importa:** sem esse modelo mental, depurar 404, 406 ou um JSON estranho vira tentativa e erro. Ele também é a base para estender o framework.
- **Pré-requisitos:** 08.02
- **Tópicos:**
  - Servlet container (Tomcat 11) → cadeia de filtros → `DispatcherServlet`
  - `HandlerMapping` (`RequestMappingHandlerMapping`; `PathPattern` × `AntPathMatcher` depreciado)
  - `HandlerExecutionChain` com interceptors
  - `HandlerAdapter` (`RequestMappingHandlerAdapter`) → resolução de argumentos → método → *return value handlers* → `HttpMessageConverter`
  - `HandlerExceptionResolver`
  - Como ver tudo isso: *debug* com *breakpoints* e `logging.level.org.springframework.web=DEBUG`
- **Diagramas:** **diagrama principal da parte**: sequência completa da requisição, com cada componente e ponto de extensão.
- **Código:** log de DEBUG de uma requisição real do Bazar, comentado.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet.html

#### 09.02 · Argument resolvers e return value handlers customizados
`Avançado` · `Complementar`
- **Por que importa:** permite injetar dados recorrentes (cliente autenticado, contexto de paginação) sem repetir código em todo controller.
- **Pré-requisitos:** 09.01
- **Tópicos:**
  - `HandlerMethodArgumentResolver`: `supportsParameter` e `resolveArgument`
  - Registro em `WebMvcConfigurer`
  - Exemplo `@ContextoRequisicao` (canal e idioma)
  - `HandlerMethodReturnValueHandler`: quando faz sentido e quando é exagero
  - Alternativas mais simples: `@ModelAttribute`, `@RequestAttribute`
- **Diagramas:** nenhum (reutiliza o de 09.01, com destaque).
- **Código:** resolver de `ContextoRequisicao` a partir de headers; teste com `@WebMvcTest` (antecipado como "rode assim; explicação em 11.03").
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/customize.html · Javadoc de `HandlerMethodArgumentResolver`

#### 09.03 · HttpMessageConverter e a serialização por dentro
`Intermediário` · `Importante`
- **Por que importa:** explica 406/415 e como servir CSV, PDF ou outros formatos sem *hacks*.
- **Pré-requisitos:** 09.01, 02.05, 06.03
- **Tópicos:**
  - Lista de *converters* padrão e a ordem entre eles
  - Escolha pelo `Accept` e pelo `produces`
  - `consumes` e `produces` nos *mappings*
  - Converter customizado (`text/csv`)
  - `HttpMessageConverters` no Boot e a forma de adicionar *converters*
  - *Content negotiation* por sufixo ou parâmetro (desligada por padrão e por quê)
  - Mudanças no Framework 7.1 (`MultipartHttpMessageConverter`, JAXB deixando de ser detectado automaticamente)
- **Diagramas:** seleção do converter: lista ordenada × `Accept` → escolhido.
- **Código:** `CsvHttpMessageConverter` para `List<ProdutoResponse>`; `curl` com `Accept: text/csv`.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/message-converters.html · https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/content-negotiation.html

#### 09.04 · Filtros, interceptors e AOP: quando usar cada um
`Intermediário` · `Importante`
- **Por que importa:** é uma decisão frequente (logging, correlation ID, métricas, auditoria). Escolher o mecanismo errado causa bugs de ordem e de exceções não tratadas.
- **Pré-requisitos:** 09.01
- **Tópicos:**
  - `Filter` / `OncePerRequestFilter`: antes do `DispatcherServlet`, acesso ao *request* cru, ordem com `@Order` / `FilterRegistrationBean`
  - `HandlerInterceptor`: `preHandle`, `postHandle`, `afterCompletion`; sabe qual handler foi escolhido
  - AOP (`@Aspect`) em serviços
  - Tabela de decisão
  - Por que exceções em filtros não passam pelo `@ControllerAdvice`
  - Ler o corpo duas vezes: `ContentCachingRequestWrapper` e seus custos
- **Diagramas:** camadas concêntricas (filtro ⊃ dispatcher ⊃ interceptor ⊃ controller ⊃ aspecto no serviço).
- **Código:** `TempoDeRespostaFilter`; `AuditoriaInterceptor` que lê a anotação do handler; aspecto `@Auditavel`.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc/filters.html · https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/interceptors.html

#### 09.05 · Functional endpoints (RouterFunction)
`Intermediário` · `Complementar`
- **Por que importa:** é uma alternativa às anotações, útil para roteamento dinâmico e para quem vem do WebFlux.
- **Pré-requisitos:** 09.01
- **Tópicos:**
  - `RouterFunction`, `HandlerFunction`, `ServerRequest`/`ServerResponse`
  - Predicados, filtros funcionais, versionamento por rota (`#api-version`)
  - Prós e contras em relação às anotações; integração com springdoc
- **Diagramas:** nenhum.
- **Código:** `GET /v1/categorias` reescrito em estilo funcional.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc-functional.html

---

# Parte 4: Persistência para APIs

## M10: Spring Data JPA a serviço da API

#### 10.01 · Spring Data JPA essencial para APIs
`Básico` · `Essencial`
- **Por que importa:** quase toda API persiste dados. Usar JPA sem entender o que ele faz é a principal fonte de problemas de desempenho em APIs Spring.
- **Pré-requisitos:** 06.01
- **Tópicos:**
  - PostgreSQL via Docker Compose (suporte do Boot)
  - Entidades JPA 3.2 com Hibernate 7.4; `@Entity`, `@Id`, estratégias de geração
  - Ciclo de vida: *managed*, *detached* e *dirty checking*
  - `JpaRepository`, *query methods*, `@Query`
  - Entidade ≠ DTO (reforço de 06.01)
  - `spring.jpa.open-in-view`: o que é, por que vem ligado e por que o Bazar desliga
  - `ddl-auto`: só em protótipos
  - Log de SQL com parâmetros
- **Diagramas:** contexto de persistência dentro de uma requisição (o que acontece com OSIV ligado e desligado).
- **Código:** entidade `Produto`, `ProdutoRepository`, `compose.yaml` com Postgres, `application.yml` com `open-in-view: false`.
- **Referências:** https://docs.spring.io/spring-data/jpa/reference/ · https://hibernate.org/orm/documentation/ · https://docs.spring.io/spring-boot/reference/data/sql.html

#### 10.02 · Migrations com Flyway
`Básico` · `Essencial`
- **Por que importa:** o esquema do banco precisa ser versionado como o código. Sem isso, *deploys* e ambientes divergem.
- **Pré-requisitos:** 10.01
- **Tópicos:**
  - Flyway 12: migrations versionadas (`V1__...`) e *repeatable* (`R__`)
  - Convenções de nomes; `flyway_schema_history`
  - Migrations em produção: nunca editar uma migration aplicada
  - **Expand-contract** (*parallel change*) para mudar o esquema sem quebrar a versão anterior da API (liga com 18.01 e 29.04)
  - Dados de referência × dados de teste
  - Flyway × Liquibase (uma frase)
- **Diagramas:** expand-contract em três *deploys* (adicionar coluna → migrar dados e escrever nas duas → remover a antiga).
- **Código:** `V1__criar_catalogo.sql`, `V2__criar_estoque.sql`; renomeação de coluna feita com expand-contract.
- **Referências:** https://documentation.red-gate.com/fd · https://martinfowler.com/bliki/ParallelChange.html · https://docs.spring.io/spring-boot/how-to/data-initialization.html

#### 10.03 · Transações na camada de serviço
`Intermediário` · `Essencial`
- **Por que importa:** limites de transação errados geram dados inconsistentes, conexões presas e *deadlocks*. Chamar uma API externa dentro de uma transação é um erro clássico.
- **Pré-requisitos:** 10.01
- **Tópicos:**
  - `@Transactional`: onde colocar (serviço, não controller); `readOnly = true`
  - Propagação (`REQUIRED`, `REQUIRES_NEW`) e isolamento
  - *Rollback* em *checked* × *unchecked exceptions*
  - *Self-invocation* e *proxies*
  - **Não fazer I/O remoto dentro da transação**, com o exemplo da PagaFácil
  - `TransactionTemplate`
  - Eventos após o *commit* (`@TransactionalEventListener`), preparando 30.04
- **Diagramas:**
  - Linha do tempo da conexão segurada durante uma chamada HTTP lenta × conexão liberada
  - Proxy transacional
- **Código:** `PedidoService.criar` com reserva de estoque na mesma transação; versão errada e versão certa da chamada à PagaFácil.
- **Referências:** https://docs.spring.io/spring-framework/reference/data-access/transaction.html

#### 10.04 · O problema N+1 e estratégias de *fetch*
`Intermediário` · `Essencial`
- **Por que importa:** é a causa nº 1 de endpoints lentos em APIs com JPA, e só aparece com dados reais.
- **Pré-requisitos:** 10.03
- **Tópicos:**
  - `LAZY` × `EAGER` (por que `EAGER` não resolve)
  - Como detectar: log de SQL, contagem de *queries* em teste, Hibernate Statistics
  - Soluções: `JOIN FETCH`, `@EntityGraph`, `@BatchSize` / `default_batch_fetch_size`, projeções de DTO
  - Paginação com `JOIN FETCH` de coleção: o aviso "firstResult/maxResults specified with collection fetch" e a técnica de duas *queries*
  - `LazyInitializationException` com OSIV desligado
- **Diagramas:** número de *queries* em `GET /v1/pedidos` antes e depois (1 + N → 2).
- **Código:** `PedidoRepository` com `@EntityGraph`; teste que verifica o número de *queries* (com `datasource-proxy-spring-boot-starter` 2.0.x, compatível com Boot 4, ou com as estatísticas do Hibernate em `hibernate.generate_statistics`).
- **Referências:** Spring Data JPA Reference (Entity Graphs) · Hibernate ORM User Guide (fetching) · https://vladmihalcea.com/n-plus-1-query-problem/

#### 10.05 · Projeções e consultas para leitura
`Intermediário` · `Importante`
- **Por que importa:** carregar entidades completas para montar uma listagem desperdiça memória e banco. Projeções devolvem só o necessário.
- **Pré-requisitos:** 10.04
- **Tópicos:**
  - Projeções por interface, por record (DTO) e dinâmicas
  - `@Query` com construtor de record
  - Consultas dinâmicas para filtros: `Specification` (incluindo a API fluente) e Querydsl (menção)
  - `JdbcClient` para relatórios e consultas complexas
  - CQRS leve: modelo de leitura separado
- **Diagramas:** escolha "entidade × projeção × JdbcClient" por caso de uso.
- **Código:** `ProdutoResumo` (record) para listagens; `ProdutoSpecifications` para os filtros de 13.01; relatório de vendas com `JdbcClient`.
- **Referências:** Spring Data JPA Reference (Projections, Specifications) · https://docs.spring.io/spring-framework/reference/data-access/jdbc/core.html (`JdbcClient`)

#### 10.06 · Concorrência no banco: *locking* otimista e pessimista
`Intermediário` · `Essencial`
- **Por que importa:** duas requisições simultâneas reduzindo o mesmo estoque causam *overselling*. A API precisa de uma estratégia explícita.
- **Pré-requisitos:** 10.03
- **Tópicos:**
  - *Lost update*
  - `@Version` e `OptimisticLockException` → 409 na API
  - *Lock* pessimista (`@Lock(PESSIMISTIC_WRITE)`, `SELECT ... FOR UPDATE`, `SKIP LOCKED`)
  - Atualização atômica (`UPDATE ... SET qtd = qtd - :n WHERE qtd >= :n`)
  - Como escolher; impacto em *retries*
  - Preparação para `ETag`/`If-Match` (20.03)
- **Diagramas:** sequência de duas requisições concorrentes nos três cenários (sem controle, otimista, atômica).
- **Código:** `Estoque` com `@Version`; reserva com `UPDATE` atômico; teste concorrente com `ExecutorService` / virtual threads.
- **Referências:** Hibernate ORM User Guide (Locking) · Jakarta Persistence 3.2 spec · https://www.postgresql.org/docs/current/explicit-locking.html

---

# Parte 5: Testes de API

## M11: Testando APIs Spring Boot

#### 11.01 · Estratégia de testes para APIs
`Básico` · `Essencial`
- **Por que importa:** sem estratégia os testes ficam lentos, frágeis ou não pegam bugs reais. Esta página define o que testar em cada camada.
- **Pré-requisitos:** 10.01
- **Tópicos:**
  - Pirâmide × "troféu" de testes
  - Testes unitários, *slice tests*, integração, ponta a ponta e contrato (ligação com 18.05)
  - O que cada um prova
  - Velocidade e isolamento
  - Cache de contexto do Spring e como não destruí-lo (`@MockitoBean` com moderação)
  - Nomenclatura, *Given-When-Then*, dados de teste com *builders*
- **Diagramas:** pirâmide anotada com as ferramentas de cada nível.
- **Código:** nenhum (é uma página conceitual com a estrutura de pastas de teste).
- **Referências:** https://docs.spring.io/spring-boot/reference/testing/ · https://martinfowler.com/articles/practical-test-pyramid.html

#### 11.02 · Testes unitários de domínio e serviços
`Básico` · `Essencial`
- **Por que importa:** regras de negócio (máquina de estados do pedido, cálculo de total) devem ser testadas rápido e sem Spring.
- **Pré-requisitos:** 11.01
- **Tópicos:**
  - JUnit 6 (Jupiter): `@Test`, `@ParameterizedTest`, `@Nested`
  - AssertJ (*assertions* fluentes e de exceções)
  - Mockito 5: *mocks* × *stubs* × *fakes*; quando não "mockar" (*value objects*, repositório *fake*)
  - Testar a máquina de estados com tabela de transições
- **Diagramas:** nenhum.
- **Código:** `PedidoTest` (transições válidas e inválidas, parametrizado); `PedidoServiceTest` com *mock* de repositório.
- **Referências:** https://docs.junit.org/ · https://assertj.github.io/doc/ · https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html

#### 11.03 · Testando a camada web: `@WebMvcTest`, `MockMvcTester` e `RestTestClient`
`Intermediário` · `Essencial`
- **Por que importa:** verifica o contrato HTTP (status, headers, JSON, erros) em milissegundos, sem banco.
- **Pré-requisitos:** 11.02, 08.02
- **Tópicos:**
  - `@WebMvcTest(ProdutoController.class)`: o que carrega
  - `@MockitoBean`
  - **`MockMvcTester`** (AssertJ, Framework 6.2+): `assertThat(mvc.get().uri(...)).hasStatus(...).bodyJson()...`
  - **`RestTestClient`** (Framework 7 / Boot 4): uma API para MockMvc ou servidor real
  - Testar ProblemDetail, validação, `Location`, `ETag`
  - `@JsonTest` para serialização
  - MockMvc clássico (reconhecer em código legado)
- **Diagramas:** o que o *slice* carrega × o que fica de fora.
- **Código:** `ProdutoControllerTest` com `MockMvcTester` (201 + `Location`; 400 com `erros[]`); o mesmo teste com `RestTestClient`.
- **Referências:** https://docs.spring.io/spring-framework/reference/testing/mockmvc/assertj.html · https://docs.spring.io/spring-framework/reference/testing/resttestclient.html · https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html

#### 11.04 · Testes de integração com `@SpringBootTest` e Testcontainers
`Intermediário` · `Essencial`
- **Por que importa:** H2 não é PostgreSQL. Testes com o banco real pegam bugs de SQL, migrations e transações.
- **Pré-requisitos:** 11.03, 10.02
- **Tópicos:**
  - `@SpringBootTest(webEnvironment = RANDOM_PORT)`
  - Testcontainers 2.0: `@Testcontainers`, `@Container`, **`@ServiceConnection`**
  - Configuração compartilhada (`@TestConfiguration` com *beans* de contêiner)
  - Contêineres no desenvolvimento (`SpringApplication.from(...).with(...)`)
  - Reutilização de contêineres; limpeza de dados entre testes
  - `@Transactional` em teste e por que pode enganar
  - Testar migrations
- **Diagramas:** ciclo de vida do contêiner × contexto Spring × testes.
- **Código:** `ProdutoIntegracaoTest` com Postgres; `TestBazarApplication` para subir a app local com contêineres.
- **Referências:** https://java.testcontainers.org/ · https://docs.spring.io/spring-boot/reference/testing/testcontainers.html

#### 11.05 · Testes ponta a ponta com RestAssured
`Intermediário` · `Importante`
- **Por que importa:** valida fluxos completos (criar produto → criar pedido → pagar) pela perspectiva de um cliente real, via HTTP.
- **Pré-requisitos:** 11.04
- **Tópicos:**
  - RestAssured 6: `given/when/then`, *specs* reutilizáveis, extração de valores, validação contra JSON Schema
  - RestAssured × `RestTestClient` (quando usar cada um)
  - Organização de cenários de fluxo; dados isolados por teste
- **Diagramas:** fluxo do cenário "compra completa".
- **Código:** `FluxoCompraE2ETest` com RestAssured.
- **Referências:** https://rest-assured.io/ · https://github.com/rest-assured/rest-assured/wiki/Usage

---

# Parte 6: Design de APIs profissional

## M12: Recursos e URIs

#### 12.01 · Modelando recursos a partir do domínio
`Intermediário` · `Essencial`
- **Por que importa:** o modelo de recursos é a decisão mais cara de mudar depois. Um modelo ruim gera dezenas de endpoints RPC disfarçados.
- **Pré-requisitos:** 03.02, 10.01
- **Tópicos:**
  - De casos de uso a recursos
  - Coleções, itens, *singletons* (`/estoque`), recursos de processo
  - Granularidade (*chatty* × *chunky*)
  - Agregados do DDD como guia de fronteira
  - Não espelhar tabelas
  - Nomes de recursos estáveis (Google AIP-122)
  - Exercício: modelar o Bazar
- **Diagramas:** mapa de recursos do Bazar (árvore de URIs).
- **Código:** tabela de recursos e URIs (sem Java).
- **Referências:** https://google.aip.dev/121 · https://google.aip.dev/122 · https://opensource.zalando.com/restful-api-guidelines/#urls

#### 12.02 · Convenções de nomenclatura
`Básico` · `Essencial`
- **Por que importa:** consistência é o que faz uma API parecer "profissional". Clientes aprendem um padrão e o aplicam em toda a API.
- **Pré-requisitos:** 12.01
- **Tópicos:**
  - Substantivos no plural; kebab-case nos caminhos; camelCase no JSON (Bazar)
  - Sem verbos no caminho (exceto ações, em 12.04)
  - Sem extensão (`.json`), sem barra final
  - Parâmetros de *query* em camelCase
  - Nomes de campos: booleanos, datas (`criadoEm`), quantidades
  - Comparação das escolhas de Microsoft, Google e Zalando (tabela)
  - Português × inglês no contrato: decisão consciente
- **Diagramas:** tabela comparativa dos três guias.
- **Código:** exemplos certo/errado.
- **Referências:** https://opensource.zalando.com/restful-api-guidelines/#urls · https://google.aip.dev/140 · https://github.com/microsoft/api-guidelines

#### 12.03 · Relacionamentos e sub-recursos
`Intermediário` · `Essencial`
- **Por que importa:** decide entre aninhar URIs, usar links ou embutir dados. Isso impacta *cache*, permissões e o número de chamadas do cliente.
- **Pré-requisitos:** 12.01
- **Tópicos:**
  - Sub-recursos de composição (`/clientes/{id}/enderecos`) × referência (`categoriaId` no produto)
  - Profundidade máxima (um nível)
  - Coleções filtradas × aninhamento (`/pedidos?clienteId=` × `/clientes/{id}/pedidos`)
  - Embutir × referenciar (*embed/expand*, ligação com 13.05)
  - Muitos-para-muitos
  - Implementação: `@RequestMapping("/v1/clientes/{clienteId}/enderecos")` e verificação de pertencimento (prévia de BOLA, 26.03)
- **Diagramas:** grafo de relacionamentos do Bazar e as opções de URI.
- **Código:** `EnderecoController`; validação de que o endereço pertence ao cliente.
- **Referências:** https://opensource.zalando.com/restful-api-guidelines/#143 (regra "MUST identify resources and sub-resources via path segments") · https://google.aip.dev/124

#### 12.04 · Ações que não são CRUD e máquinas de estado
`Intermediário` · `Essencial`
- **Por que importa:** "cancelar pedido" não é um PATCH de `status`. Modelar ações corretamente protege as regras de negócio e deixa o contrato claro.
- **Pré-requisitos:** 12.03, 11.02
- **Tópicos:**
  - Opções:
    - PATCH do estado (e por que evitar)
    - sub-recurso de ação (`POST /pedidos/{id}/cancelamento`)
    - recurso de processo (`POST /reembolsos`)
    - método customizado no estilo Google (`POST /pedidos/{id}:cancelar`, AIP-136)
  - Máquina de estados no domínio; 409 em transição inválida com ProblemDetail
  - Idempotência de ações (prévia de 21.01)
  - Ações em lote (ligação com 14.04)
  - *Opcional*: links condicionais ao estado com Spring HATEOAS 3.1 (`EntityModel`, `RepresentationModelAssembler`, `linkTo(methodOn(...))`), aplicando 03.04
- **Diagramas:** máquina de estados do Pedido; sequência do cancelamento.
- **Código:** `Pedido.cancelar()` com validação de estado; `PedidoAcoesController`; teste 409; `PedidoModelAssembler` (opcional, HATEOAS).
- **Referências:** https://google.aip.dev/136 · https://opensource.zalando.com/restful-api-guidelines/#138 (regra "MUST avoid actions — think about resources") · Microsoft Azure API Guidelines (actions)

## M13: Consultando coleções

#### 13.01 · Filtros e busca
`Intermediário` · `Essencial`
- **Por que importa:** filtros mal projetados viram dezenas de endpoints ou *queries* lentas e inseguras.
- **Pré-requisitos:** 12.03, 10.05
- **Tópicos:**
  - Filtros simples por *query param* (`status=ATIVO&categoria=livros`); listas (`status=ATIVO,RASCUNHO`)
  - Intervalos (`precoMin`/`precoMax`, `criadoApos`)
  - Busca textual (`q`)
  - Linguagens de filtro: RSQL/FIQL e o filtro do Google (AIP-160), com prós e contras
  - Buscas complexas:
    - `POST /produtos/buscas` (recurso de busca)
    - **método QUERY (RFC 10008)** e o suporte mínimo no Spring 7.1
  - *Whitelist* de campos filtráveis
  - Implementação com `Specification`
  - Índices (prévia de 28.04)
- **Diagramas:** árvore de decisão "*query param* × POST de busca × QUERY".
- **Código:** `FiltroProduto` (record com `@ModelAttribute`); `ProdutoSpecifications`; `POST /produtos/buscas`.
- **Referências:** https://google.aip.dev/160 · https://github.com/jirutka/rsql-parser · RFC 10008 · Spring Framework 7.1 Release Notes

#### 13.02 · Ordenação
`Básico` · `Importante`
- **Por que importa:** ordenação sem critério de desempate gera páginas inconsistentes. Ordenação livre abre risco de desempenho.
- **Pré-requisitos:** 13.01
- **Tópicos:**
  - Sintaxes (`sort=preco,desc` do Spring × `sort=-preco` de outros guias)
  - Múltiplos campos; desempate obrigatório pelo id
  - *Whitelist*; mapear o nome da API para o nome da coluna
  - `Sort` e `Pageable` do Spring Data; limites
- **Diagramas:** nenhum.
- **Código:** conversor de `sort` com *whitelist*; teste de ordenação estável.
- **Referências:** Spring Data Commons Reference (Paging and Sorting) · https://google.aip.dev/132#ordering

#### 13.03 · Paginação por *offset*
`Básico` · `Essencial`
- **Por que importa:** é o modelo mais comum e o padrão do Spring, mas tem custos (`COUNT`, páginas instáveis) que um sênior precisa conhecer.
- **Pré-requisitos:** 13.02
- **Tópicos:**
  - `page`/`size` × `offset`/`limit`; `Page` × `Slice`
  - O custo do `COUNT(*)`
  - Tamanho máximo de página (`spring.data.web.pageable.max-page-size`)
  - **Serialização estável de `Page`**: `PagedModel` e `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` ou `spring.data.web.pageable.serialization-mode=via-dto`; o padrão do Boot 4.1 continua `direct`, então é preciso ativar
  - Envelope de resposta × header `Link` (RFC 8288)
  - Instabilidade com inserções concorrentes; *offset* alto = *query* lenta
- **Diagramas:** problema do *offset* com inserção concorrente (item duplicado ou pulado).
- **Código:** `GET /v1/categorias` paginado; resposta com metadados e `Link`.
- **Referências:** Spring Data Commons Reference (Web support) · RFC 8288 · https://opensource.zalando.com/restful-api-guidelines/#pagination

#### 13.04 · Paginação por cursor (*keyset*)
`Intermediário` · `Essencial`
- **Por que importa:** é a única paginação que escala para coleções grandes e com escrita constante (pedidos, eventos). APIs como Stripe, Slack e Google usam.
- **Pré-requisitos:** 13.03
- **Tópicos:**
  - Como funciona o *keyset* (`WHERE (criado_em, id) < (:c, :i)`)
  - Cursor opaco (Base64 de JSON ou assinado) e por que não expor valores crus
  - `nextCursor`/`prevCursor`, `limit`
  - Spring Data: `Window`, `ScrollPosition`, `KeysetScrollPosition`
  - Índice composto obrigatório
  - Limitações: não pula para a "página 50"; ordenação dinâmica
  - Google AIP-158 (`page_token`)
- **Diagramas:** comparação visual offset × keyset no índice B-tree; formato do cursor.
- **Código:** `GET /v1/pedidos?limit=20&cursor=...` com `Window`; codificador de cursor; índice na migration.
- **Referências:** https://google.aip.dev/158 · Spring Data Commons Reference (Scrolling) · https://use-the-index-luke.com/no-offset

#### 13.05 · Respostas parciais e expansão
`Avançado` · `Complementar`
- **Por que importa:** reduz o tráfego para clientes móveis e evita chamadas extras, mas aumenta a complexidade e prejudica o *cache*.
- **Pré-requisitos:** 13.04, 06.03
- **Tópicos:**
  - *Sparse fieldsets* (`fields=id,nome`)
  - *Field masks* (Google AIP-161)
  - `expand=categoria` / `embed`
  - Implementação: filtro do Jackson, `@JsonView`, projeção dinâmica
  - Custo para *cache* e documentação OpenAPI
  - Quando GraphQL é a resposta melhor
- **Diagramas:** nenhum.
- **Código:** `fields` implementado com filtro do Jackson 3; `expand=categoria`.
- **Referências:** https://google.aip.dev/161 · https://opensource.zalando.com/restful-api-guidelines/#157 (regra "SHOULD support partial responses via filtering")

## M14: Escrita e modificação

#### 14.01 · Criação com POST (e PUT com id do cliente)
`Básico` · `Essencial`
- **Por que importa:** os detalhes da criação (status, `Location`, corpo de retorno, ids) afetam toda a experiência do cliente.
- **Pré-requisitos:** 12.02, 07.01
- **Tópicos:**
  - 201 + `Location` + representação × `Prefer: return=minimal`
  - Campos somente leitura (id, datas, status) e o que fazer quando o cliente os envia
  - Criação com id gerado pelo cliente via `PUT /recurso/{id}` (*upsert* idempotente)
  - 409 em duplicidade (SKU repetido)
  - Valores padrão
- **Diagramas:** sequência POST → 201 → GET em `Location`.
- **Código:** `POST /v1/produtos` com `Prefer` tratado; 409 em SKU duplicado (com *constraint* do banco traduzida para ProblemDetail).
- **Referências:** RFC 9110 §9.3.3 · RFC 7240 · https://google.aip.dev/133

#### 14.02 · PUT × PATCH: JSON Merge Patch e JSON Patch
`Intermediário` · `Essencial`
- **Por que importa:** atualização parcial mal feita apaga dados (o `null` acidental) ou não consegue limpar campos. É uma das partes mais sutis do design de APIs.
- **Pré-requisitos:** 14.01, 02.06
- **Tópicos:**
  - Semântica do PUT (substituição completa) e do PATCH (RFC 5789)
  - **JSON Merge Patch (RFC 7396)**: `application/merge-patch+json`, `null` remove; limitações com arrays
  - **JSON Patch (RFC 6902)**: operações (`add`, `remove`, `replace`, `move`, `copy`, `test`) e atomicidade
  - Como diferenciar "ausente" de `null` em Java: `JsonNode`, `Optional` em campos, `JsonNullable` (`org.openapitools:jackson-databind-nullable` 0.2.12, que tem `JsonNullableJackson3Module`)
  - Validar depois de aplicar o *patch*
  - PATCH não é idempotente por definição (JSON Patch com `add` em array)
  - Bibliotecas: `json-patch` (java-json-tools, última versão 2020) e `zjsonpatch` só suportam Jackson 2. Para Jackson 3, o Bazar implementa o Merge Patch sobre `JsonNode` e usa a **Jakarta JSON Processing 2.1** (`jakarta.json-api` 2.1.3 + Eclipse Parsson 1.1.9), que traz `JsonPatch` e `JsonMergePatch` nativos
- **Diagramas:** o mesmo documento após PUT, Merge Patch e JSON Patch (antes/depois).
- **Código:** `PATCH /v1/produtos/{id}` com Merge Patch (`JsonNode` → aplicar → validar → salvar); exemplo de JSON Patch com `test` para concorrência.
- **Referências:** https://www.rfc-editor.org/rfc/rfc7396 · https://www.rfc-editor.org/rfc/rfc6902 · https://www.rfc-editor.org/rfc/rfc5789 · https://google.aip.dev/134

#### 14.03 · DELETE, *soft delete* e arquivamento
`Básico` · `Importante`
- **Por que importa:** apagar de verdade dados com histórico (produto já vendido) quebra relatórios e auditoria. Decidir a semântica do DELETE é parte do contrato.
- **Pré-requisitos:** 14.01
- **Tópicos:**
  - 204 × 200 com corpo; DELETE repetido (404 ou 204?)
  - 409 quando há dependentes; 410 Gone
  - *Soft delete* (status `DESCONTINUADO`) × exclusão física × arquivamento; o filtro padrão nas listagens (`@SQLRestriction` do Hibernate)
  - *Undelete*
  - LGPD: direito de exclusão × retenção (menção)
- **Diagramas:** nenhum.
- **Código:** `DELETE /v1/produtos/{id}` como *soft delete*; `DELETE /v1/categorias/{id}` com 409.
- **Referências:** RFC 9110 §9.3.5 · https://google.aip.dev/135 · https://google.aip.dev/164

#### 14.04 · Operações em lote
`Avançado` · `Importante`
- **Por que importa:** integrações (por exemplo, o ERP de um lojista) precisam atualizar centenas de itens. Fazer N chamadas é lento; um lote mal projetado gera falhas parciais confusas.
- **Pré-requisitos:** 14.02, 10.03
- **Tópicos:**
  - Lote atômico (tudo ou nada) × parcial (resultado por item)
  - Formato do resultado por item (status e ProblemDetail por item)
  - 200 com resultados × 207 Multi-Status (WebDAV, uso controverso)
  - Limites de tamanho; lote assíncrono (ligação com 21.02)
  - Idempotência do lote
  - Google AIP-231/233 (*batch get/create*)
- **Diagramas:** fluxo de um lote parcial com itens OK e itens com erro.
- **Código:** `POST /v1/ajustes-estoque/lotes` com até 500 itens e resultado por item.
- **Referências:** https://google.aip.dev/233 · https://opensource.zalando.com/restful-api-guidelines/#152 (regra "MUST use code 207 for batch or bulk requests"; note que o Zalando torna o 207 obrigatório) · RFC 4918 §13 (207)

## M15: Tipos de dados no contrato

#### 15.01 · Datas, horas e fusos horários
`Intermediário` · `Essencial`
- **Por que importa:** bugs de fuso aparecem na virada do dia, no horário de verão e com clientes em outros países, e quase sempre já em produção.
- **Pré-requisitos:** 06.03
- **Tópicos:**
  - ISO 8601 × RFC 3339 (o perfil que APIs devem usar)
  - Instante (`Instant`, sempre UTC com `Z`) × data local (`LocalDate`) × data-hora com *offset* (`OffsetDateTime`) × `ZonedDateTime` (com regra de fuso)
  - O que guardar no banco (`timestamptz`)
  - `Clock` injetável para testes
  - Durações (ISO 8601 `PT30M`)
  - Filtros por período: intervalo semiaberto `[início, fim)`
  - O Brasil e o fim do horário de verão
- **Diagramas:** linha do tempo mostrando o mesmo instante em UTC, em `America/Sao_Paulo` e em `Asia/Tokyo`.
- **Código:** `Pedido.criadoEm` como `Instant`; configuração do Jackson; teste com `Clock.fixed`.
- **Referências:** https://www.rfc-editor.org/rfc/rfc3339 · https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/package-summary.html · https://opensource.zalando.com/restful-api-guidelines/#169 (regra "MUST use standard formats for date and time properties")

#### 15.02 · Dinheiro e números decimais
`Intermediário` · `Essencial`
- **Por que importa:** `double` para dinheiro gera centavos errados, e um erro de moeda é um incidente financeiro.
- **Pré-requisitos:** 15.01
- **Tópicos:**
  - Por que não usar ponto flutuante
  - Duas representações: unidades menores (inteiro em centavos, a v1 do Bazar) × decimal em string + moeda ISO 4217 (a v2)
  - `BigDecimal` (escala, `RoundingMode`, `compareTo` × `equals`)
  - *Value object* `Dinheiro`
  - JSR 354 / Moneta (menção)
  - Arredondamento no rateio de frete e desconto
- **Diagramas:** nenhum (tabela comparativa).
- **Código:** record `Dinheiro` com validação; serializador e desserializador Jackson; rateio com resto.
- **Referências:** https://www.iso.org/iso-4217-currency-codes.html · https://opensource.zalando.com/restful-api-guidelines/#173 (regra "MUST use the common money object") · `google.type.Money` https://github.com/googleapis/googleapis/blob/master/google/type/money.proto · https://google.aip.dev/143 (códigos padronizados, inclusive moeda)

#### 15.03 · Identificadores: sequenciais, UUID v7 e ULID
`Intermediário` · `Importante`
- **Por que importa:** ids sequenciais expostos permitem enumeração e vazam volume de negócio. UUID v4 aleatório degrada índices. O UUID v7 resolve os dois problemas.
- **Pré-requisitos:** 10.01
- **Tópicos:**
  - Sequencial × UUID v4 × **UUID v7 (RFC 9562)** × ULID
  - Ordenação temporal e a localidade no índice B-tree
  - Id interno × id público; ids legíveis para humanos (`BZR-2026-000123`)
  - Geração no Java: o JDK 25 não gera v7 (`UUID.ofEpochMillis` só existe a partir do JDK 26); opções:
    - Hibernate 7 `@UuidGenerator(style = UuidGenerator.Style.VERSION_7)` (confirmado no Javadoc da 7.4)
    - biblioteca `uuid-creator` 6.1.1
    - nota para quem usa JDK 26+: `UUID.ofEpochMillis(long)`
  - Tipo `uuid` no PostgreSQL
  - Ids como string no JSON
- **Diagramas:** layout de bits do UUID v7; inserções em índice (v4 espalhado × v7 sequencial).
- **Código:** entidade `Produto` com UUID v7; gerador de `numero` de pedido.
- **Referências:** https://www.rfc-editor.org/rfc/rfc9562 · https://github.com/ulid/spec · Hibernate ORM User Guide (identifiers)

#### 15.04 · Enums, evolução e *tolerant reader*
`Intermediário` · `Importante`
- **Por que importa:** adicionar um valor de enum é uma mudança que quebra clientes que não estão preparados. Projetar para evolução evita incidentes.
- **Pré-requisitos:** 15.01, 06.03
- **Tópicos:**
  - Enums em JSON: strings em UPPER_SNAKE_CASE
  - Enums "extensíveis" (`x-extensible-enum` do Zalando) e valor desconhecido no cliente
  - Padrão *tolerant reader* (ignorar campos desconhecidos; tratar valor desconhecido)
  - `@JsonEnumDefaultValue`
  - Lei de Postel e suas críticas
  - Booleanos que deveriam ser enums
- **Diagramas:** cliente antigo recebendo o novo status `EM_SEPARACAO`.
- **Código:** `StatusPedido` com valor desconhecido no cliente; configuração Jackson do lado consumidor.
- **Referências:** https://martinfowler.com/bliki/TolerantReader.html · https://opensource.zalando.com/restful-api-guidelines/#112 (regra "SHOULD use open-ended list of values for enumeration types")

#### 15.05 · Internacionalização na API
`Intermediário` · `Complementar`
- **Por que importa:** mensagens de erro e conteúdos traduzidos dependem de negociar o idioma corretamente.
- **Pré-requisitos:** 08.02, 02.05
- **Tópicos:**
  - `Accept-Language` e `Content-Language`; `LocaleResolver` (`AcceptHeaderLocaleResolver`)
  - Mensagens de erro e de validação traduzidas
  - Conteúdo multilíngue (nome do produto por idioma)
  - Formatar ou não números e datas na API (não formatar)
- **Diagramas:** nenhum.
- **Código:** ProblemDetail em pt-BR e en-US conforme o header.
- **Referências:** RFC 9110 §12.5.4 · https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-servlet/localeresolver.html

## M16: Consistência e guia de estilo

#### 16.01 · Projetando erros: catálogo, códigos e decisões difíceis
`Intermediário` · `Essencial`
- **Por que importa:** além do formato (08.01), um sênior define *quais* erros existem, quando usar cada status e como o cliente deve reagir.
- **Pré-requisitos:** 08.02, 12.04
- **Tópicos:**
  - Catálogo de problemas do Bazar (`type`, `codigo`, status, ação sugerida ao cliente)
  - 400 × 422 × 409: um critério objetivo
  - Erros de validação com ponteiro de campo (JSON Pointer)
  - Erros retentáveis × definitivos
  - Não vazar detalhes internos (SQL, *stack*, nomes de classes)
  - Documentar os tipos (página HTML no `type` URI)
  - Evolução: adicionar `codigo` novo é compatível?
- **Diagramas:** fluxograma 400/409/422/412/428.
- **Código:** `enum TipoProblema` com URI, título e status; *factory* de ProblemDetail; exemplo do catálogo publicado.
- **Referências:** RFC 9457 §3-4 · https://google.aip.dev/193 · https://opensource.zalando.com/restful-api-guidelines/#176 (regra "MUST support problem JSON") · RFC 6901 (JSON Pointer)

#### 16.02 · Escrevendo o guia de estilo da sua API
`Avançado` · `Importante`
- **Por que importa:** em empresas com várias equipes, o guia de estilo é o que mantém dezenas de APIs coerentes. Ele vira regras automáticas de *lint* em 17.04.
- **Pré-requisitos:** 16.01, 15.04, 13.04, 14.02
- **Tópicos:**
  - Estrutura de um guia (MUST/SHOULD/MAY, RFC 2119)
  - Consolidação das decisões do Bazar (nomes, paginação, erros, datas, dinheiro, versionamento)
  - Como os guias da Microsoft, do Google (AIPs) e do Zalando se organizam e onde divergem
  - Processo de revisão de API (*API review*); registro de decisões (ADR)
- **Diagramas:** do guia às regras de *lint* e à revisão no PR.
- **Código:** trecho do `GUIA-DE-ESTILO.md` do Bazar.
- **Referências:** https://github.com/microsoft/api-guidelines · https://google.aip.dev/ · https://opensource.zalando.com/restful-api-guidelines/ · https://www.rfc-editor.org/rfc/rfc2119 · https://adr.github.io/

---

# Parte 7: Contratos, documentação e ciclo de vida

## M17: OpenAPI e contrato

#### 17.01 · A especificação OpenAPI
`Intermediário` · `Essencial`
- **Por que importa:** o contrato OpenAPI é a base para documentação, geração de clientes, *mocks*, testes e governança. Precisa ser lido e escrito com fluência.
- **Pré-requisitos:** 16.01
- **Tópicos:**
  - Estrutura: `info`, `servers`, `paths`, `components` (`schemas`, `parameters`, `responses`, `securitySchemes`), `$ref`
  - OAS 3.0 × 3.1 (alinhamento total com JSON Schema 2020-12, `type: [string, "null"]`) × 3.2 (mais recente, 3.2.1); por que o Bazar usa 3.1
  - *Webhooks* no OAS 3.1
  - Exemplos (`examples`)
  - Projetos irmãos: Arazzo (fluxos) e Overlay (transformações), só a menção
- **Diagramas:** mapa da estrutura de um documento OpenAPI.
- **Código:** trecho de `bazar-api.yaml` (produto, paginação, ProblemDetail reutilizável).
- **Referências:** https://spec.openapis.org/oas/latest.html · https://spec.openapis.org/oas/v3.1.2.html · https://json-schema.org/draft/2020-12 · https://spec.openapis.org/arazzo/latest.html

#### 17.02 · springdoc-openapi: documentação a partir do código
`Básico` · `Essencial`
- **Por que importa:** é o caminho mais rápido para ter um contrato e uma UI de testes. Ainda assim, o contrato gerado precisa de cuidado para ser útil.
- **Pré-requisitos:** 17.01, 08.02
- **Tópicos:**
  - springdoc 3.2 com Boot 4.1
  - `/v3/api-docs`, Swagger UI (Scalar como alternativa)
  - Anotações `@Operation`, `@ApiResponse`, `@Schema`, `@Parameter`; exemplos
  - Documentar ProblemDetail globalmente; grupos (`GroupedOpenApi`) por versão
  - Paginação e `Pageable`
  - Esconder em produção ou proteger
  - Exportar o contrato no *build* (plugin Maven) para versionar
  - Javadoc como fonte: suportado pelo springdoc com `therapi-runtime-javadoc` + `therapi-runtime-javadoc-scribe` 0.15.0 (biblioteca sem *release* desde 2022: Complementar)
  - Versão do OpenAPI gerada: 3.1 por padrão (`springdoc.api-docs.version=openapi_3_1`); a 3.2 não é documentada pelo springdoc
- **Diagramas:** nenhum.
- **Código:** `ProdutoController` anotado; `OpenApiConfig`; UI com exemplos.
- **Referências:** https://springdoc.org/ · https://github.com/springdoc/springdoc-openapi

#### 17.03 · API-first: o contrato como fonte da verdade
`Avançado` · `Importante`
- **Por que importa:** projetar o contrato antes do código permite revisão, *mocks* para o front-end e trabalho em paralelo, além de evitar que detalhes de implementação vazem para a API.
- **Pré-requisitos:** 17.02
- **Tópicos:**
  - *Code-first* × *design-first*: prós e contras
  - Fluxo: escrever o YAML → revisar → *mock* (Prism) → gerar as interfaces → implementar
  - openapi-generator (gerador `spring`: `interfaceOnly`, `useSpringBoot4=true` e `useJackson3=true` (openapi-generator 7.26.0), `useJakartaEe`, `delegatePattern`)
  - O que gerar e o que não gerar
  - Contrato no repositório e versionado
  - Validar a implementação contra o contrato em teste (Atlassian `openapi-request-validator-mockmvc`/`-restassured` 3.0.0, antigo `swagger-request-validator`, compatível com Spring 7+)
- **Diagramas:** fluxo *design-first* com revisão e *mock*.
- **Código:** `bazar-api.yaml` → configuração do `openapi-generator-maven-plugin` → `ProdutosApi` gerada → `ProdutoController implements ProdutosApi`.
- **Referências:** https://openapi-generator.tech/docs/generators/spring/ · https://stoplight.io/open-source/prism · https://swagger.io/resources/articles/adopting-an-api-first-approach/

#### 17.04 · Governança: *linting* e detecção de *breaking changes*
`Avançado` · `Importante`
- **Por que importa:** automatizar o guia de estilo e bloquear mudanças incompatíveis no CI é o que torna a consistência sustentável.
- **Pré-requisitos:** 17.03, 16.02
- **Tópicos:**
  - Spectral: *rulesets* (`spectral:oas`), regras customizadas a partir do guia do Bazar (kebab-case, ProblemDetail obrigatório, paginação)
  - Rulesets prontos: Zally (linter do Zalando) tem repositório ativo, mas a última *release* é de 2022; o Bazar usa Spectral 6.17 com regras próprias inspiradas no guia do Zalando
  - `oasdiff` para *breaking changes* entre versões e o *changelog* gerado
  - Integração no pipeline (ligação com 29.06)
  - Catálogo de APIs (Backstage, só a menção)
- **Diagramas:** pipeline: PR → lint → diff → revisão.
- **Código:** `.spectral.yaml` do Bazar; saída do `oasdiff breaking` detectando a remoção de um campo.
- **Referências:** https://docs.stoplight.io/docs/spectral/ · https://github.com/oasdiff/oasdiff · https://github.com/zalando/zally

#### 17.05 · Documentação além da referência
`Intermediário` · `Complementar`
- **Por que importa:** a referência OpenAPI diz *o que* existe; guias dizem *como* usar. A falta de guias é a principal queixa de quem integra APIs.
- **Pré-requisitos:** 17.02
- **Tópicos:**
  - Tipos de documentação (Diátaxis: tutorial, guia, referência, explicação)
  - *Getting started*, autenticação, erros, paginação, *rate limits*, *changelog*
  - Spring REST Docs 4 (documentação gerada de testes) × springdoc
  - Coleções Bruno/Postman; SDKs gerados
  - Portal do desenvolvedor
- **Diagramas:** nenhum.
- **Código:** teste com Spring REST Docs gerando um *snippet*.
- **Referências:** https://diataxis.fr/ · https://docs.spring.io/spring-restdocs/docs/current/reference/htmlsingle/

## M18: Evolução e versionamento

#### 18.01 · Compatibilidade retroativa: o que quebra um cliente
`Intermediário` · `Essencial`
- **Por que importa:** versionar é caro. Quem sabe evoluir sem quebrar raramente precisa de uma v2.
- **Pré-requisitos:** 17.01, 15.04
- **Tópicos:**
  - Catálogo de mudanças compatíveis × incompatíveis:
    - campo novo
    - campo obrigatório novo na entrada
    - remover ou renomear campo
    - mudar tipo
    - novo valor de enum
    - novo status code
    - mudar valor padrão
    - mudar ordenação padrão
    - validação mais restrita
  - Lei de Hyrum
  - *Tolerant reader* (revisão)
  - Expand-contract no contrato (campo novo + antigo em paralelo)
  - Google AIP-180
- **Diagramas:** tabela de mudanças com semáforo (verde, amarelo, vermelho).
- **Código:** exemplos de evolução do `ProdutoResponse`.
- **Referências:** https://google.aip.dev/180 · https://www.hyrumslaw.com/ · https://opensource.zalando.com/restful-api-guidelines/#compatibility

#### 18.02 · Estratégias de versionamento
`Intermediário` · `Essencial`
- **Por que importa:** quando uma mudança incompatível é inevitável, a estratégia de versão define custo de operação, *cache* e experiência do cliente.
- **Pré-requisitos:** 18.01, 02.05
- **Tópicos:**
  - Caminho (`/v1`), header (`API-Version`), *query param*, media type (`application/vnd.bazar.v2+json`)
  - Versionamento por data (estilo Stripe)
  - "Sem versão" (evolução contínua, Zalando)
  - Versão *major* só × semântica
  - Granularidade (API inteira × recurso)
  - Quantas versões manter
  - Decisão do Bazar (caminho, só *major*) e o porquê
- **Diagramas:** quadro comparativo; impacto no *cache* e no roteamento do gateway.
- **Código:** a mesma requisição em cada estratégia (`curl`).
- **Referências:** https://stripe.com/blog/api-versioning · https://github.com/microsoft/api-guidelines/blob/vNext/azure/Guidelines.md (versioning) · https://opensource.zalando.com/restful-api-guidelines/#113 ("SHOULD avoid versioning") · #114 ("MUST use media type versioning")

#### 18.03 · Versionamento nativo no Spring Framework 7
`Intermediário` · `Essencial`
- **Por que importa:** desde o Spring 7 / Boot 4 o versionamento é suportado pelo framework, sem *hacks* de prefixo ou controllers duplicados.
- **Pré-requisitos:** 18.02, 09.01
- **Tópicos:**
  - `ApiVersionConfigurer` (`WebMvcConfigurer#configureApiVersioning`)
  - Resolvers: header, *query param*, segmento de caminho, parâmetro de media type, customizado
  - Propriedades: `spring.mvc.apiversion.use.header`, `.use.query-parameter`, `.use.path-segment`, `.use.media-type-parameter.*`, `.default`, `.required`, `.supported`, `.detect-supported`
  - `@GetMapping(version = "1.1")` (fixa), `version = "1.2+"` (de base: essa versão e as suportadas acima) e sem `version` (qualquer versão, menor prioridade)
  - Versão por caminho exige variável na URI (`/{version}/produtos`) ou prefixo comum configurado em *path matching*
  - `SemanticApiVersionParser`
  - Versão obrigatória × padrão; 400 em versão ausente ou inválida (`MissingApiVersionException`, `InvalidApiVersionException`)
  - Versões suportadas declaradas × inferidas
  - Versionamento no cliente (`RestClient`, HTTP interfaces) e nos testes (MockMvc / `RestTestClient`)
  - Compartilhar código entre versões (serviço único, *mappers* por versão)
- **Diagramas:** resolução da versão → seleção do handler.
- **Código:** `ProdutoV2Controller` com `Dinheiro`; configuração de versão por caminho; testes das duas versões.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc-versioning.html · https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-config/api-version.html · https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-requestmapping.html (seção API Version) · Spring Boot 4.0 Release Notes

#### 18.04 · Depreciação e desligamento de APIs
`Avançado` · `Importante`
- **Por que importa:** desligar uma versão sem processo derruba clientes. Os headers padronizados permitem que ferramentas avisem automaticamente.
- **Pré-requisitos:** 18.03
- **Tópicos:**
  - Header `Deprecation` (RFC 9745, formato de data em *structured field*)
  - Header `Sunset` (RFC 8594)
  - `Link` com `rel="deprecation"` e `rel="sunset"`
  - `StandardApiVersionDeprecationHandler` do Spring 7
  - Depreciar campos e endpoints individuais (`deprecated: true` no OpenAPI)
  - Métricas de uso por versão e por cliente para saber quem ainda usa (ligação com 27.05)
  - Comunicação e prazos; 410 Gone após o *sunset*
- **Diagramas:** linha do tempo de vida de uma versão (lançamento → depreciação → *sunset* → 410).
- **Código:** configuração do handler de depreciação para a v1 do produto; resposta com os headers; métrica por versão.
- **Referências:** https://www.rfc-editor.org/rfc/rfc9745 · https://www.rfc-editor.org/rfc/rfc8594 · Spring Framework reference (API Versioning, deprecation)

#### 18.05 · Testes de contrato
`Avançado` · `Importante`
- **Por que importa:** garantem que produtor e consumidores continuam compatíveis sem precisar de ambientes integrados frágeis.
- **Pré-requisitos:** 18.01, 11.04
- **Tópicos:**
  - Contratos dirigidos pelo produtor × pelo consumidor (CDC)
  - Spring Cloud Contract 5: DSL, testes gerados, *stubs* para consumidores
  - Pact: *broker*, `can-i-deploy`
  - Validação contra o OpenAPI como alternativa leve
  - Onde cada um se encaixa no pipeline
- **Diagramas:** fluxo CDC com consumidor → *broker* → verificação no produtor.
- **Código:** contrato do `GET /v1/produtos/{id}` no Spring Cloud Contract; teste de consumidor (ERP do lojista) com Pact.
- **Referências:** https://docs.spring.io/spring-cloud-contract/reference/ · https://docs.pact.io/ · https://martinfowler.com/articles/consumerDrivenContracts.html

---

# Parte 8: Concorrência e execução assíncrona

## M19: Threads, virtual threads e tarefas em segundo plano

#### 19.01 · O modelo *thread-per-request* e seus limites
`Intermediário` · `Essencial`
- **Por que importa:** entender quantas requisições a API atende ao mesmo tempo e o que acontece quando uma dependência fica lenta é a base de desempenho e resiliência.
- **Pré-requisitos:** 09.01, 10.03
- **Tópicos:**
  - *Pool* de *threads* do Tomcat (`server.tomcat.threads.max`, `accept-count`, `max-connections`)
  - Lei de Little
  - Por que uma dependência lenta esgota o *pool*
  - I/O bloqueante × CPU
  - Recursos limitados além das *threads* (conexões do banco)
- **Diagramas:** fila de requisições, *pool* de *threads* e *pool* de conexões (gargalos em série).
- **Código:** experimento com um endpoint lento (`Thread.sleep`) e carga simples para observar a saturação.
- **Referências:** https://docs.spring.io/spring-boot/appendix/application-properties/index.html (`server.tomcat.*`) · https://tomcat.apache.org/tomcat-11.0-doc/config/http.html

#### 19.02 · Virtual threads em APIs Spring
`Intermediário` · `Essencial`
- **Por que importa:** virtual threads (Java 21+) permitem milhares de requisições bloqueantes simultâneas com código imperativo simples, mas não removem outros gargalos.
- **Pré-requisitos:** 19.01
- **Tópicos:**
  - JEP 444
  - `spring.threads.virtual.enabled=true`: o que o Boot muda (Tomcat, `@Async`, *schedulers*, clientes)
  - *Pinning*: resolvido para `synchronized` na JDK 24 (JEP 491), mas ainda há casos (código nativo)
  - `ThreadLocal` e custo de memória
  - O gargalo se move para o *pool* do banco e as APIs externas (ligação com 28.02 e 25.03)
  - Quando não ajudam: CPU-*bound*
  - Diagnóstico com JFR (`jdk.VirtualThreadPinned`)
- **Diagramas:** *carrier threads* × virtual threads (montar e desmontar em I/O).
- **Código:** habilitar no Bazar; teste de carga comparativo antes e depois (com nota de método).
- **Referências:** https://openjdk.org/jeps/444 · https://openjdk.org/jeps/491 · https://docs.spring.io/spring-boot/reference/features/spring-application.html#features.spring-application.virtual-threads

#### 19.03 · `@Async`, `@Scheduled` e tarefas em segundo plano
`Intermediário` · `Essencial`
- **Por que importa:** operações demoradas (relatórios, envio de webhooks) não podem segurar a requisição. É preciso executá-las fora dela, com segurança.
- **Pré-requisitos:** 19.02
- **Tópicos:**
  - `@EnableAsync`, `@Async`, `TaskExecutor` e a configuração `spring.task.execution.*`
  - Retorno `CompletableFuture`; tratamento de exceções (`AsyncUncaughtExceptionHandler`)
  - **Propagação de contexto** (MDC, *tracing*), automática para `@Async` no Boot 4.1
  - `@Scheduled` (*fixed delay*, cron)
  - Agendamento em múltiplas instâncias e o problema do *job* duplicado (ShedLock ou `SKIP LOCKED`)
  - Limites: perder tarefas em memória no *restart* (motivação para a tabela de *jobs* e o Outbox)
- **Diagramas:** requisição → tarefa enviada → resposta imediata; a tarefa se perde em *crash* (com e sem persistência).
- **Código:** `RelatorioVendasService.gerarAsync`; `@Scheduled` que processa a tabela `operacao` com `SELECT ... FOR UPDATE SKIP LOCKED`.
- **Referências:** https://docs.spring.io/spring-framework/reference/integration/scheduling.html · https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html · https://github.com/lukas-krecan/ShedLock

#### 19.04 · Server-Sent Events (e uma visão de WebSocket)
`Intermediário` · `Importante`
- **Por que importa:** acompanhar o status do pedido em tempo real é um requisito comum. SSE resolve com HTTP simples, sem a complexidade do WebSocket.
- **Pré-requisitos:** 19.03, 04.01
- **Tópicos:**
  - Formato `text/event-stream` (`event`, `id`, `data`, `retry`)
  - `SseEmitter` (*timeouts*, conclusão, erros)
  - `Last-Event-ID` para retomada
  - *Heartbeats*; *proxies* e *buffering* (`X-Accel-Buffering`)
  - Escala com várias instâncias (Redis pub/sub ou mensageria)
  - Limite de conexões em HTTP/1.1
  - WebSocket/STOMP em visão geral e quando preferir
- **Diagramas:** sequência SSE com reconexão e `Last-Event-ID`; topologia com várias instâncias.
- **Código:** `GET /v1/pedidos/{id}/eventos` com `SseEmitter`; cliente JS `EventSource`.
- **Referências:** https://html.spec.whatwg.org/multipage/server-sent-events.html · https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html · https://docs.spring.io/spring-framework/reference/web/websocket.html

#### 19.05 · WebFlux: visão geral e quando usar
`Avançado` · `Complementar`
- **Por que importa:** o leitor vai encontrar WebFlux em projetos e entrevistas e precisa saber quando ele realmente compensa, especialmente depois das virtual threads.
- **Pré-requisitos:** 19.02
- **Tópicos:**
  - Reactive Streams, `Mono`/`Flux`, *backpressure*
  - Netty e *event loop*
  - R2DBC
  - Custos: depuração, curva de aprendizado, bibliotecas bloqueantes
  - Virtual threads × reativo: critérios (*streaming*, *backpressure*, gateways)
  - `WebClient` em aplicações MVC (ligação com 24.04)
- **Diagramas:** *event loop* × *thread-per-request* × virtual threads.
- **Código:** `GET /v1/produtos/{id}` em WebFlux, só para comparar.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webflux.html · https://www.reactive-streams.org/

#### 19.06 · Scoped values e concorrência estruturada
`Avançado` · `Complementar`
- **Por que importa:** são as novas ferramentas do Java para propagar contexto e paralelizar chamadas com segurança, substituindo `ThreadLocal` e `CompletableFuture` encadeados.
- **Pré-requisitos:** 19.02
- **Tópicos:**
  - `ScopedValue` (final no JDK 25, JEP 506)
  - `StructuredTaskScope`: ainda em *preview* (JDK 25: JEP 505, quinta *preview*; JDK 26: JEP 525; JDK 27: JEP 533, sétima *preview*), com a API mudando entre versões
  - Caso de uso: montar o detalhe do pedido chamando frete e pagamento em paralelo com *timeout*
  - Cuidados com *preview features* em produção
- **Diagramas:** árvore de tarefas com cancelamento em cascata.
- **Código:** agregação paralela com `StructuredTaskScope` (marcada como *preview*).
- **Referências:** https://openjdk.org/jeps/506 · https://openjdk.org/jeps/505 · https://openjdk.org/jeps/533

---

# Parte 9: Padrões avançados de HTTP

## M20: Cache e concorrência no HTTP

#### 20.01 · Cache HTTP (RFC 9111)
`Intermediário` · `Essencial`
- **Por que importa:** o *cache* HTTP é a otimização mais barata que existe. Bem feito, reduz carga e latência; mal feito, mostra dados de outro usuário.
- **Pré-requisitos:** 02.04, 14.01
- **Tópicos:**
  - Caches privados × compartilhados (CDN, *proxy*)
  - `Cache-Control`: `max-age`, `s-maxage`, `no-cache` × `no-store`, `private`/`public`, `must-revalidate`, `stale-while-revalidate`, `stale-if-error`, `immutable`
  - Frescor × validação; `Vary`
  - Respostas autenticadas e *cache*
  - `CacheControl` no `ResponseEntity`
  - O que nunca cachear
- **Diagramas:** árvore de decisão de diretivas; fluxo cliente → CDN → API com *hit*, *miss* e revalidação.
- **Código:** `GET /v1/produtos/{id}` com `CacheControl.maxAge(...)`; `GET /v1/categorias` com `public, max-age`.
- **Referências:** https://www.rfc-editor.org/rfc/rfc9111 · RFC 5861 (`stale-*`) · https://developer.mozilla.org/pt-BR/docs/Web/HTTP/Caching

#### 20.02 · ETag e requisições condicionais
`Intermediário` · `Essencial`
- **Por que importa:** com `ETag`, o cliente revalida sem baixar de novo (304). Com o mesmo mecanismo se faz controle de concorrência (20.03).
- **Pré-requisitos:** 20.01
- **Tópicos:**
  - ETag forte × fraca; `Last-Modified`
  - `If-None-Match` → 304; `If-Modified-Since`
  - Gerar a ETag: hash do corpo (`ShallowEtagHeaderFilter`, que só economiza banda, não processamento) × versão da entidade (`@Version`)
  - `WebRequest.checkNotModified`
  - ETag em coleções
  - ETag e compressão (a ETag fraca após gzip)
- **Diagramas:** sequência GET → 200 + ETag → GET com `If-None-Match` → 304.
- **Código:** produto com ETag derivada de `versao`; teste do 304.
- **Referências:** RFC 9110 §8.8 e §13 · https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-caching.html

#### 20.03 · Concorrência otimista com `If-Match` (412 e 428)
`Avançado` · `Essencial`
- **Por que importa:** impede que dois usuários editando o mesmo produto ou estoque sobrescrevam as alterações um do outro (*lost update*) via HTTP, sem *locks*.
- **Pré-requisitos:** 20.02, 10.06
- **Tópicos:**
  - Fluxo GET (ETag) → PUT/PATCH com `If-Match`
  - 412 Precondition Failed; tornar a precondição obrigatória com 428 Precondition Required
  - Mapear `@Version` ↔ ETag
  - Comparação com `If-Unmodified-Since`
  - O que o cliente faz no 412 (recarregar e mesclar)
  - Concorrência otimista × idempotência (complementares)
- **Diagramas:** dois clientes editando o mesmo produto: o segundo recebe 412.
- **Código:** `PUT /v1/produtos/{id}/estoque` exigindo `If-Match`; ProblemDetail 412/428; testes.
- **Referências:** RFC 9110 §13.1.1 · RFC 6585 (428) · https://google.aip.dev/154

## M21: Confiabilidade nas escritas

#### 21.01 · Idempotency keys
`Avançado` · `Essencial`
- **Por que importa:** quando a rede falha depois que o servidor processou um POST, o cliente não sabe se o pedido foi criado. A idempotency key permite repetir com segurança, sem cobrança dupla.
- **Pré-requisitos:** 20.03, 10.03, 02.02
- **Tópicos:**
  - O problema (*timeout* após o *commit*)
  - Header `Idempotency-Key` (*Internet-Draft* da IETF, ainda não é RFC)
  - Escopo da chave (cliente + rota)
  - *Fingerprint* do corpo: chave reutilizada com corpo diferente → 422
  - Armazenar e retornar a resposta original
  - Requisições concorrentes com a mesma chave → 409
  - Expiração (TTL); onde guardar: Postgres × Redis (Redis apresentado aqui em uma seção curta; aprofundado em 28.03)
  - Atomicidade com a operação de negócio
  - Implementação como filtro/interceptor + anotação `@Idempotente`
- **Diagramas:**
  - Sequência com falha de rede e *retry* (sem chave → pedido duplicado; com chave → mesma resposta)
  - Máquina de estados da chave (`EM_PROCESSAMENTO` → `CONCLUIDA`)
- **Código:** `ChaveIdempotencia`; `IdempotenciaInterceptor`; `POST /v1/pedidos` idempotente; teste concorrente.
- **Referências:** https://datatracker.ietf.org/doc/draft-ietf-httpapi-idempotency-key-header/ · https://stripe.com/blog/idempotency · https://docs.stripe.com/api/idempotent_requests

#### 21.02 · Operações assíncronas e de longa duração (202 + *polling*)
`Avançado` · `Essencial`
- **Por que importa:** reembolsos e relatórios levam segundos ou minutos. Segurar a conexão causa *timeouts* em cascata. O padrão 202 dá ao cliente controle e visibilidade.
- **Pré-requisitos:** 19.03, 21.01
- **Tópicos:**
  - 202 Accepted + `Location` para o recurso de operação
  - Recurso `Operacao` (status, progresso, resultado, erro como ProblemDetail)
  - `Retry-After` no *polling*
  - Ao concluir: 200 com link para o resultado × 303 See Other
  - Cancelamento (`POST /operacoes/{id}/cancelamento`)
  - Expiração de resultados
  - `Prefer: respond-async`
  - Webhook ao concluir como alternativa ao *polling*
  - Padrões de LRO da Microsoft e do Google (AIP-151)
- **Diagramas:** sequência 202 → *polling* → 303 → resultado; estados da operação.
- **Código:** `POST /v1/pagamentos/{id}/reembolsos` → 202; `GET /v1/operacoes/{id}`; processador agendado.
- **Referências:** https://google.aip.dev/151 · https://github.com/microsoft/api-guidelines/blob/vNext/azure/ConsiderationsForServiceDesign.md#long-running-operations · RFC 9110 §15.3.3 (202) · RFC 7240

#### 21.03 · Webhooks: enviando e recebendo
`Avançado` · `Essencial`
- **Por que importa:** integrações modernas são orientadas a eventos. Webhooks mal feitos perdem eventos, duplicam ações ou viram vetor de ataque.
- **Pré-requisitos:** 21.02, 19.03
- **Tópicos:**
  - **Enviando:**
    - registro de assinaturas e validação de URL (SSRF, 26.05)
    - formato do evento (CloudEvents)
    - assinatura HMAC (Standard Webhooks: `webhook-id`, `webhook-timestamp`, `webhook-signature`)
    - entrega pelo menos uma vez (*at-least-once*)
    - *retries* com *backoff* exponencial e *jitter*
    - desativar endpoints que falham
    - histórico de entregas
    - ordenação (não garantida) e eventos "magros" (*thin*) × completos
  - **Recebendo** (do PagaFácil):
    - validar a assinatura e o *timestamp* (proteção contra *replay*)
    - responder 2xx rápido e processar depois
    - deduplicar pelo `webhook-id`
  - Menção a HTTP Message Signatures (RFC 9421)
  - Ligação com o Outbox (30.04) para não perder eventos
- **Diagramas:** sequência de envio com *retries*; sequência de recebimento com validação e fila interna.
- **Código:** `AssinaturaWebhook`; `EntregadorWebhook` agendado; assinador HMAC; `POST /v1/integracoes/pagafacil/notificacoes` com verificação.
- **Referências:** https://www.standardwebhooks.com/ · https://github.com/standard-webhooks/standard-webhooks/blob/main/spec/standard-webhooks.md · https://cloudevents.io/ · https://www.rfc-editor.org/rfc/rfc9421

## M22: Conteúdo e transferência

#### 22.01 · Upload de arquivos
`Intermediário` · `Importante`
- **Por que importa:** uploads sem limites derrubam o servidor ou permitem armazenar conteúdo malicioso.
- **Pré-requisitos:** 14.01
- **Tópicos:**
  - `multipart/form-data`, `MultipartFile`, `@RequestPart` (metadados JSON + arquivo)
  - Limites: `spring.servlet.multipart.max-file-size` / `max-request-size` → 413
  - Validar o tipo real (*magic bytes*, Apache Tika), não confiar no `Content-Type`
  - Nomes de arquivo seguros
  - Upload direto como `application/octet-stream`
  - *Streaming* para o *storage* sem carregar tudo na memória
  - URLs pré-assinadas (S3) para arquivos grandes
  - Upload retomável (tus, só a menção)
  - Antivírus (menção)
- **Diagramas:** upload via API × upload direto com URL pré-assinada.
- **Código:** `POST /v1/produtos/{id}/imagens`; validação de tipo e tamanho; testes com `MockMultipartFile`.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/multipart-forms.html · https://docs.spring.io/spring-boot/reference/web/servlet.html (multipart) · https://tus.io/

#### 22.02 · Download, *streaming* e Range
`Intermediário` · `Importante`
- **Por que importa:** exportar 1 milhão de linhas carregando tudo em memória causa `OutOfMemoryError`. O *streaming* resolve.
- **Pré-requisitos:** 22.01, 10.05
- **Tópicos:**
  - `Resource` / `InputStreamResource`
  - `Content-Disposition` (RFC 6266; `attachment`, `filename*` UTF-8)
  - `StreamingResponseBody`
  - *Streaming* do banco (`Stream<T>` do Spring Data com `@Transactional(readOnly)`, *fetch size*)
  - Range requests → 206 Partial Content, `Accept-Ranges`; *timeouts* assíncronos
  - NDJSON para *streaming* de JSON
- **Diagramas:** memória × tempo: carregar tudo × *streaming*.
- **Código:** `GET /v1/relatorios/{id}/arquivo` em CSV por *streaming*; imagem com suporte a Range.
- **Referências:** https://www.rfc-editor.org/rfc/rfc6266 · RFC 9110 §14 (Range) · https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html

#### 22.03 · Compressão
`Intermediário` · `Complementar`
- **Por que importa:** JSON comprime muito bem (70–90%). É ganho de banda quase gratuito, com algumas armadilhas.
- **Pré-requisitos:** 02.04
- **Tópicos:**
  - `Content-Encoding` (gzip, br, zstd) × `Transfer-Encoding`
  - `server.compression.*` e `additional-mime-types` (Boot 4.1)
  - Tamanho mínimo; comprimir no *proxy* ou na app
  - Compressão de requisições
  - BREACH (risco com segredos refletidos em respostas comprimidas)
  - ETag e compressão
- **Diagramas:** nenhum.
- **Código:** configuração no `application.yml`; `curl --compressed` comparando tamanhos.
- **Referências:** RFC 9110 §8.4 · https://docs.spring.io/spring-boot/how-to/webserver.html (compression) · https://www.breachattack.com/

## M23: Proteção da API

#### 23.01 · Rate limiting e quotas
`Avançado` · `Essencial`
- **Por que importa:** protege a API contra abuso e clientes descontrolados e garante uso justo entre parceiros. Também é contrato: o cliente precisa saber os limites.
- **Pré-requisitos:** 21.01, 09.04
- **Tópicos:**
  - Algoritmos: *fixed window*, *sliding window*, *token bucket*, *leaky bucket*
  - Chave do limite (cliente, IP, rota)
  - 429 + `Retry-After`
  - Headers `RateLimit` e `RateLimit-Policy` (*draft* da IETF, versão 11)
  - Distribuído com Redis (Bucket4j)
  - Onde aplicar: gateway × aplicação
  - Quotas comerciais × proteção técnica
  - *Rate limiting* do lado cliente (ligação com 25.03)
- **Diagramas:** *token bucket* animável (fase 2); topologia gateway + app + Redis.
- **Código:** filtro com Bucket4j + Redis por parceiro; resposta 429 com ProblemDetail e headers.
- **Referências:** https://datatracker.ietf.org/doc/draft-ietf-httpapi-ratelimit-headers/ · https://bucket4j.com/ · RFC 6585 §4

#### 23.02 · Limites de payload e proteção contra abuso
`Intermediário` · `Importante`
- **Por que importa:** JSON de 50 MB, arrays com 1 milhão de itens ou aninhamento profundo derrubam a API sem nenhum ataque sofisticado.
- **Pré-requisitos:** 23.01, 06.03
- **Tópicos:**
  - Limites do Tomcat (`max-http-form-post-size`, tamanho de header, `max-swallow-size`)
  - Restrições de leitura do Jackson (`StreamReadConstraints`: tamanho de string, profundidade, tamanho de número), configuráveis via `spring.jackson.factory.*` no Boot 4.1
  - `@Size` em coleções
  - Paginação com máximo
  - *Timeouts* de requisição
  - Limites como parte do contrato (documentar)
- **Diagramas:** nenhum.
- **Código:** configuração de limites no Bazar; testes enviando payloads abusivos.
- **Referências:** https://github.com/FasterXML/jackson-core (StreamReadConstraints) · Spring Boot 4.1 Release Notes · OWASP API4:2023 Unrestricted Resource Consumption https://owasp.org/API-Security/editions/2023/en/0xa4-unrestricted-resource-consumption/

---

# Parte 10: Consumindo outras APIs

## M24: Clientes HTTP no Spring

#### 24.01 · Panorama dos clientes HTTP no Spring
`Básico` · `Essencial`
- **Por que importa:** há quatro opções, e uma está sendo depreciada. Escolher certo evita retrabalho.
- **Pré-requisitos:** 08.02, 19.02
- **Tópicos:**
  - `RestTemplate`: depreciado formalmente no Framework 7.1 (nov/2026), remoção prevista no 8.0
  - `RestClient` (síncrono, fluente)
  - `WebClient` (reativo)
  - HTTP interfaces (`@HttpExchange`, declarativo)
  - Bibliotecas HTTP por baixo: `ClientHttpRequestFactory` com JDK HttpClient, Jetty, Reactor Netty e Apache HttpClient 5
  - Configuração global: `HttpClientSettings` e `spring.http.clients.*` (Boot 4)
  - Quadro de decisão
- **Diagramas:** camadas: API do cliente → *request factory* → biblioteca HTTP → rede.
- **Código:** a mesma chamada à FreteJá com os três estilos.
- **Referências:** https://docs.spring.io/spring-framework/reference/integration/rest-clients.html · https://docs.spring.io/spring-boot/reference/io/rest-client.html · Spring Framework 7.1 Release Notes

#### 24.02 · RestClient a fundo
`Intermediário` · `Essencial`
- **Por que importa:** é o cliente padrão para aplicações MVC. Saber tratar erros, configurar *timeouts* e interceptar requisições é obrigatório para integrar com terceiros.
- **Pré-requisitos:** 24.01
- **Tópicos:**
  - `RestClient.Builder` autoconfigurado; `baseUrl`, headers padrão
  - `retrieve()` × `exchange()`
  - `onStatus` e conversão de erros remotos em exceções de domínio
  - Ler o ProblemDetail do remoto
  - `ClientHttpRequestInterceptor` (autenticação, logging, *correlation id*)
  - *Timeouts* por *request factory*
  - Mudança no 7.1 (o *builder* parte dos *converters* padrão; `disableDefaults()`)
  - Logging seguro de corpo
  - Observabilidade automática (ligação com 27.06)
- **Diagramas:** fluxo de uma chamada com interceptors e tratamento de status.
- **Código:** `PagaFacilClient` com `RestClient`; tratamento de 4xx/5xx; interceptor de autenticação.
- **Referências:** https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-restclient · Spring Boot Reference (RestClient)

#### 24.03 · HTTP interfaces (`@HttpExchange`) e o registro de clientes
`Intermediário` · `Essencial`
- **Por que importa:** clientes declarativos reduzem *boilerplate* e, com o registro do Spring 7, ficam configuráveis por propriedade, como o Feign, mas nativos.
- **Pré-requisitos:** 24.02
- **Tópicos:**
  - `@HttpExchange`, `@GetExchange`, `@PostExchange`; parâmetros
  - `HttpServiceProxyFactory`
  - **`@ImportHttpServices`** e grupos de serviços
  - Configuração por propriedades no Boot 4: `spring.http.serviceclient.<grupo>.base-url`, `.connect-timeout`, `.read-timeout`, além de headers padrão, versão de API, *redirects*, *cookies* e SSL por grupo; padrões globais em `spring.http.clients.*`
  - Lembrete: a configuração de versão do servidor não é aplicada ao cliente; configure no *builder* ou no grupo
  - Customização por grupo (`AbstractHttpServiceRegistrar`, *configurers*)
  - Versão de API no cliente
  - Migração a partir do OpenFeign
- **Diagramas:** interface → *proxy* → RestClient do grupo → serviço remoto.
- **Código:** `FreteJaClient` como HTTP interface; configuração do grupo `freteja` no `application.yml`.
- **Referências:** https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface · Spring Boot 4.0 Release Notes (HTTP Service Clients)

#### 24.04 · WebClient em aplicações MVC
`Intermediário` · `Complementar`
- **Por que importa:** ainda é a opção para *streaming* de respostas e para chamadas paralelas não bloqueantes, e aparece muito em código existente.
- **Pré-requisitos:** 24.02, 19.05
- **Tópicos:**
  - Quando faz sentido em app MVC
  - `block()` e seus riscos
  - Chamadas paralelas com `Mono.zip` × virtual threads com `RestClient`
  - *Streaming* de resposta
  - Configuração de *timeouts* no Reactor Netty
- **Diagramas:** nenhum.
- **Código:** cotação paralela em duas transportadoras com `WebClient` e a versão equivalente com virtual threads.
- **Referências:** https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html

#### 24.05 · Testando clientes HTTP
`Intermediário` · `Essencial`
- **Por que importa:** integrações falham de formas que só aparecem com respostas reais (lentidão, 503, JSON inesperado). É preciso simulá-las.
- **Pré-requisitos:** 24.03, 11.04
- **Tópicos:**
  - `@RestClientTest` + `MockRestServiceServer`
  - WireMock 3: *stubs*, cenários com estado, atrasos, falhas, verificação de requisições
  - Integração oficial `org.wiremock.integrations:wiremock-spring-boot` 4.4.3 (linha 4.x compatível com Boot 4): `@EnableWireMock`, `@ConfigureWireMock`, `@InjectWireMock`
  - Gravar e reproduzir (*record/playback*)
  - Testar *timeouts* e *retries* (prévia de 25.01)
- **Diagramas:** nenhum.
- **Código:** `PagaFacilClientTest` com `MockRestServiceServer`; cenário WireMock "503 duas vezes e depois 201".
- **Referências:** https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html (`@RestClientTest`) · https://wiremock.org/docs/

## M25: Resiliência

#### 25.01 · Timeouts e orçamento de tempo
`Intermediário` · `Essencial`
- **Por que importa:** sem *timeout*, uma dependência lenta trava *threads* e derruba a API inteira. É a primeira linha de defesa de qualquer integração.
- **Pré-requisitos:** 24.05, 19.01
- **Tópicos:**
  - *Connect timeout*, *read/response timeout*, *timeout* de aquisição de conexão do *pool*
  - Valores padrão perigosos (infinito)
  - Semântica no Spring 7.1 (0 = sem *timeout*; negativo é rejeitado)
  - Orçamento de tempo e propagação de *deadline*
  - *Timeout* do servidor × do cliente × do gateway (alinhamento)
  - Como escolher valores (p99 da dependência)
- **Diagramas:** cascata de *timeouts* (gateway 30 s > API 10 s > dependência 2 s).
- **Código:** `HttpClientSettings` com *timeouts* para PagaFácil e FreteJá; teste WireMock com atraso.
- **Referências:** https://docs.spring.io/spring-boot/reference/io/rest-client.html · https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/

#### 25.02 · Retry: quando é seguro e como fazer
`Avançado` · `Essencial`
- **Por que importa:** *retry* ingênuo duplica cobranças e amplifica incidentes (*retry storms*). Feito certo, absorve falhas transitórias.
- **Pré-requisitos:** 25.01, 21.01
- **Tópicos:**
  - O que é retentável (métodos idempotentes, 503/429/rede) e o que não é (400, POST sem idempotency key)
  - *Backoff* exponencial com *jitter*
  - Respeitar `Retry-After`
  - Limite de tentativas e orçamento de *retry*
  - **Retry nativo do Spring Framework 7:** `@Retryable`, `RetryTemplate`, `RetryPolicy` e `@EnableResilientMethods` (em `spring-core`)
  - Spring Retry (projeto antigo) × nativo
  - *Retries* em camadas (cliente + gateway + biblioteca) multiplicando a carga
- **Diagramas:** *retry* sem e com *jitter* (picos sincronizados × espalhados); multiplicação de *retries* em camadas.
- **Código:** `@Retryable` na cotação de frete; `RetryTemplate` no pagamento usando a idempotency key; testes com WireMock.
- **Referências:** https://docs.spring.io/spring-framework/reference/core/resilience.html · Spring Framework 7.0 Release Notes · https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/

#### 25.03 · Circuit breaker, bulkhead e limites de concorrência
`Avançado` · `Essencial`
- **Por que importa:** evita que uma dependência fora do ar consuma todos os recursos e permite falhar rápido enquanto ela se recupera.
- **Pré-requisitos:** 25.02
- **Tópicos:**
  - Estados do *circuit breaker* (fechado, aberto, meio-aberto), janelas e limiares
  - Resilience4j 2.4 (`resilience4j-spring-boot4`): `@CircuitBreaker`, `@Bulkhead`, `@RateLimiter`, `@TimeLimiter`, configuração por instância, ordem dos *decorators*
  - `@ConcurrencyLimit` nativo do Spring 7 como *bulkhead* simples
  - Métricas e eventos do *breaker*
  - Spring Cloud Circuit Breaker (abstração, só a menção)
- **Diagramas:** máquina de estados do *circuit breaker*; ordem Retry(CircuitBreaker(RateLimiter(TimeLimiter(Bulkhead(chamada))))), a ordem padrão dos aspectos na doc do Resilience4j.
- **Código:** `PagaFacilClient` protegido com *circuit breaker* + *bulkhead*; teste abrindo o circuito.
- **Referências:** https://resilience4j.readme.io/docs · https://martinfowler.com/bliki/CircuitBreaker.html · Spring Framework reference (Resilience)

#### 25.04 · Fallbacks e degradação graciosa
`Avançado` · `Importante`
- **Por que importa:** quando a FreteJá cai, o Bazar pode oferecer frete fixo em vez de impedir a compra. Isso é decisão de produto expressa em código.
- **Pré-requisitos:** 25.03
- **Tópicos:**
  - Tipos de *fallback*: valor padrão, *cache* antigo (`stale`), fila para depois, erro claro
  - *Fallbacks* perigosos (esconder falhas, inconsistência)
  - Informar o cliente (campo `estimado: true`, header `Warning`, obsoleto e por quê)
  - *Load shedding* no servidor (503 + `Retry-After`)
  - Testes de caos (menção)
- **Diagramas:** árvore de decisão de *fallback* por tipo de dependência.
- **Código:** cotação com *fallback* para tabela fixa e indicação no corpo; `stale-if-error` para o catálogo.
- **Referências:** https://aws.amazon.com/builders-library/avoiding-fallback-in-distributed-systems/ · https://resilience4j.readme.io/docs/getting-started-3 (regras de *fallback*; a página cita os módulos Boot 2/3, mas o `resilience4j-spring-boot4` 2.4.0 segue o mesmo modelo)

---

# Parte 11: Segurança essencial para APIs 🔒

> Esta parte cobre só o que todo desenvolvedor de API precisa aplicar. Autenticação, OAuth 2.1/OIDC e Spring Security a fundo estão na **documentação de segurança** (link a definir na fase 2).

## M26: O essencial de segurança para quem constrói APIs

#### 26.01 · Panorama: OWASP API Security Top 10
`Intermediário` · `Essencial`
- **Por que importa:** a maioria dos incidentes em APIs vem de falhas de autorização e de exposição de dados, não de criptografia. O Top 10 dá o mapa.
- **Pré-requisitos:** 12.03, 23.01
- **Tópicos:**
  - OWASP API Security Top 10 2023, item a item, com um exemplo no Bazar e a página desta doc ou da doc de segurança que trata cada um
  - Autenticação × autorização
  - Defesa em profundidade
  - Onde cada controle mora (gateway, app, banco)
- **Diagramas:** tabela "risco → exemplo no Bazar → página".
- **Código:** nenhum.
- **Referências:** https://owasp.org/API-Security/editions/2023/en/0x11-t10/ · link para a doc de segurança 🔒

#### 26.02 · CORS
`Básico` · `Essencial`
- **Por que importa:** é o erro mais comum quando um front-end consome a API. É frequentemente "resolvido" com `*`, abrindo brechas.
- **Pré-requisitos:** 26.01, 02.04
- **Tópicos:**
  - *Same-origin policy*; requisições simples × *preflight* (`OPTIONS`)
  - Headers `Access-Control-*`; credenciais e por que `*` não funciona com elas
  - Configuração global (`WebMvcConfigurer#addCorsMappings`) × `@CrossOrigin` × Spring Security (`CorsConfigurationSource`)
  - CORS não é mecanismo de segurança do servidor (protege o usuário do navegador)
  - Depurar no DevTools
- **Diagramas:** sequência de *preflight*.
- **Código:** configuração CORS do Bazar por perfil; `curl` simulando o *preflight*.
- **Referências:** https://fetch.spec.whatwg.org/#http-cors-protocol · https://developer.mozilla.org/pt-BR/docs/Web/HTTP/CORS · https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html

#### 26.03 · Autorização por objeto, exposição de dados e validação de entrada
`Intermediário` · `Essencial`
- **Por que importa:** BOLA/IDOR (cliente A lendo o pedido do cliente B) é o risco nº 1 do OWASP API, e se resolve no design e no código da API, não no *framework*.
- **Pré-requisitos:** 26.01, 06.01
- **Tópicos:**
  - BOLA (API1): sempre filtrar pelo dono (`findByIdAndClienteId`)
  - BOPLA (API3): *mass assignment* e exposição excessiva; DTOs como defesa
  - Mascarar dados sensíveis (CPF) e não logá-los
  - LGPD: minimização
  - Validação de entrada como segurança: tipos, tamanhos, *allowlists*
  - Injeção (SQL, JPQL, *log injection*)
  - 404 × 403 para não revelar existência
- **Diagramas:** sequência de um ataque BOLA e a correção.
- **Código:** `PedidoService.buscarDoCliente`; teste que tenta acessar o pedido de outro cliente; serializador que mascara o CPF.
- **Referências:** https://owasp.org/API-Security/editions/2023/en/0xa1-broken-object-level-authorization/ · https://owasp.org/API-Security/editions/2023/en/0xa3-broken-object-property-level-authorization/ · https://www.gov.br/anpd/

#### 26.04 · Resource Server com JWT (o essencial)
`Intermediário` · `Essencial`
- **Por que importa:** é a forma mais comum de proteger APIs. Esta página entrega o mínimo funcional e correto; o resto está na doc de segurança.
- **Pré-requisitos:** 26.03
- **Tópicos:**
  - JWT em uma página (estrutura, assinatura, `exp`, `aud`, `iss`)
  - `spring-boot-starter-oauth2-resource-server`, `issuer-uri`, validação de audiência
  - Escopos e papéis → `@PreAuthorize`
  - Respostas 401 e 403 como ProblemDetail (`WWW-Authenticate`)
  - Obter o cliente atual (`@AuthenticationPrincipal`)
  - Testar com `jwt()` do `spring-security-test`
  - Swagger UI com autenticação
- **Diagramas:** sequência cliente → IdP → API com validação do token (alto nível).
- **Código:** `SecurityConfig` mínimo do Bazar; endpoint com `@PreAuthorize("hasAuthority('SCOPE_pedidos:escrever')")`; teste com JWT simulado.
- **Referências:** https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html · RFC 7519 (JWT) · RFC 9068 (JWT Access Token Profile) · doc de segurança 🔒

#### 26.05 · Transporte, segredos, headers de segurança e SSRF
`Intermediário` · `Importante`
- **Por que importa:** HTTPS mal configurado, segredos no Git e chamadas a URLs fornecidas pelo usuário (webhooks!) são falhas comuns e graves.
- **Pré-requisitos:** 26.04, 21.03
- **Tópicos:**
  - HTTPS obrigatório; HSTS
  - TLS terminado no *proxy*: `server.forward-headers-strategy` e `X-Forwarded-*` / `Forwarded` (RFC 7239)
  - Headers de segurança relevantes para APIs (`Cache-Control: no-store` em dados sensíveis, `X-Content-Type-Options`)
  - Segredos fora do código (variáveis de ambiente, Vault, *secrets* do Kubernetes)
  - Rotação do segredo de webhook
  - **SSRF** (API7):
    - validar as URLs de webhook
    - bloquear IPs internos
    - `InetAddressFilter` nos clientes HTTP (Boot 4.1)
  - Consumo inseguro de APIs de terceiros (API10)
- **Diagramas:** ataque SSRF via URL de webhook apontando para `169.254.169.254`.
- **Código:** validação de URL de webhook; cliente de entrega com filtro de endereços; `forward-headers-strategy`.
- **Referências:** https://owasp.org/API-Security/editions/2023/en/0xa7-server-side-request-forgery/ · Spring Boot 4.1 Release Notes · https://www.rfc-editor.org/rfc/rfc7239 · https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html

---

# Parte 12: Observabilidade

## M27: Enxergando a API em produção

#### 27.01 · Fundamentos de observabilidade
`Intermediário` · `Essencial`
- **Por que importa:** sem observabilidade a equipe descobre os problemas pelo cliente. Esta página define o vocabulário e o que medir.
- **Pré-requisitos:** 19.03
- **Tópicos:**
  - Logs, métricas e *traces* (e como se conectam)
  - Métodos RED (*Rate*, *Errors*, *Duration*) e USE
  - SLI, SLO e orçamento de erro
  - Monitoramento × observabilidade
  - Cardinalidade
  - OpenTelemetry como padrão aberto
  - Stack local (Grafana LGTM via Docker Compose)
- **Diagramas:** os três sinais correlacionados pelo `traceId`.
- **Código:** `compose.yaml` com a stack de observabilidade.
- **Referências:** https://opentelemetry.io/docs/concepts/ · https://sre.google/sre-book/service-level-objectives/ · https://grafana.com/blog/2018/08/02/the-red-method-how-to-instrument-your-services/

#### 27.02 · Spring Boot Actuator
`Básico` · `Essencial`
- **Por que importa:** expõe saúde, métricas e informações operacionais prontas, mas exposto sem cuidado vaza dados sensíveis.
- **Pré-requisitos:** 27.01
- **Tópicos:**
  - Endpoints (`health`, `info`, `metrics`, `prometheus`, `env`, `loggers`, `sbom`, `httpexchanges`)
  - Exposição (`management.endpoints.web.exposure.include`)
  - Porta de gerenciamento separada
  - Proteger os endpoints
  - `info` com dados de *build* e git, além dos novos campos `process.*` do Boot 4.1
  - Endpoints customizados (`@Endpoint`)
- **Diagramas:** nenhum.
- **Código:** configuração do Actuator no Bazar; `info` com versão; endpoint `@Endpoint` com estatísticas de entregas de webhook.
- **Referências:** https://docs.spring.io/spring-boot/reference/actuator/index.html · https://docs.spring.io/spring-boot/reference/actuator/endpoints.html

#### 27.03 · Health checks: *liveness*, *readiness* e indicadores
`Intermediário` · `Essencial`
- **Por que importa:** health checks errados reiniciam pods saudáveis em cascata ou mandam tráfego para instâncias que não estão prontas.
- **Pré-requisitos:** 27.02
- **Tópicos:**
  - *Liveness* × *readiness* × *startup*; `ApplicationAvailability`
  - Grupos de *health*
  - Nunca colocar dependências externas no *liveness*
  - `HealthIndicator` customizado (PagaFácil)
  - Detalhes só para autorizados
  - *Readiness* durante o *shutdown*
- **Diagramas:** o que acontece com o *liveness* dependendo do banco quando o banco cai (reinício em massa).
- **Código:** grupos `liveness`/`readiness`; `PagaFacilHealthIndicator` no grupo de *readiness* com cautela.
- **Referências:** https://docs.spring.io/spring-boot/reference/actuator/endpoints.html#actuator.endpoints.kubernetes-probes · https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/

#### 27.04 · Logs estruturados
`Intermediário` · `Essencial`
- **Por que importa:** logs em JSON podem ser pesquisados e correlacionados. Logs em texto livre viram `grep` em incidente.
- **Pré-requisitos:** 27.01
- **Tópicos:**
  - `logging.structured.format.console` (`ecs`, `logstash`, `gelf`)
  - Campos do serviço (`logging.structured.ecs.service.*`)
  - MDC e a API fluente do SLF4J (`addKeyValue`)
  - Customização (`include`/`exclude`/`rename`/`add`, `StructuredLoggingJsonMembersCustomizer`)
  - *Stack traces* (`logging.structured.json.stacktrace.*`)
  - Níveis e o que logar (eventos de negócio, decisões, erros) e o que não logar (dados pessoais, segredos, corpo completo)
  - Mudança de nível em tempo real via Actuator
- **Diagramas:** nenhum.
- **Código:** configuração ECS no Bazar; log de evento de negócio com `addKeyValue`; exemplo de saída.
- **Referências:** https://docs.spring.io/spring-boot/reference/features/logging.html#features.logging.structured · https://www.elastic.co/guide/en/ecs/current/index.html

#### 27.05 · Métricas com Micrometer
`Intermediário` · `Essencial`
- **Por que importa:** métricas alimentam *dashboards*, alertas e SLOs, e são a forma mais barata de saber se a API está saudável.
- **Pré-requisitos:** 27.02
- **Tópicos:**
  - Micrometer 1.17: `MeterRegistry`, `Counter`, `Timer`, `Gauge`, `DistributionSummary`
  - **Observation API** (`@Observed`, `ObservationRegistry`): uma instrumentação para métricas e *traces*
  - `http.server.requests` e `http.client.requests`
  - Percentis × histogramas; SLO *buckets*
  - Tags e o perigo da cardinalidade (nunca o id do pedido como tag)
  - Prometheus
  - Métricas de negócio (pedidos criados, valor) e métricas por versão de API (18.04)
- **Diagramas:** do contador ao *dashboard* (app → `/actuator/prometheus` → Prometheus → Grafana).
- **Código:** contador `bazar.pedidos.criados` com tag `metodoPagamento`; `@Observed` no `PedidoService`; alerta de exemplo para SLO.
- **Referências:** https://docs.micrometer.io/micrometer/reference/ · https://docs.micrometer.io/micrometer/reference/observation.html · https://docs.spring.io/spring-boot/reference/actuator/metrics.html

#### 27.06 · Tracing distribuído com OpenTelemetry
`Avançado` · `Essencial`
- **Por que importa:** com várias dependências (PagaFácil, FreteJá, banco, Redis), só o *tracing* mostra onde o tempo de uma requisição lenta foi gasto.
- **Pré-requisitos:** 27.05, 24.02
- **Tópicos:**
  - *Spans*, *traces*, contexto; W3C Trace Context (`traceparent`)
  - `spring-boot-starter-opentelemetry` (Boot 4) × Micrometer Tracing com *bridge* OTel × agente Java do OpenTelemetry: quando usar cada um
  - Exportação OTLP; amostragem (*sampler*)
  - Propagação automática no `RestClient` e no `@Async`
  - *Spans* customizados
  - *Exemplars* (de uma métrica para o *trace*)
  - OpenTelemetry Collector
- **Diagramas:** *waterfall* de um *trace* de `POST /v1/pedidos/{id}/pagamentos`.
- **Código:** configuração OTLP; *span* customizado na reserva de estoque; visualização no Grafana Tempo.
- **Referências:** https://opentelemetry.io/docs/languages/java/ · https://www.w3.org/TR/trace-context/ · https://docs.spring.io/spring-boot/reference/actuator/tracing.html · Spring Boot 4.0/4.1 Release Notes

#### 27.07 · Correlation ID e contexto de requisição
`Intermediário` · `Importante`
- **Por que importa:** ligar o erro relatado pelo cliente ao log e ao *trace* exatos economiza horas de investigação.
- **Pré-requisitos:** 27.06, 27.04
- **Tópicos:**
  - `traceId` × *request id* × *correlation id* de negócio
  - Aceitar ou gerar `X-Request-Id`
  - Devolver o id na resposta e no ProblemDetail (`traceId`)
  - Propagar para logs (MDC automático), filas e webhooks
  - Não confiar cegamente no id vindo de fora
- **Diagramas:** o id percorrendo cliente → gateway → API → PagaFácil → log.
- **Código:** filtro que adiciona `traceId` à resposta; extensão do ProblemDetail com `traceId`.
- **Referências:** W3C Trace Context · Micrometer Tracing docs https://docs.micrometer.io/tracing/reference/

---

# Parte 13: Performance

## M28: Medir, entender e otimizar

#### 28.01 · Medindo antes de otimizar: testes de carga
`Intermediário` · `Essencial`
- **Por que importa:** otimizar sem medir é chute. Testes de carga revelam gargalos, regressões e a capacidade real.
- **Pré-requisitos:** 27.05
- **Tópicos:**
  - Latência (p50, p95, p99) × média
  - *Throughput*; *coordinated omission*
  - Tipos de teste (carga, estresse, *soak*, *spike*)
  - k6 2.3 ou Gatling 3.16; cenários realistas
  - Ambiente de teste comparável
  - Interpretar os resultados com as métricas de 27.05
- **Diagramas:** curva latência × carga (o "joelho").
- **Código:** script k6 do fluxo de compra; *dashboard* com os resultados.
- **Referências:** https://grafana.com/docs/k6/latest/ · https://docs.gatling.io/ · https://www.brendangregg.com/usemethod.html

#### 28.02 · Pool de conexões (HikariCP)
`Intermediário` · `Essencial`
- **Por que importa:** o *pool* do banco é o gargalo mais comum em APIs, especialmente com virtual threads, quando há mais concorrência do que conexões.
- **Pré-requisitos:** 28.01, 19.02
- **Tópicos:**
  - Como o *pool* funciona; dimensionamento (fórmula do HikariCP e por que "mais" não é melhor)
  - `maximum-pool-size`, `connection-timeout`, `max-lifetime`, `leak-detection-threshold`
  - Métricas do Hikari
  - Virtual threads + *pool* pequeno (limitar a concorrência: *semaphore* / `@ConcurrencyLimit`)
  - Conexões por instância × limite do Postgres (PgBouncer)
- **Diagramas:** muitas *threads* → *pool* → banco (fila de espera de conexão).
- **Código:** configuração do Hikari; *dashboard* de `hikaricp_connections_pending`.
- **Referências:** https://github.com/brettwooldridge/HikariCP · https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing

#### 28.03 · Cache na aplicação: `@Cacheable`, Caffeine e Redis
`Intermediário` · `Importante`
- **Por que importa:** dados lidos muito e alterados pouco (catálogo, categorias) não precisam ir ao banco toda vez. Porém, *cache* errado serve dados obsoletos ou derruba o banco quando expira.
- **Pré-requisitos:** 28.01, 20.01
- **Tópicos:**
  - Abstração de *cache* do Spring (`@Cacheable`, `@CacheEvict`, `@CachePut`)
  - Caffeine (local) × Redis (distribuído) × multinível
  - TTL e invalidação
  - *Cache stampede* (`sync = true`, *jitter* no TTL)
  - Serialização no Redis (Jackson 3)
  - Cache HTTP (20.01) × cache de aplicação
  - O que não cachear
  - Métricas de *hit ratio*
- **Diagramas:** padrões *cache-aside* e *write-through*; *stampede*.
- **Código:** `CategoriaService` com Caffeine; `ProdutoService` com Redis; invalidação ao atualizar.
- **Referências:** https://docs.spring.io/spring-framework/reference/integration/cache.html · https://docs.spring.io/spring-boot/reference/io/caching.html · https://github.com/ben-manes/caffeine/wiki

#### 28.04 · Consultas e paginação eficientes
`Avançado` · `Importante`
- **Por que importa:** a maior parte da latência de uma API está no banco. Índices e *queries* corretos valem mais que qualquer *tuning* de JVM.
- **Pré-requisitos:** 28.01, 13.04, 10.05
- **Tópicos:**
  - `EXPLAIN (ANALYZE, BUFFERS)`
  - Índices compostos para filtros e ordenação; índices parciais
  - `COUNT` caro e alternativas (estimativas, `Slice`, *keyset*)
  - `pg_stat_statements`
  - *Batch inserts* (`hibernate.jdbc.batch_size`)
  - Revisão do N+1 em escala
- **Diagramas:** plano de execução antes e depois do índice.
- **Código:** migration com índice para `GET /v1/produtos?categoria&sort=preco`; comparação de tempos.
- **Referências:** https://www.postgresql.org/docs/current/using-explain.html · https://use-the-index-luke.com/ · Hibernate ORM User Guide (batching)

#### 28.05 · Profiling da JVM
`Avançado` · `Complementar`
- **Por que importa:** quando métricas e *traces* não explicam a lentidão (CPU, GC, contenção), o *profiling* mostra exatamente qual código está consumindo recursos.
- **Pré-requisitos:** 28.01
- **Tópicos:**
  - JDK Flight Recorder e JDK Mission Control
  - async-profiler e *flame graphs*
  - GC (G1, ZGC geracional) e *heap*
  - Contenção de *locks*
  - *Profiling* contínuo em produção (Pyroscope, só a menção)
- **Diagramas:** como ler um *flame graph*.
- **Código:** comando `jcmd JFR.start`; *flame graph* de um endpoint com serialização pesada.
- **Referências:** https://dev.java/learn/jvm/jfr/ · https://github.com/async-profiler/async-profiler · https://docs.oracle.com/en/java/javase/25/gctuning/

---

# Parte 14: Produção e deploy

## M29: Da máquina do desenvolvedor para produção

#### 29.01 · Configuração externa e profiles
`Básico` · `Essencial`
- **Por que importa:** a mesma imagem precisa rodar em desenvolvimento, homologação e produção mudando só a configuração. Segredos não podem estar no repositório.
- **Pré-requisitos:** 05.02
- **Tópicos:**
  - Ordem de precedência das fontes
  - Profiles (`spring.profiles.active`, grupos)
  - `@ConfigurationProperties` com records e validação (`@Validated`)
  - Variáveis de ambiente (*relaxed binding*); `spring.config.import` (`configtree:` para *secrets* do Kubernetes)
  - Metadados para a IDE
  - O que nunca colocar em `application.yml`
- **Diagramas:** pirâmide de precedência das fontes de configuração.
- **Código:** `PagaFacilProperties` (record validado); `application-prod.yml`.
- **Referências:** https://docs.spring.io/spring-boot/reference/features/external-config.html · https://docs.spring.io/spring-boot/reference/features/profiles.html

#### 29.02 · Containers: Dockerfile, layered jars e buildpacks
`Intermediário` · `Essencial`
- **Por que importa:** imagens grandes e inseguras atrasam *deploys* e ampliam a superfície de ataque. A JVM precisa conhecer os limites do contêiner.
- **Pré-requisitos:** 29.01
- **Tópicos:**
  - Jar executável × camadas (`-Djarmode=tools extract --layers`)
  - Dockerfile *multi-stage* com JRE 25
  - Usuário não *root*
  - Buildpacks (`spring-boot:build-image`, Paketo)
  - JVM em contêiner (`MaxRAMPercentage`, CPU)
  - *AOT cache* (JEP 483/514/515) para o *startup*: a doc do Boot recomenda AOT cache em vez de CDS no Java 25+; requer *training run* com o jar extraído (`-Djarmode=tools extract`) e é suportado também pelos *buildpacks*
  - SBOM; *scan* de vulnerabilidades
- **Diagramas:** camadas da imagem e o que muda a cada *build*.
- **Código:** `Dockerfile` do Bazar; `./mvnw spring-boot:build-image`; comparação de *startup* com e sem CDS/AOT cache.
- **Referências:** https://docs.spring.io/spring-boot/reference/packaging/container-images/index.html · https://paketo.io/docs/ · https://docs.spring.io/spring-boot/reference/packaging/aot-cache.html · https://openjdk.org/jeps/483 · https://openjdk.org/jeps/514 · https://openjdk.org/jeps/515

#### 29.03 · Imagem nativa com GraalVM
`Avançado` · `Complementar`
- **Por que importa:** o *startup* em milissegundos e o baixo uso de memória importam em *serverless* e *scale-to-zero*, ao custo de *build* lento e restrições.
- **Pré-requisitos:** 29.02
- **Tópicos:**
  - AOT do Spring; GraalVM 25
  - `native:compile` e `build-image` nativo
  - *Reachability metadata* e *runtime hints* (`RuntimeHintsRegistrar`)
  - Bibliotecas incompatíveis
  - Testar em modo nativo
  - Comparação nativa × JVM + AOT cache × CRaC (suportado pelo Boot 4.1 sob demanda ou no *startup*; exige JDK com CRaC, como Liberica ou Zulu, e gestão do ciclo de vida de recursos)
- **Diagramas:** pipeline JVM × nativa (*build time* × *runtime*).
- **Código:** `RuntimeHints` para um serializador customizado; medidas de *startup* e memória.
- **Referências:** https://docs.spring.io/spring-boot/reference/packaging/native-image/index.html · https://docs.spring.io/spring-boot/reference/packaging/checkpoint-restore.html · https://docs.spring.io/spring-framework/reference/integration/checkpoint-restore.html · https://www.graalvm.org/latest/reference-manual/native-image/

#### 29.04 · Kubernetes para APIs: probes, *graceful shutdown* e *rollouts*
`Avançado` · `Importante`
- **Por que importa:** *deploys* sem *graceful shutdown* e sem probes corretas derrubam requisições em andamento a cada versão nova.
- **Pré-requisitos:** 29.02, 27.03
- **Tópicos:**
  - Probes mapeadas para o Actuator
  - *Graceful shutdown* (`server.shutdown=graceful`, `spring.lifecycle.timeout-per-shutdown-phase`) e o *preStop*
  - Requisições e SSE em andamento
  - *Rolling update* e compatibilidade entre versões da API e do esquema (expand-contract, 10.02)
  - Recursos (*requests/limits*) e JVM
  - HPA por métricas
  - ConfigMap e Secret
- **Diagramas:** sequência do *shutdown* (SIGTERM → *readiness* falha → drena → encerra).
- **Código:** manifestos `Deployment`/`Service` do Bazar; configuração de *shutdown*.
- **Referências:** https://docs.spring.io/spring-boot/reference/web/graceful-shutdown.html · https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-termination · https://docs.spring.io/spring-boot/how-to/deployment/cloud.html#howto.deployment.cloud.kubernetes

#### 29.05 · API Gateway e BFF
`Avançado` · `Importante`
- **Por que importa:** decidir o que fica no gateway (TLS, autenticação, *rate limit*, roteamento) e o que fica na aplicação evita duplicação e lacunas de segurança.
- **Pré-requisitos:** 29.04, 23.01, 26.04
- **Tópicos:**
  - Responsabilidades do gateway: roteamento, TLS, autenticação, *rate limit*, CORS, transformação mínima
  - Spring Cloud Gateway 5.0.x (Server MVC e WebFlux), do *release train* 2025.1 (Oakwood), compatível com Boot 4.1 a partir da 2025.1.2
  - Gateways gerenciados
  - *Backend for Frontend* (BFF)
  - Riscos (lógica de negócio no gateway, ponto único de falha)
  - Versionamento no gateway
- **Diagramas:** topologia cliente web/móvel/parceiro → gateway → BFF → Bazar.
- **Código:** rota do Spring Cloud Gateway com *rate limit* para `/v1/**`.
- **Referências:** https://docs.spring.io/spring-cloud-gateway/reference/ · https://samnewman.io/patterns/architectural/bff/

#### 29.06 · CI/CD para APIs
`Intermediário` · `Importante`
- **Por que importa:** o pipeline é onde todas as garantias anteriores (testes, *lint* de contrato, *breaking changes*, imagem segura) viram algo automático.
- **Pré-requisitos:** 29.02, 17.04, 18.05
- **Tópicos:**
  - Etapas: *build*, testes unitários e de integração (Testcontainers no CI), *lint* (Spectral), `oasdiff`, contrato (Pact `can-i-deploy`), imagem, *scan*, *deploy*
  - Ambientes; *feature flags* (menção)
  - Publicar o contrato e o *changelog*
- **Diagramas:** pipeline completo com *gates*.
- **Código:** workflow do GitHub Actions do Bazar.
- **Referências:** https://docs.github.com/actions · https://docs.pact.io/pact_broker/can_i_deploy

---

# Parte 15: Arquitetura

## M30: APIs no contexto da arquitetura

#### 30.01 · Monólito modular com Spring Modulith
`Avançado` · `Importante`
- **Por que importa:** a maioria dos sistemas deveria começar como monólito bem modularizado. O Spring Modulith verifica as fronteiras e prepara uma futura extração.
- **Pré-requisitos:** 05.02, 10.03
- **Tópicos:**
  - Módulos de aplicação (pacotes de topo); API pública × interna
  - `ApplicationModules.verify()`
  - Eventos entre módulos (`ApplicationEventPublisher`, `@ApplicationModuleListener`)
  - Testes por módulo (`@ApplicationModuleTest`)
  - Documentação gerada (C4)
  - Versão: Spring Modulith 2.1.2, compilado contra o Boot 4.1.2
- **Diagramas:** módulos do Bazar e as dependências permitidas.
- **Código:** teste de verificação de módulos; `PedidoCriado` publicado em `pedidos` e consumido em `integracoes`.
- **Referências:** https://docs.spring.io/spring-modulith/reference/ · https://www.kamilgrzybek.com/blog/posts/modular-monolith-primer

#### 30.02 · Microsserviços: quando (não) usar e como se comunicam
`Avançado` · `Importante`
- **Por que importa:** microsserviços resolvem problemas organizacionais ao custo de complexidade distribuída. Escolhê-los cedo demais é um erro comum e caro.
- **Pré-requisitos:** 30.01, 25.03
- **Tópicos:**
  - Critérios (times, escala independente, domínio)
  - Custos (rede, consistência, observabilidade, *deploy*)
  - Comunicação síncrona (REST/gRPC) × assíncrona (eventos)
  - Acoplamento temporal; cadeias síncronas e disponibilidade composta
  - *Service discovery* (menção)
  - Estrangulamento (*strangler fig*)
- **Diagramas:** disponibilidade composta (99,9% × 5 serviços em série); extração do módulo de pagamentos.
- **Código:** nenhum (decisão e ADR de exemplo).
- **Referências:** https://martinfowler.com/articles/microservices.html · https://microservices.io/ · https://martinfowler.com/bliki/MonolithFirst.html

#### 30.03 · Eventos e mensageria: introdução
`Avançado` · `Importante`
- **Por que importa:** eventos desacoplam serviços no tempo e permitem integrações escaláveis. Toda API sênior convive com mensageria.
- **Pré-requisitos:** 30.02
- **Tópicos:**
  - Eventos de domínio × de integração
  - Notificação × *event-carried state transfer*
  - Kafka × RabbitMQ (modelos e garantias)
  - Spring Kafka 4.1 (produtor e consumidor básicos)
  - Entrega *at-least-once* e consumidor idempotente
  - Esquemas e evolução
  - AsyncAPI para documentar eventos
  - CloudEvents (revisão)
- **Diagramas:** fluxo de `pedido.pago` para os consumidores (estoque, notificações, parceiros).
- **Código:** publicar `PedidoPago` no Kafka; consumidor idempotente; trecho AsyncAPI.
- **Referências:** https://docs.spring.io/spring-kafka/reference/ · https://www.asyncapi.com/docs · https://martinfowler.com/articles/201701-event-driven.html

#### 30.04 · Padrão Outbox
`Avançado` · `Essencial`
- **Por que importa:** "salvar no banco e publicar o evento" sem Outbox perde ou duplica eventos (*dual write*). É a base para webhooks e eventos confiáveis.
- **Pré-requisitos:** 30.03, 10.03, 19.03
- **Tópicos:**
  - O problema do *dual write*
  - Tabela de *outbox* na mesma transação
  - *Relay* por *polling* (`SKIP LOCKED`) × CDC (Debezium)
  - Ordenação por agregado; limpeza
  - Spring Modulith Event Publication Registry como implementação
  - Consumidores idempotentes; *inbox pattern*
- **Diagramas:** *dual write* com falha × Outbox (transação → *relay* → *broker* / webhooks).
- **Código:** `EventoOutbox`; gravação no `PedidoService`; `OutboxRelay` agendado alimentando os webhooks (21.03) e o Kafka (30.03).
- **Referências:** https://microservices.io/patterns/data/transactional-outbox.html · https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html · https://docs.spring.io/spring-modulith/reference/events.html

#### 30.05 · Consistência eventual e sagas
`Avançado` · `Importante`
- **Por que importa:** sem transação distribuída, processos como "reservar estoque → cobrar → confirmar" precisam de compensação explícita, e a API precisa comunicar estados intermediários.
- **Pré-requisitos:** 30.04
- **Tópicos:**
  - Consistência eventual e o que o cliente vê (estados `PENDENTE`)
  - Sagas por orquestração × coreografia
  - Compensações (liberar estoque)
  - *Timeouts* de saga
  - Idempotência em cada passo
  - Como expor isso na API (operações, status e webhooks)
  - Por que não usar 2PC
- **Diagramas:** saga do checkout com caminho feliz e compensação.
- **Código:** orquestrador simples da saga de pagamento com estados persistidos.
- **Referências:** https://microservices.io/patterns/data/saga.html · Garcia-Molina & Salem, "Sagas" (1987)

#### 30.06 · gRPC com Spring Boot 4.1
`Avançado` · `Complementar`
- **Por que importa:** para comunicação interna de alto volume, gRPC oferece contrato forte e desempenho. Desde o Boot 4.1 o suporte é oficial.
- **Pré-requisitos:** 30.02, 04.01
- **Tópicos:**
  - Protobuf (evolução de campos)
  - Spring gRPC 1.1: servidor (Netty ou Servlet sobre HTTP/2) e cliente
  - Erros (`Status`, *rich error model*)
  - *Deadlines*; observabilidade
  - REST externo + gRPC interno
  - Testes
- **Diagramas:** Bazar expondo REST para fora e gRPC para o serviço de estoque.
- **Código:** `estoque.proto`; serviço e cliente gRPC.
- **Referências:** https://docs.spring.io/spring-grpc/reference/ · https://grpc.io/docs/languages/java/ · https://protobuf.dev/programming-guides/proto3/

#### 30.07 · GraphQL com Spring for GraphQL
`Avançado` · `Complementar`
- **Por que importa:** para front-ends com telas muito variadas, GraphQL pode substituir dezenas de endpoints sob medida, mas traz outros problemas.
- **Pré-requisitos:** 04.01, 10.04
- **Tópicos:**
  - *Schema*, *queries*, *mutations*; `@QueryMapping`, `@SchemaMapping`
  - `@BatchMapping` contra N+1
  - Erros
  - Limites de profundidade e complexidade
  - Coexistência com REST
- **Diagramas:** uma *query* GraphQL resolvida em vários *resolvers*.
- **Código:** *schema* de catálogo e *controller* com `@BatchMapping`.
- **Referências:** https://docs.spring.io/spring-graphql/reference/ · https://graphql.org/learn/

---

# Parte 16: Projeto final, o Bazar

> Cada página consolida o que foi construído aos poucos nas partes anteriores, agora como um projeto único e coerente. Detalhes em `PROJETO-FINAL.md`.

## M31: Construindo o Bazar do zero à produção

#### 31.01 · Visão, requisitos e decisões arquiteturais
`Avançado` · `Essencial`
- **Por que importa:** projetos profissionais começam por requisitos (inclusive os não funcionais) e decisões registradas, não pelo código.
- **Pré-requisitos:** todas as Partes 1–15 essenciais.
- **Tópicos:**
  - Requisitos funcionais e não funcionais (SLOs: disponibilidade e p99 por endpoint)
  - Módulos; ADRs (versionamento por caminho, paginação *keyset*, Outbox, monólito modular)
- **Diagramas:** C4 (contexto e contêineres) do Bazar.
- **Código:** estrutura do repositório e ADRs.
- **Referências:** https://c4model.com/ · https://adr.github.io/

#### 31.02 · O contrato primeiro
`Avançado` · `Essencial`
- **Por que importa:** aplica o API-first de ponta a ponta.
- **Pré-requisitos:** 31.01, 17.03, 17.04
- **Tópicos:**
  - `bazar-api.yaml` completo
  - Revisão com o guia de estilo; Spectral; *mock* com Prism
  - Geração de interfaces
- **Diagramas:** mapa de endpoints por módulo.
- **Código:** contrato completo, `.spectral.yaml`, configuração do gerador.
- **Referências:** OpenAPI 3.1 · Spectral

#### 31.03 · Catálogo
`Avançado` · `Essencial`
- **Por que importa:** consolida CRUD, filtros, *keyset*, PATCH, ETag, *cache* e upload em um módulo real.
- **Pré-requisitos:** 31.02
- **Tópicos:**
  - Categorias, produtos e imagens
  - Busca e respostas parciais
  - Cache HTTP e Redis
  - Testes de todas as camadas
- **Diagramas:** componentes do módulo.
- **Código:** módulo `catalogo` completo.
- **Referências:** páginas de origem.

#### 31.04 · Clientes
`Avançado` · `Essencial`
- **Por que importa:** consolida dados sensíveis, BOLA, validação customizada e segurança essencial.
- **Pré-requisitos:** 31.03
- **Tópicos:**
  - Cadastro e endereços
  - Mascaramento; JWT e escopos
  - Testes de autorização
- **Diagramas:** nenhum.
- **Código:** módulo `clientes`.
- **Referências:** páginas de origem.

#### 31.05 · Estoque, frete e pedidos
`Avançado` · `Essencial`
- **Por que importa:** é o coração transacional, com concorrência, idempotência e máquina de estados.
- **Pré-requisitos:** 31.04
- **Tópicos:**
  - Reserva atômica; `If-Match`
  - Cotação com HTTP interface e *fallback*
  - `POST /pedidos` idempotente; cancelamento; listagem por cursor
- **Diagramas:** sequência do checkout.
- **Código:** módulos `estoque`, `frete` e `pedidos`.
- **Referências:** páginas de origem.

#### 31.06 · Pagamentos e reembolsos
`Avançado` · `Essencial`
- **Por que importa:** integração externa com dinheiro envolvido, que exige todos os padrões de resiliência e idempotência.
- **Pré-requisitos:** 31.05
- **Tópicos:**
  - Cliente PagaFácil com *timeouts*, *retry* seguro e *circuit breaker*
  - Webhook recebido (assinatura e deduplicação)
  - Reembolso assíncrono (202)
  - Saga do pagamento
- **Diagramas:** sequência completa pagamento → webhook → `PAGO`.
- **Código:** módulo `pagamentos`; cenários WireMock de falha.
- **Referências:** páginas de origem.

#### 31.07 · Eventos, Outbox, webhooks e tempo real
`Avançado` · `Essencial`
- **Por que importa:** garante que nenhum evento de pedido se perca e que parceiros e clientes sejam notificados de forma confiável.
- **Pré-requisitos:** 31.06
- **Tópicos:**
  - Outbox + *relay*
  - Assinaturas de webhook e entregas com *retries*
  - SSE de acompanhamento do pedido
  - (Opcional) Kafka
- **Diagramas:** fluxo do evento `pedido.pago` desde a transação até o parceiro e o navegador.
- **Código:** módulo `integracoes`.
- **Referências:** páginas de origem.

#### 31.08 · Relatórios assíncronos e exportação
`Avançado` · `Importante`
- **Por que importa:** consolida *jobs* em segundo plano, operações longas e *streaming*.
- **Pré-requisitos:** 31.07
- **Tópicos:**
  - `POST /relatorios/vendas` → 202
  - Processamento agendado com `SKIP LOCKED`
  - Download CSV com Range
- **Diagramas:** estados da operação.
- **Código:** módulo `relatorios`.
- **Referências:** páginas de origem.

#### 31.09 · Evoluindo: v2 do produto e depreciação da v1
`Avançado` · `Importante`
- **Por que importa:** exercita o ciclo de vida completo de uma mudança incompatível.
- **Pré-requisitos:** 31.08
- **Tópicos:**
  - v2 com `Dinheiro`
  - Versionamento nativo
  - Headers `Deprecation`/`Sunset`
  - Métricas de uso da v1
  - `oasdiff` no CI
  - Plano de *sunset*
- **Diagramas:** linha do tempo v1/v2.
- **Código:** `ProdutoV2Controller`, configuração de depreciação.
- **Referências:** páginas de origem.

#### 31.10 · Observabilidade, carga e deploy
`Avançado` · `Essencial`
- **Por que importa:** uma API só está pronta quando é operável: medida, observada e implantada sem *downtime*.
- **Pré-requisitos:** 31.09
- **Tópicos:**
  - *Dashboards* RED e de negócio; alertas de SLO
  - Teste de carga do checkout e ajustes (*pool*, *cache*)
  - Imagem, manifestos Kubernetes e pipeline CI/CD
- **Diagramas:** topologia de produção.
- **Código:** `compose.yaml` de observabilidade, script k6, `Dockerfile`, manifestos e workflow.
- **Referências:** páginas de origem.

#### 31.11 · Revisão final: o checklist da API sênior
`Avançado` · `Essencial`
- **Por que importa:** fecha o aprendizado com uma revisão crítica do próprio projeto, como seria feito em uma *API review* real.
- **Pré-requisitos:** 31.10
- **Tópicos:**
  - Passar o Bazar pelo checklist do Apêndice A
  - Débitos conscientes; próximos passos de estudo
- **Diagramas:** nenhum.
- **Código:** nenhum.
- **Referências:** Apêndice A.

---

# Apêndices

## M32: Material de consulta

#### 32.01 · Apêndice A: Checklist de revisão de API
`Intermediário` · `Essencial`
- **Por que importa:** é a ferramenta prática para revisar qualquer API (a própria ou de outro time) em minutos.
- **Pré-requisitos:** nenhum (cada item aponta para a página correspondente).
- **Tópicos:** checklist por categoria (recursos e nomes, métodos e status, erros, paginação, tipos de dados, evolução, *cache*/concorrência, idempotência, segurança, observabilidade, operação), com o link de cada item.
- **Diagramas:** nenhum.
- **Código:** nenhum.
- **Referências:** guias Microsoft, Google e Zalando.

#### 32.02 · Apêndice B: Referência rápida de status codes, métodos e headers
`Básico` · `Essencial`
- **Por que importa:** é a consulta rápida do dia a dia.
- **Pré-requisitos:** nenhum.
- **Tópicos:** tabelas de métodos (propriedades), status (quando usar no Bazar) e headers (direção e página).
- **Diagramas:** nenhum.
- **Código:** nenhum.
- **Referências:** RFC 9110, RFC 9111, registros da IANA.

#### 32.03 · Apêndice C: Migrando do Spring Boot 3 para o 4
`Intermediário` · `Importante`
- **Por que importa:** o leitor provavelmente trabalha com projetos em Boot 3.x. A migração tem armadilhas específicas para APIs.
- **Pré-requisitos:** 05.01, 06.03
- **Tópicos:**
  - Jackson 2 → 3 (pacotes, *builders*, padrões)
  - *Starters* modularizados
  - Undertow removido
  - `RestTemplate` → `RestClient`
  - Spring Retry → retry nativo
  - Versionamento manual → nativo
  - `MockMvc` → `MockMvcTester`/`RestTestClient`
  - JUnit 4 → JUnit 6
  - Ferramentas (OpenRewrite)
- **Diagramas:** nenhum.
- **Código:** exemplos antes/depois.
- **Referências:** https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide · https://docs.openrewrite.org/

#### 32.04 · Apêndice D: Glossário
`Básico` · `Importante`
- **Por que importa:** reúne em um só lugar os termos em português e inglês usados na doc.
- **Pré-requisitos:** nenhum.
- **Tópicos:** termos com definição curta e a página onde aparecem pela primeira vez.
- **Diagramas:** nenhum.
- **Código:** nenhum.
- **Referências:** —

---

## Fontes gerais consultadas

- RFC 9110 (HTTP Semantics): https://www.rfc-editor.org/rfc/rfc9110
- RFC 9111 (HTTP Caching): https://www.rfc-editor.org/rfc/rfc9111
- RFC 9457 (Problem Details): https://www.rfc-editor.org/rfc/rfc9457
- RFC 10008 (QUERY): https://www.rfc-editor.org/rfc/rfc10008.html
- Dissertação de Roy Fielding: https://ics.uci.edu/~fielding/pubs/dissertation/top.htm
- OpenAPI Specification: https://spec.openapis.org/oas/
- Microsoft REST API Guidelines: https://github.com/microsoft/api-guidelines
- Google API Improvement Proposals: https://google.aip.dev/
- Zalando RESTful API Guidelines: https://opensource.zalando.com/restful-api-guidelines/
- OWASP API Security Top 10 (2023): https://owasp.org/API-Security/
- Spring Framework Reference: https://docs.spring.io/spring-framework/reference/
- Spring Boot Reference: https://docs.spring.io/spring-boot/
- Release notes do Spring Boot 4.0 e 4.1 e do Spring Framework 7.0 e 7.1 (links em `VERSOES.md`)
