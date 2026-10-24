# Registro de verificações (24/10/2026)

Resolução de todos os itens marcados como "a verificar" na primeira versão do planejamento.
**Legenda:** ✅ confirmado como estava · ✏️ corrigido ou completado · 🔁 decisão alterada

## Spring Boot / Spring Framework

| Página | Item | Resultado | Fonte |
|---|---|---|---|
| VERSOES | Framework 7.0.8 × 7.0.10 | ✏️ 7.0.10 lançado em 22/10/2026; a página do spring.io estava desatualizada. 7.1.0-RC1 também saiu. | GitHub releases |
| VERSOES | — | ✏️ Boot 4.2.0-RC1 já publicado (novo achado) | docs.spring.io/spring-boot/4.2 |
| 05.01 | Nomes dos novos *starters* | ✏️ `spring-boot-starter-webmvc` (o `-web` está depreciado), `-webmvc-test`, `-restclient` | Migration Guide 4.0 |
| 05.02 | URL Docker Compose | ✅ `features/dev-services.html` ("Development-time Services") | doc oficial |
| 06.03 | Nome do customizer | ✏️ `JsonMapperBuilderCustomizer`; bean `JsonMapper`; `@JacksonComponent`/`@JacksonMixin` | Migration Guide 4.0 |
| 06.03 | `spring.jackson.read/write` | ✏️ JSON específico em `spring.jackson.json.read/write.*` (4.0); comuns em `spring.jackson.read/write.*` (4.1) | properties appendix |
| 06.03 | Padrões do Jackson 3 | ✏️ Lista completa adicionada (incluindo a ordenação alfabética) | Jackson 3.0 release notes |
| 09.02 | URL MVC Config API | ✅ | doc oficial |
| 11.03 | URL RestTestClient | ✅ | doc oficial |
| 13.03 | Padrão de serialização de `Page` | ✏️ Continua `direct`; ativar com `spring.data.web.pageable.serialization-mode=via-dto` | properties appendix |
| 18.03 | Propriedades `spring.mvc.apiversion.*` | ✏️ `use.header`, `use.query-parameter`, `use.path-segment`, `use.media-type-parameter.*`, `default`, `required`, `supported`, `detect-supported` | properties appendix |
| 18.03 | Sintaxe de versão de base | ✅ `"1.2+"`; versão fixa `"1.2"`; sem versão = qualquer uma | doc de *mappings* |
| 18.03 | URL MVC config api-version | ✅ | doc oficial |
| 19.02 | Âncora de virtual threads | ✅ `#features.spring-application.virtual-threads` | doc oficial |
| 24.03 | Propriedades de HTTP service client | ✏️ `spring.http.serviceclient.<grupo>.base-url/connect-timeout/read-timeout` (+ headers, versão, SSL); a versão do servidor não se aplica ao cliente | doc oficial |
| 25.02 | URL de resiliência | ✅ `core/resilience.html` | doc oficial |
| 29.02 | CDS / AOT cache no Boot | ✏️ Página própria "AOT Cache"; no Java 25+ usar AOT cache em vez de CDS | doc oficial |
| 29.03 | CRaC | ✏️ Suportado (sob demanda e no *startup*), exige JDK com CRaC | doc oficial |
| 32.03 | URL do Migration Guide | ✅ | GitHub wiki |

## Ecossistema e bibliotecas

