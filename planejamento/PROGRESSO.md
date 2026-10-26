# Progresso do conteúdo

> Status de cada página do site. A fase 3 atualiza este arquivo ao escrever cada página.
> Status possíveis: **esqueleto** (gerado na fase 2) · **rascunho** (escrita em andamento) · **revisão** (escrita, falta revisar) · **pronta**.
> O status também fica no frontmatter de cada MDX (`status:`), e o site mostra um aviso nas páginas que ainda não estão prontas.

**Total:** 143 páginas · **esqueleto:** 70 · **rascunho:** 0 · **revisão:** 1 · **pronta:** 72

## Parte 1: Começando e fundamentos de HTTP

### M01: Começando

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 01.01 | Como usar esta documentação | basico | essencial | `src/content/01-comecando-e-fundamentos-de-http/01-01-como-usar-esta-documentacao.mdx` | pronta |
| 01.02 | Preparando o ambiente | basico | essencial | `src/content/01-comecando-e-fundamentos-de-http/01-02-preparando-o-ambiente.mdx` | pronta |

### M02: HTTP essencial

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 02.01 | Anatomia de uma mensagem HTTP | basico | essencial | `src/content/01-comecando-e-fundamentos-de-http/02-01-anatomia-de-uma-mensagem-http.mdx` | pronta |
| 02.02 | Métodos HTTP: semântica, segurança e idempotência | basico | essencial | `src/content/01-comecando-e-fundamentos-de-http/02-02-metodos-http-semantica-seguranca-e-idempotencia.mdx` | pronta |
| 02.03 | Status codes em profundidade | basico | essencial | `src/content/01-comecando-e-fundamentos-de-http/02-03-status-codes-em-profundidade.mdx` | pronta |
| 02.04 | Headers essenciais | basico | essencial | `src/content/01-comecando-e-fundamentos-de-http/02-04-headers-essenciais.mdx` | pronta |
| 02.05 | Content negotiation e media types | basico | essencial | `src/content/01-comecando-e-fundamentos-de-http/02-05-content-negotiation-e-media-types.mdx` | pronta |
| 02.06 | JSON para APIs | basico | essencial | `src/content/01-comecando-e-fundamentos-de-http/02-06-json-para-apis.mdx` | pronta |
| 02.07 | Cookies, estado e sessões | intermediario | importante | `src/content/01-comecando-e-fundamentos-de-http/02-07-cookies-estado-e-sessoes.mdx` | pronta |
| 02.08 | HTTP/1.1, HTTP/2 e HTTP/3 | intermediario | importante | `src/content/01-comecando-e-fundamentos-de-http/02-08-http-1-1-http-2-e-http-3.mdx` | pronta |

## Parte 2: REST e estilos de API

### M03: REST de verdade

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 03.01 | As restrições de Fielding | intermediario | essencial | `src/content/02-rest-e-estilos-de-api/03-01-as-restricoes-de-fielding.mdx` | pronta |
| 03.02 | Recursos, representações e a interface uniforme | intermediario | essencial | `src/content/02-rest-e-estilos-de-api/03-02-recursos-representacoes-e-a-interface-uniforme.mdx` | pronta |
| 03.03 | O modelo de maturidade de Richardson | basico | importante | `src/content/02-rest-e-estilos-de-api/03-03-o-modelo-de-maturidade-de-richardson.mdx` | pronta |
| 03.04 | HATEOAS e hipermídia | intermediario | complementar | `src/content/02-rest-e-estilos-de-api/03-04-hateoas-e-hipermidia.mdx` | pronta |

### M04: Estilos de API

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 04.01 | REST, RPC, GraphQL, gRPC, WebSocket, SSE e webhooks: quando usar cada um | intermediario | importante | `src/content/02-rest-e-estilos-de-api/04-01-rest-rpc-graphql-grpc-websocket-sse-e-webhooks-quando-usar.mdx` | pronta |

## Parte 3: Construindo APIs com Spring Boot

