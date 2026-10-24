# Fase 1 — Pesquisa e escolha do conteúdo

## Contexto

Vou construir um site de documentação, em **português do Brasil**, para aprender **APIs REST do básico ao avançado com Spring Boot**. O trabalho tem 3 fases, cada uma em uma sessão separada:

1. **Pesquisa e escolha do conteúdo** ← esta fase
2. Construção do site (React + Tailwind + shadcn)
3. Escrita do conteúdo dentro do site

Nesta fase **não escreva código nem conteúdo final**. Seu trabalho é ser o especialista que monta o currículo. Eu não sei o que não sei: você decide o que um desenvolvedor precisa dominar para ser considerado sênior em APIs REST com Spring Boot.

## Sobre mim

- Desenvolvedor Java, conheço o básico de Spring Boot.
- Quero sair do "faço um CRUD" para "projeto e opero APIs de nível profissional".
- Segurança (autenticação, OAuth, Spring Security a fundo) já está em **outra documentação**. Aqui, trate segurança só no essencial para APIs e indique que o aprofundamento está na outra doc.

## O que fazer

### 1. Pesquise

- Confirme as **versões estáveis atuais** de Java, Spring Boot e Spring Framework na documentação oficial, e liste as novidades recentes que afetam APIs (ex.: versionamento nativo de API, `RestClient`, HTTP interfaces, `ProblemDetail`, virtual threads, logs estruturados, retry nativo).
- Consulte fontes de referência: RFCs do HTTP (9110, 9111), RFC 9457 (Problem Details), dissertação de Roy Fielding, OpenAPI Specification, documentação do Spring, guias de design de API reconhecidos (ex.: Microsoft, Google, Zalando).
- Identifique os tópicos que profissionais experientes consideram essenciais e que iniciantes costumam ignorar.

### 2. Monte o currículo

Use a lista abaixo como **ponto de partida mínimo**. Reorganize, divida, junte e, principalmente, **acrescente o que estiver faltando**.

- **Fundamentos de HTTP**: métodos, segurança e idempotência dos métodos, status codes, headers, content negotiation, cookies, HTTP/1.1 vs HTTP/2 vs HTTP/3
- **REST de verdade**: restrições de Fielding, modelo de maturidade de Richardson, HATEOAS, REST vs RPC vs GraphQL vs gRPC vs WebSocket/SSE
- **Design de APIs**: recursos e URIs, nomenclatura, relacionamentos, filtros, ordenação, busca, paginação (offset vs cursor/keyset), respostas parciais, operações em lote, ações que não são CRUD, PUT vs PATCH (JSON Patch e Merge Patch), datas e fusos (ISO 8601), dinheiro, IDs (UUID v7, ULID), padronização de erros (RFC 9457)
- **Ciclo de vida**: versionamento, depreciação (headers `Deprecation`/`Sunset`), compatibilidade retroativa, API-first e contratos OpenAPI, governança e linting de contratos
- **Padrões avançados**: idempotency keys, operações assíncronas (202 + polling), webhooks, cache HTTP (`Cache-Control`, `ETag`, requisições condicionais), concorrência otimista (`If-Match`, 412), upload/download e streaming, compressão, rate limiting
- **Spring MVC por dentro**: `DispatcherServlet`, `HandlerMapping`, `HandlerAdapter`, argument resolvers, `HttpMessageConverter`, configuração do Jackson, filtros vs interceptors
- **Construção com Spring Boot**: estrutura do projeto e camadas, `@RestController`, `ResponseEntity`, DTOs e records, MapStruct, Bean Validation (grupos, validadores customizados), tratamento global de erros com `ProblemDetail`
- **Persistência**: Spring Data JPA para APIs, paginação, projeções, problema N+1, transações, migrations com Flyway
- **Documentação**: springdoc-openapi, exemplos, contrato como fonte da verdade
- **Consumindo APIs**: `RestClient`, `WebClient`, HTTP interfaces (`@HttpExchange`), timeouts, retry, circuit breaker (Resilience4j)
- **Concorrência e reatividade**: `@Async`, virtual threads, SSE, visão geral do WebFlux e quando usar
- **Segurança essencial para APIs**: visão geral, CORS, rate limiting, validação de entrada, Resource Server com JWT (com link para a doc de segurança)
- **Testes**: unitários, `@WebMvcTest` com `MockMvcTester`, `@SpringBootTest`, Testcontainers, RestAssured, testes de contrato
- **Observabilidade**: Actuator, health checks, Micrometer, métricas, tracing com OpenTelemetry, logs estruturados, correlation ID
- **Performance**: pool de conexões, cache (`@Cacheable`, Redis), paginação eficiente, profiling, virtual threads
- **Produção e deploy**: configuração e profiles, Docker, buildpacks, imagem nativa com GraalVM, probes do Kubernetes, graceful shutdown, API Gateway
- **Arquitetura**: monólito modular vs microsserviços, comunicação entre serviços, eventos e mensageria (introdução), padrão Outbox, consistência eventual
- **Projeto final**: uma API completa construída passo a passo aplicando tudo

### 3. Classifique cada página

Para cada página do currículo informe:

- **Nível**: Básico, Intermediário ou Avançado
- **Prioridade**: Essencial, Importante ou Complementar
- **Por que importa**: 1 a 2 frases dizendo que problema real isso resolve
- **Pré-requisitos**: quais páginas devem vir antes
- **Tópicos detalhados**: os subtópicos que a página deve cobrir (seja específico)
- **Diagramas sugeridos**: quais fluxos ou ilustrações a página precisa
- **Exemplos de código**: quais exemplos devem aparecer
- **Referências**: links das fontes oficiais usadas

## Entregáveis

Crie a pasta `planejamento/` com:

1. **`CURRICULO.md`** — o currículo completo: partes → módulos → páginas, com todas as informações acima. Este é o documento central das fases 2 e 3.
2. **`DESCOBERTAS.md`** — lista dos tópicos que você acrescentou além da minha lista, com uma frase explicando por que cada um é importante. Quero saber o que eu não sabia.
3. **`PROJETO-FINAL.md`** — o domínio da API do projeto final (ex.: e-commerce, sistema de pedidos), suas entidades, endpoints e em quais páginas cada parte será construída. Os exemplos de código ao longo da doc devem, sempre que possível, usar esse mesmo domínio.
4. **`VERSOES.md`** — versões de Java, Spring Boot, Spring Framework e principais bibliotecas que serão usadas, com a data da verificação.

## Regras

- Ordem didática: nunca use um conceito antes de ele ser explicado.
- Prefira profundidade a superficialidade, mas marque o que é complementar para eu poder pular.
- Não invente informações; se não tiver certeza de algo, marque como "a verificar".
- Ao terminar, me mostre um resumo do currículo (número de partes, módulos e páginas) e a lista do `DESCOBERTAS.md`, e **espere minha aprovação**. Vou pedir ajustes antes de seguir.