| Página | Item | Resultado | Fonte |
|---|---|---|---|
| VERSOES | Spring Cloud com Boot 4.1 | 🔁 2025.1.x suporta Boot 4.1 desde a 2025.1.2 → usar a **2025.1.4** (não esperar a 2026.0) | spring.io/projects/spring-cloud |
| 29.05 | Spring Cloud Gateway com Boot 4.1 | ✏️ Gateway 5.0.x (*train* 2025.1) | idem |
| 30.01 | Spring Modulith com Boot 4.1 | ✏️ 2.1.2, compilado contra o Boot 4.1.2 | POM no Maven Central |
| 10.04 | Biblioteca para contar *queries* | ✏️ `datasource-proxy-spring-boot-starter` 2.0.x (Boot 4) ou estatísticas do Hibernate | README / Maven Central |
| 14.02 | `JsonNullable` com Jackson 3 | ✅ 0.2.12 tem `JsonNullableJackson3Module` | jar no Maven Central |
| 14.02 | Biblioteca de JSON Patch | 🔁 `json-patch` e `zjsonpatch` só suportam Jackson 2 → usar Jakarta JSON-P 2.1 (Parsson) | POMs no Maven Central |
| 15.03 | UUID v7 no JDK | ✏️ JDK 25 não tem; `UUID.ofEpochMillis` a partir do JDK 26 | Javadoc Oracle 25/26/27 |
| 15.03 | Hibernate `VERSION_7` | ✅ Existe em `UuidGenerator.Style` (7.0 a 7.4) | Javadoc Hibernate |
| 17.02 | therapi / versão OAS do springdoc | ✏️ therapi 0.15.0 suportado; padrão OAS 3.1 | springdoc.org |
| 17.03 | Opção do openapi-generator para Boot 4 | ✏️ `useSpringBoot4` + `useJackson3` (7.26.0) | doc oficial |
| 17.03 | Validador de contrato | ✏️ Renomeado para `openapi-request-validator-*` 3.0.0, compatível com Spring 7+ | README / Maven Central |
| 17.04 | Manutenção do Zally | ✏️ Última *release* em 2022 → foco em Spectral 6.17 | GitHub |
| 24.05 | Integração WireMock + Spring | ✏️ `wiremock-spring-boot` 4.4.3 (4.x = Boot 4) | GitHub releases |
| 25.03 | Ordem dos aspectos do Resilience4j | ✅ Retry(CircuitBreaker(RateLimiter(TimeLimiter(Bulkhead(f))))) | doc oficial |
| 25.04 | URL de *fallback* | ✅ (a página cita Boot 2/3; o módulo `-spring-boot4` 2.4.0 existe) | doc oficial |
| 28.01 | Versões k6 / Gatling | ✏️ k6 2.3.0; Gatling 3.16.0 | GitHub / Maven Central |
| VERSOES | Maven | ✏️ 3.10.0 (27/09/2026) | maven.apache.org |
| VERSOES | PostgreSQL | ✏️ 18 (18.6) | postgresql.org |
| PROJETO-FINAL | Imagem de observabilidade | ✏️ `grafana/otel-lgtm:0.38.0` | Docker Hub |

## Especificações, JDK e guias

| Página | Item | Resultado | Fonte |
|---|---|---|---|
| 02.07 | Estado da RFC 6265bis | ✏️ *draft* -22, aprovado e na fila do RFC Editor | IETF datatracker |
| 07.01 | URL do Hibernate Validator 9.1 | ✅ | docs.jboss.org |
| 11.02 | URL da doc do JUnit 6 | ✅ docs.junit.org | doc oficial |
| 15.02 | AIP de Money | ✏️ A AIP-143 é "Standardized codes"; Money é `google.type.Money` (proto) | google.aip.dev / googleapis |
| 17.01 | Última *patch* da OAS 3.1 | ✏️ 3.1.2 | spec.openapis.org |
| 19.06 | Status de Structured Concurrency | ✏️ *Preview* em 25/26/27 (JEPs 505, 525, 533) | openjdk.org |
| 21.02 | Âncora LRO da Microsoft | ✅ `#long-running-operations` | GitHub |
| 28.05 | URL de GC tuning do Java 25 | ✅ | docs.oracle.com |
| 12.03, 12.04, 13.05, 14.04, 15.01, 15.02, 15.04, 16.01 | Âncoras do Zalando (#143, #138, #157, #152, #169, #173, #112, #176) | ✅ Todas corretas (título da regra anotado); a #152 **exige** 207 para lotes | opensource.zalando.com |
| 12.01, 12.02, 18.02 | Âncoras nomeadas do Zalando (`#resources`, `#naming`, `#versioning`) | ✏️ Não existiam → `#urls`, `#113`, `#114` | opensource.zalando.com |