### M05: Primeiro projeto

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 05.01 | Spring Boot 4 em uma página | basico | essencial | `src/content/03-construindo-apis-com-spring-boot/05-01-spring-boot-4-em-uma-pagina.mdx` | pronta |
| 05.02 | Criando o projeto Bazar | basico | essencial | `src/content/03-construindo-apis-com-spring-boot/05-02-criando-o-projeto-bazar.mdx` | pronta |
| 05.03 | Primeiro endpoint | basico | essencial | `src/content/03-construindo-apis-com-spring-boot/05-03-primeiro-endpoint.mdx` | pronta |
| 05.04 | Um CRUD com status corretos | basico | essencial | `src/content/03-construindo-apis-com-spring-boot/05-04-um-crud-com-status-corretos.mdx` | pronta |

### M06: Modelando entrada e saída

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 06.01 | DTOs com records e por que não expor entidades | basico | essencial | `src/content/03-construindo-apis-com-spring-boot/06-01-dtos-com-records-e-por-que-nao-expor-entidades.mdx` | pronta |
| 06.02 | Mapeamento: manual ou MapStruct | basico | importante | `src/content/03-construindo-apis-com-spring-boot/06-02-mapeamento-manual-ou-mapstruct.mdx` | pronta |
| 06.03 | Jackson 3 no Spring Boot 4 | intermediario | essencial | `src/content/03-construindo-apis-com-spring-boot/06-03-jackson-3-no-spring-boot-4.mdx` | pronta |

### M07: Validação

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 07.01 | Bean Validation essencial | basico | essencial | `src/content/03-construindo-apis-com-spring-boot/07-01-bean-validation-essencial.mdx` | pronta |
| 07.02 | Validação avançada | intermediario | importante | `src/content/03-construindo-apis-com-spring-boot/07-02-validacao-avancada.mdx` | pronta |

### M08: Tratamento de erros

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 08.01 | Problem Details (RFC 9457) | basico | essencial | `src/content/03-construindo-apis-com-spring-boot/08-01-problem-details-rfc-9457.mdx` | pronta |
| 08.02 | ProblemDetail no Spring: tratamento global de erros | intermediario | essencial | `src/content/03-construindo-apis-com-spring-boot/08-02-problemdetail-no-spring-tratamento-global-de-erros.mdx` | pronta |

### M09: Spring MVC por dentro

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 09.01 | O caminho de uma requisição no Spring MVC | intermediario | essencial | `src/content/03-construindo-apis-com-spring-boot/09-01-o-caminho-de-uma-requisicao-no-spring-mvc.mdx` | pronta |
| 09.02 | Argument resolvers e return value handlers customizados | avancado | complementar | `src/content/03-construindo-apis-com-spring-boot/09-02-argument-resolvers-e-return-value-handlers-customizados.mdx` | pronta |
| 09.03 | HttpMessageConverter e a serialização por dentro | intermediario | importante | `src/content/03-construindo-apis-com-spring-boot/09-03-httpmessageconverter-e-a-serializacao-por-dentro.mdx` | pronta |
| 09.04 | Filtros, interceptors e AOP: quando usar cada um | intermediario | importante | `src/content/03-construindo-apis-com-spring-boot/09-04-filtros-interceptors-e-aop-quando-usar-cada-um.mdx` | pronta |
| 09.05 | Functional endpoints (RouterFunction) | intermediario | complementar | `src/content/03-construindo-apis-com-spring-boot/09-05-functional-endpoints-routerfunction.mdx` | pronta |

## Parte 4: Persistência para APIs

### M10: Spring Data JPA a serviço da API

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 10.01 | Spring Data JPA essencial para APIs | basico | essencial | `src/content/04-persistencia-para-apis/10-01-spring-data-jpa-essencial-para-apis.mdx` | pronta |
| 10.02 | Migrations com Flyway | basico | essencial | `src/content/04-persistencia-para-apis/10-02-migrations-com-flyway.mdx` | pronta |
| 10.03 | Transações na camada de serviço | intermediario | essencial | `src/content/04-persistencia-para-apis/10-03-transacoes-na-camada-de-servico.mdx` | pronta |
| 10.04 | O problema N+1 e estratégias de fetch | intermediario | essencial | `src/content/04-persistencia-para-apis/10-04-o-problema-n-1-e-estrategias-de-fetch.mdx` | pronta |
| 10.05 | Projeções e consultas para leitura | intermediario | importante | `src/content/04-persistencia-para-apis/10-05-projecoes-e-consultas-para-leitura.mdx` | pronta |
| 10.06 | Concorrência no banco: locking otimista e pessimista | intermediario | essencial | `src/content/04-persistencia-para-apis/10-06-concorrencia-no-banco-locking-otimista-e-pessimista.mdx` | pronta |

## Parte 5: Testes de API

### M11: Testando APIs Spring Boot

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 11.01 | Estratégia de testes para APIs | basico | essencial | `src/content/05-testes-de-api/11-01-estrategia-de-testes-para-apis.mdx` | pronta |
| 11.02 | Testes unitários de domínio e serviços | basico | essencial | `src/content/05-testes-de-api/11-02-testes-unitarios-de-dominio-e-servicos.mdx` | pronta |
| 11.03 | Testando a camada web: @WebMvcTest, MockMvcTester e RestTestClient | intermediario | essencial | `src/content/05-testes-de-api/11-03-testando-a-camada-web-webmvctest-mockmvctester.mdx` | pronta |
| 11.04 | Testes de integração com @SpringBootTest e Testcontainers | intermediario | essencial | `src/content/05-testes-de-api/11-04-testes-de-integracao-com-springboottest-e-testcontainers.mdx` | pronta |
| 11.05 | Testes ponta a ponta com RestAssured | intermediario | importante | `src/content/05-testes-de-api/11-05-testes-ponta-a-ponta-com-restassured.mdx` | pronta |

## Parte 6: Design de APIs profissional

### M12: Recursos e URIs

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 12.01 | Modelando recursos a partir do domínio | intermediario | essencial | `src/content/06-design-de-apis-profissional/12-01-modelando-recursos-a-partir-do-dominio.mdx` | pronta |
| 12.02 | Convenções de nomenclatura | basico | essencial | `src/content/06-design-de-apis-profissional/12-02-convencoes-de-nomenclatura.mdx` | pronta |
| 12.03 | Relacionamentos e sub-recursos | intermediario | essencial | `src/content/06-design-de-apis-profissional/12-03-relacionamentos-e-sub-recursos.mdx` | pronta |
| 12.04 | Ações que não são CRUD e máquinas de estado | intermediario | essencial | `src/content/06-design-de-apis-profissional/12-04-acoes-que-nao-sao-crud-e-maquinas-de-estado.mdx` | pronta |

### M13: Consultando coleções

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 13.01 | Filtros e busca | intermediario | essencial | `src/content/06-design-de-apis-profissional/13-01-filtros-e-busca.mdx` | pronta |
| 13.02 | Ordenação | basico | importante | `src/content/06-design-de-apis-profissional/13-02-ordenacao.mdx` | pronta |
| 13.03 | Paginação por offset | basico | essencial | `src/content/06-design-de-apis-profissional/13-03-paginacao-por-offset.mdx` | pronta |
| 13.04 | Paginação por cursor (keyset) | intermediario | essencial | `src/content/06-design-de-apis-profissional/13-04-paginacao-por-cursor-keyset.mdx` | pronta |
| 13.05 | Respostas parciais e expansão | avancado | complementar | `src/content/06-design-de-apis-profissional/13-05-respostas-parciais-e-expansao.mdx` | pronta |

### M14: Escrita e modificação

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 14.01 | Criação com POST (e PUT com id do cliente) | basico | essencial | `src/content/06-design-de-apis-profissional/14-01-criacao-com-post-e-put-com-id-do-cliente.mdx` | pronta |
| 14.02 | PUT × PATCH: JSON Merge Patch e JSON Patch | intermediario | essencial | `src/content/06-design-de-apis-profissional/14-02-put-patch-json-merge-patch-e-json-patch.mdx` | pronta |
| 14.03 | DELETE, soft delete e arquivamento | basico | importante | `src/content/06-design-de-apis-profissional/14-03-delete-soft-delete-e-arquivamento.mdx` | pronta |
| 14.04 | Operações em lote | avancado | importante | `src/content/06-design-de-apis-profissional/14-04-operacoes-em-lote.mdx` | pronta |

### M15: Tipos de dados no contrato

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 15.01 | Datas, horas e fusos horários | intermediario | essencial | `src/content/06-design-de-apis-profissional/15-01-datas-horas-e-fusos-horarios.mdx` | pronta |
| 15.02 | Dinheiro e números decimais | intermediario | essencial | `src/content/06-design-de-apis-profissional/15-02-dinheiro-e-numeros-decimais.mdx` | pronta |
| 15.03 | Identificadores: sequenciais, UUID v7 e ULID | intermediario | importante | `src/content/06-design-de-apis-profissional/15-03-identificadores-sequenciais-uuid-v7-e-ulid.mdx` | pronta |
| 15.04 | Enums, evolução e tolerant reader | intermediario | importante | `src/content/06-design-de-apis-profissional/15-04-enums-evolucao-e-tolerant-reader.mdx` | pronta |
| 15.05 | Internacionalização na API | intermediario | complementar | `src/content/06-design-de-apis-profissional/15-05-internacionalizacao-na-api.mdx` | pronta |

### M16: Consistência e guia de estilo

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 16.01 | Projetando erros: catálogo, códigos e decisões difíceis | intermediario | essencial | `src/content/06-design-de-apis-profissional/16-01-projetando-erros-catalogo-codigos-e-decisoes-dificeis.mdx` | pronta |
| 16.02 | Escrevendo o guia de estilo da sua API | avancado | importante | `src/content/06-design-de-apis-profissional/16-02-escrevendo-o-guia-de-estilo-da-sua-api.mdx` | pronta |

## Parte 7: Contratos, documentação e ciclo de vida

### M17: OpenAPI e contrato

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 17.01 | A especificação OpenAPI | intermediario | essencial | `src/content/07-contratos-documentacao-e-ciclo-de-vida/17-01-a-especificacao-openapi.mdx` | pronta |
| 17.02 | springdoc-openapi: documentação a partir do código | basico | essencial | `src/content/07-contratos-documentacao-e-ciclo-de-vida/17-02-springdoc-openapi-documentacao-a-partir-do-codigo.mdx` | pronta |
| 17.03 | API-first: o contrato como fonte da verdade | avancado | importante | `src/content/07-contratos-documentacao-e-ciclo-de-vida/17-03-api-first-o-contrato-como-fonte-da-verdade.mdx` | pronta |
| 17.04 | Governança: linting e detecção de breaking changes | avancado | importante | `src/content/07-contratos-documentacao-e-ciclo-de-vida/17-04-governanca-linting-e-deteccao-de-breaking-changes.mdx` | pronta |
| 17.05 | Documentação além da referência | intermediario | complementar | `src/content/07-contratos-documentacao-e-ciclo-de-vida/17-05-documentacao-alem-da-referencia.mdx` | pronta |

### M18: Evolução e versionamento

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 18.01 | Compatibilidade retroativa: o que quebra um cliente | intermediario | essencial | `src/content/07-contratos-documentacao-e-ciclo-de-vida/18-01-compatibilidade-retroativa-o-que-quebra-um-cliente.mdx` | pronta |
| 18.02 | Estratégias de versionamento | intermediario | essencial | `src/content/07-contratos-documentacao-e-ciclo-de-vida/18-02-estrategias-de-versionamento.mdx` | pronta |
| 18.03 | Versionamento nativo no Spring Framework 7 | intermediario | essencial | `src/content/07-contratos-documentacao-e-ciclo-de-vida/18-03-versionamento-nativo-no-spring-framework-7.mdx` | pronta |
| 18.04 | Depreciação e desligamento de APIs | avancado | importante | `src/content/07-contratos-documentacao-e-ciclo-de-vida/18-04-depreciacao-e-desligamento-de-apis.mdx` | pronta |
| 18.05 | Testes de contrato | avancado | importante | `src/content/07-contratos-documentacao-e-ciclo-de-vida/18-05-testes-de-contrato.mdx` | pronta |

## Parte 8: Concorrência e execução assíncrona

### M19: Threads, virtual threads e tarefas em segundo plano

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 19.01 | O modelo thread-per-request e seus limites | intermediario | essencial | `src/content/08-concorrencia-e-execucao-assincrona/19-01-o-modelo-thread-per-request-e-seus-limites.mdx` | revisao |
| 19.02 | Virtual threads em APIs Spring | intermediario | essencial | `src/content/08-concorrencia-e-execucao-assincrona/19-02-virtual-threads-em-apis-spring.mdx` | esqueleto |
| 19.03 | @Async, @Scheduled e tarefas em segundo plano | intermediario | essencial | `src/content/08-concorrencia-e-execucao-assincrona/19-03-async-scheduled-e-tarefas-em-segundo-plano.mdx` | esqueleto |
| 19.04 | Server-Sent Events (e uma visão de WebSocket) | intermediario | importante | `src/content/08-concorrencia-e-execucao-assincrona/19-04-server-sent-events-e-uma-visao-de-websocket.mdx` | esqueleto |
| 19.05 | WebFlux: visão geral e quando usar | avancado | complementar | `src/content/08-concorrencia-e-execucao-assincrona/19-05-webflux-visao-geral-e-quando-usar.mdx` | esqueleto |
| 19.06 | Scoped values e concorrência estruturada | avancado | complementar | `src/content/08-concorrencia-e-execucao-assincrona/19-06-scoped-values-e-concorrencia-estruturada.mdx` | esqueleto |

## Parte 9: Padrões avançados de HTTP

### M20: Cache e concorrência no HTTP

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 20.01 | Cache HTTP (RFC 9111) | intermediario | essencial | `src/content/09-padroes-avancados-de-http/20-01-cache-http-rfc-9111.mdx` | esqueleto |
| 20.02 | ETag e requisições condicionais | intermediario | essencial | `src/content/09-padroes-avancados-de-http/20-02-etag-e-requisicoes-condicionais.mdx` | esqueleto |
| 20.03 | Concorrência otimista com If-Match (412 e 428) | avancado | essencial | `src/content/09-padroes-avancados-de-http/20-03-concorrencia-otimista-com-if-match-412-e-428.mdx` | esqueleto |

### M21: Confiabilidade nas escritas

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 21.01 | Idempotency keys | avancado | essencial | `src/content/09-padroes-avancados-de-http/21-01-idempotency-keys.mdx` | esqueleto |
| 21.02 | Operações assíncronas e de longa duração (202 + polling) | avancado | essencial | `src/content/09-padroes-avancados-de-http/21-02-operacoes-assincronas-e-de-longa-duracao-202-polling.mdx` | esqueleto |
| 21.03 | Webhooks: enviando e recebendo | avancado | essencial | `src/content/09-padroes-avancados-de-http/21-03-webhooks-enviando-e-recebendo.mdx` | esqueleto |

### M22: Conteúdo e transferência

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 22.01 | Upload de arquivos | intermediario | importante | `src/content/09-padroes-avancados-de-http/22-01-upload-de-arquivos.mdx` | esqueleto |
| 22.02 | Download, streaming e Range | intermediario | importante | `src/content/09-padroes-avancados-de-http/22-02-download-streaming-e-range.mdx` | esqueleto |
| 22.03 | Compressão | intermediario | complementar | `src/content/09-padroes-avancados-de-http/22-03-compressao.mdx` | esqueleto |

### M23: Proteção da API

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 23.01 | Rate limiting e quotas | avancado | essencial | `src/content/09-padroes-avancados-de-http/23-01-rate-limiting-e-quotas.mdx` | esqueleto |
| 23.02 | Limites de payload e proteção contra abuso | intermediario | importante | `src/content/09-padroes-avancados-de-http/23-02-limites-de-payload-e-protecao-contra-abuso.mdx` | esqueleto |

## Parte 10: Consumindo outras APIs

### M24: Clientes HTTP no Spring

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 24.01 | Panorama dos clientes HTTP no Spring | basico | essencial | `src/content/10-consumindo-outras-apis/24-01-panorama-dos-clientes-http-no-spring.mdx` | esqueleto |
| 24.02 | RestClient a fundo | intermediario | essencial | `src/content/10-consumindo-outras-apis/24-02-restclient-a-fundo.mdx` | esqueleto |
| 24.03 | HTTP interfaces (@HttpExchange) e o registro de clientes | intermediario | essencial | `src/content/10-consumindo-outras-apis/24-03-http-interfaces-httpexchange-e-o-registro-de-clientes.mdx` | esqueleto |
| 24.04 | WebClient em aplicações MVC | intermediario | complementar | `src/content/10-consumindo-outras-apis/24-04-webclient-em-aplicacoes-mvc.mdx` | esqueleto |
| 24.05 | Testando clientes HTTP | intermediario | essencial | `src/content/10-consumindo-outras-apis/24-05-testando-clientes-http.mdx` | esqueleto |

### M25: Resiliência

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 25.01 | Timeouts e orçamento de tempo | intermediario | essencial | `src/content/10-consumindo-outras-apis/25-01-timeouts-e-orcamento-de-tempo.mdx` | esqueleto |
| 25.02 | Retry: quando é seguro e como fazer | avancado | essencial | `src/content/10-consumindo-outras-apis/25-02-retry-quando-e-seguro-e-como-fazer.mdx` | esqueleto |
| 25.03 | Circuit breaker, bulkhead e limites de concorrência | avancado | essencial | `src/content/10-consumindo-outras-apis/25-03-circuit-breaker-bulkhead-e-limites-de-concorrencia.mdx` | esqueleto |
| 25.04 | Fallbacks e degradação graciosa | avancado | importante | `src/content/10-consumindo-outras-apis/25-04-fallbacks-e-degradacao-graciosa.mdx` | esqueleto |

## Parte 11: Segurança essencial para APIs

### M26: O essencial de segurança para quem constrói APIs

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 26.01 | Panorama: OWASP API Security Top 10 | intermediario | essencial | `src/content/11-seguranca-essencial-para-apis/26-01-panorama-owasp-api-security-top-10.mdx` | esqueleto |
| 26.02 | CORS | basico | essencial | `src/content/11-seguranca-essencial-para-apis/26-02-cors.mdx` | esqueleto |
| 26.03 | Autorização por objeto, exposição de dados e validação de entrada | intermediario | essencial | `src/content/11-seguranca-essencial-para-apis/26-03-autorizacao-por-objeto-exposicao-de-dados-e-validacao.mdx` | esqueleto |
| 26.04 | Resource Server com JWT (o essencial) | intermediario | essencial | `src/content/11-seguranca-essencial-para-apis/26-04-resource-server-com-jwt-o-essencial.mdx` | esqueleto |
| 26.05 | Transporte, segredos, headers de segurança e SSRF | intermediario | importante | `src/content/11-seguranca-essencial-para-apis/26-05-transporte-segredos-headers-de-seguranca-e-ssrf.mdx` | esqueleto |

## Parte 12: Observabilidade

### M27: Enxergando a API em produção

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 27.01 | Fundamentos de observabilidade | intermediario | essencial | `src/content/12-observabilidade/27-01-fundamentos-de-observabilidade.mdx` | esqueleto |
| 27.02 | Spring Boot Actuator | basico | essencial | `src/content/12-observabilidade/27-02-spring-boot-actuator.mdx` | esqueleto |
| 27.03 | Health checks: liveness, readiness e indicadores | intermediario | essencial | `src/content/12-observabilidade/27-03-health-checks-liveness-readiness-e-indicadores.mdx` | esqueleto |
| 27.04 | Logs estruturados | intermediario | essencial | `src/content/12-observabilidade/27-04-logs-estruturados.mdx` | esqueleto |
| 27.05 | Métricas com Micrometer | intermediario | essencial | `src/content/12-observabilidade/27-05-metricas-com-micrometer.mdx` | esqueleto |
| 27.06 | Tracing distribuído com OpenTelemetry | avancado | essencial | `src/content/12-observabilidade/27-06-tracing-distribuido-com-opentelemetry.mdx` | esqueleto |
| 27.07 | Correlation ID e contexto de requisição | intermediario | importante | `src/content/12-observabilidade/27-07-correlation-id-e-contexto-de-requisicao.mdx` | esqueleto |

## Parte 13: Performance

### M28: Medir, entender e otimizar

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 28.01 | Medindo antes de otimizar: testes de carga | intermediario | essencial | `src/content/13-performance/28-01-medindo-antes-de-otimizar-testes-de-carga.mdx` | esqueleto |
| 28.02 | Pool de conexões (HikariCP) | intermediario | essencial | `src/content/13-performance/28-02-pool-de-conexoes-hikaricp.mdx` | esqueleto |
| 28.03 | Cache na aplicação: @Cacheable, Caffeine e Redis | intermediario | importante | `src/content/13-performance/28-03-cache-na-aplicacao-cacheable-caffeine-e-redis.mdx` | esqueleto |
| 28.04 | Consultas e paginação eficientes | avancado | importante | `src/content/13-performance/28-04-consultas-e-paginacao-eficientes.mdx` | esqueleto |
| 28.05 | Profiling da JVM | avancado | complementar | `src/content/13-performance/28-05-profiling-da-jvm.mdx` | esqueleto |

## Parte 14: Produção e deploy

### M29: Da máquina do desenvolvedor para produção

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 29.01 | Configuração externa e profiles | basico | essencial | `src/content/14-producao-e-deploy/29-01-configuracao-externa-e-profiles.mdx` | esqueleto |
| 29.02 | Containers: Dockerfile, layered jars e buildpacks | intermediario | essencial | `src/content/14-producao-e-deploy/29-02-containers-dockerfile-layered-jars-e-buildpacks.mdx` | esqueleto |
| 29.03 | Imagem nativa com GraalVM | avancado | complementar | `src/content/14-producao-e-deploy/29-03-imagem-nativa-com-graalvm.mdx` | esqueleto |
| 29.04 | Kubernetes para APIs: probes, graceful shutdown e rollouts | avancado | importante | `src/content/14-producao-e-deploy/29-04-kubernetes-para-apis-probes-graceful-shutdown-e-rollouts.mdx` | esqueleto |
| 29.05 | API Gateway e BFF | avancado | importante | `src/content/14-producao-e-deploy/29-05-api-gateway-e-bff.mdx` | esqueleto |
| 29.06 | CI/CD para APIs | intermediario | importante | `src/content/14-producao-e-deploy/29-06-ci-cd-para-apis.mdx` | esqueleto |

## Parte 15: Arquitetura

### M30: APIs no contexto da arquitetura

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 30.01 | Monólito modular com Spring Modulith | avancado | importante | `src/content/15-arquitetura/30-01-monolito-modular-com-spring-modulith.mdx` | esqueleto |
| 30.02 | Microsserviços: quando (não) usar e como se comunicam | avancado | importante | `src/content/15-arquitetura/30-02-microsservicos-quando-nao-usar-e-como-se-comunicam.mdx` | esqueleto |
| 30.03 | Eventos e mensageria: introdução | avancado | importante | `src/content/15-arquitetura/30-03-eventos-e-mensageria-introducao.mdx` | esqueleto |
| 30.04 | Padrão Outbox | avancado | essencial | `src/content/15-arquitetura/30-04-padrao-outbox.mdx` | esqueleto |
| 30.05 | Consistência eventual e sagas | avancado | importante | `src/content/15-arquitetura/30-05-consistencia-eventual-e-sagas.mdx` | esqueleto |
| 30.06 | gRPC com Spring Boot 4.1 | avancado | complementar | `src/content/15-arquitetura/30-06-grpc-com-spring-boot-4-1.mdx` | esqueleto |
| 30.07 | GraphQL com Spring for GraphQL | avancado | complementar | `src/content/15-arquitetura/30-07-graphql-com-spring-for-graphql.mdx` | esqueleto |

## Parte 16: Projeto final, o Bazar

### M31: Construindo o Bazar do zero à produção

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 31.01 | Visão, requisitos e decisões arquiteturais | avancado | essencial | `src/content/16-projeto-final-o-bazar/31-01-visao-requisitos-e-decisoes-arquiteturais.mdx` | esqueleto |
| 31.02 | O contrato primeiro | avancado | essencial | `src/content/16-projeto-final-o-bazar/31-02-o-contrato-primeiro.mdx` | esqueleto |
| 31.03 | Catálogo | avancado | essencial | `src/content/16-projeto-final-o-bazar/31-03-catalogo.mdx` | esqueleto |
| 31.04 | Clientes | avancado | essencial | `src/content/16-projeto-final-o-bazar/31-04-clientes.mdx` | esqueleto |
| 31.05 | Estoque, frete e pedidos | avancado | essencial | `src/content/16-projeto-final-o-bazar/31-05-estoque-frete-e-pedidos.mdx` | esqueleto |
| 31.06 | Pagamentos e reembolsos | avancado | essencial | `src/content/16-projeto-final-o-bazar/31-06-pagamentos-e-reembolsos.mdx` | esqueleto |
| 31.07 | Eventos, Outbox, webhooks e tempo real | avancado | essencial | `src/content/16-projeto-final-o-bazar/31-07-eventos-outbox-webhooks-e-tempo-real.mdx` | esqueleto |
| 31.08 | Relatórios assíncronos e exportação | avancado | importante | `src/content/16-projeto-final-o-bazar/31-08-relatorios-assincronos-e-exportacao.mdx` | esqueleto |
| 31.09 | Evoluindo: v2 do produto e depreciação da v1 | avancado | importante | `src/content/16-projeto-final-o-bazar/31-09-evoluindo-v2-do-produto-e-depreciacao-da-v1.mdx` | esqueleto |
| 31.10 | Observabilidade, carga e deploy | avancado | essencial | `src/content/16-projeto-final-o-bazar/31-10-observabilidade-carga-e-deploy.mdx` | esqueleto |
| 31.11 | Revisão final: o checklist da API sênior | avancado | essencial | `src/content/16-projeto-final-o-bazar/31-11-revisao-final-o-checklist-da-api-senior.mdx` | esqueleto |

## Apêndices

### M32: Material de consulta

| ID | Página | Nível | Prioridade | Arquivo | Status |
|---|---|---|---|---|---|
| 32.01 | Apêndice A: Checklist de revisão de API | intermediario | essencial | `src/content/17-apendices/32-01-apendice-a-checklist-de-revisao-de-api.mdx` | esqueleto |
| 32.02 | Apêndice B: Referência rápida de status codes, métodos e headers | basico | essencial | `src/content/17-apendices/32-02-apendice-b-referencia-rapida-de-status-codes-metodos.mdx` | esqueleto |
| 32.03 | Apêndice C: Migrando do Spring Boot 3 para o 4 | intermediario | importante | `src/content/17-apendices/32-03-apendice-c-migrando-do-spring-boot-3-para-o-4.mdx` | esqueleto |
| 32.04 | Apêndice D: Glossário | basico | importante | `src/content/17-apendices/32-04-apendice-d-glossario.mdx` | esqueleto |

## Pendências de verificação (fase 3)

Comportamentos mostrados em páginas já prontas que devem ser conferidos contra o `projeto-final/` quando o código correspondente existir. Ajuste a página se o comportamento real for diferente.

| Página | O que conferir | Quando |
|---|---|---|
| 02.02 | ✅ Conferido em 05.04: `200` com `Allow: GET,HEAD,POST,OPTIONS` (a ordem varia; no MockMvc saiu `POST,GET,HEAD,OPTIONS`) | 05.04 |
| 02.05 | ✅ (05.04) `415` traz o header `Accept: application/json, application/*+json`. ✅ (08.02) Erros saem como `application/problem+json` com `Accept: application/json`, `text/csv` e até no `406` de `Accept: application/xml` | 08.02 / 22.02 |
| 02.08 | ✅ Conferido em 05.04: `HTTP/2 200` e `[HTTP/2] [1] OPENED stream` com `--server.http2.enabled=true` | 05.04 |
| 01.01 | ✅ Tags `pagina-MM.PP` criadas a partir de 05.02 (um commit e uma tag por página que altera o `projeto-final/`) | 05.02 |
| 15.01 | `Instant.now()` sai com nove casas decimais no JSON (`2026-10-24T23:20:44.027372882Z`, visto em 06.01). Decidir a precisão (truncar?) e atualizar os exemplos de 06.01 | 15.01 |
| 06.03 | ✅ Conferido: `SORT_PROPERTIES_ALPHABETICALLY=true` e `SORT_CREATOR_PROPERTIES_FIRST=true`; classes saem em ordem alfabética, records na ordem dos componentes. O Bazar desliga a ordem alfabética | 06.03 |
| 07.01 | ✅ Resolvido com `@NotNull @Positive` (teste `rejeitaUmProdutoSemPreco`): `CriarProdutoRequest` sem `precoEmCentavos` hoje causa `NullPointerException` (500) em `Dinheiro.deCentavos`; a validação deve transformar em 400 | 07.01 |
| 08.02 | ✅ Feito (testes com `UNPROCESSABLE_CONTENT`). Trocar o status de validação de `400` (padrão do Spring, visto em 07.01/07.02) para `422`, como decidido em 02.03; atualizar os testes `BAD_REQUEST` de validação | 08.02 |
| 20.01 | Listagens respondem JSON ou CSV pela mesma URI (09.03), mas o Spring MVC não envia `Vary: Accept`; acrescentar junto com o cache HTTP | 20.01 |
