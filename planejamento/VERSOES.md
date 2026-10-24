# Versões de referência

**Data da verificação:** 24/10/2026. Todos os itens "a verificar" da primeira versão foram resolvidos (ver `VERIFICACOES.md`).
**Regra:** toda a documentação usa as versões abaixo. Quando uma versão mudar, atualize este arquivo primeiro e depois as páginas afetadas.

Legenda da coluna "Fonte":
- **oficial**: página do projeto ou documentação oficial
- **BOM**: versão gerenciada pelo Spring Boot 4.1.2 (tabela *Dependency Versions*)
- **Maven Central**: `maven-metadata.xml` do artefato
- **GitHub**: *release* mais recente do repositório

## Plataforma

| Item | Versão adotada | Observações | Fonte |
|---|---|---|---|
| Java (JDK) | **25 (LTS)** | JDK 27 saiu em 15/09/2026, mas não é LTS (tem suporte até mar/2027). JDK 25 é a LTS atual e recebe atualizações gratuitas (NFTC) até set/2028. | oficial (Oracle / OpenJDK) |
| Spring Boot | **4.1.2** | Exige no mínimo Java 17 e funciona até Java 27. O **4.2.0-RC1** já foi publicado; avaliar o *upgrade* quando a GA sair. | oficial |
| Spring Framework | **7.0.10** | Lançado em 22/10/2026 e exigido pelo Boot 4.1.2. A página do projeto no spring.io ainda mostrava 7.0.8 (estava desatualizada). O **7.1.0-RC1** saiu no mesmo dia; GA prevista para nov/2026. | GitHub releases / BOM |
| Jakarta EE | 11 (Servlet 6.1, Bean Validation 3.1, JPA 3.2) | Baseline do Framework 7 | oficial |
| Servidor embutido | Tomcat 11.0.27 | Jetty 12.1 também é suportado. Undertow foi removido. | BOM |
| Maven | **3.10.0** (27/09/2026) | Mínimo exigido pelo Boot: 3.6.3. A 3.9.16 é a *minor* estável anterior. O Maven 4 ainda está em RC (4.0.0-rc-7). | oficial (maven.apache.org) |
| Gradle (alternativa) | 8.14+ ou 9.x | | oficial |
| GraalVM (imagem nativa) | GraalVM 25+ | Native Build Tools 1.1.14 | oficial |
| PostgreSQL (servidor) | **18** (18.6) | Última *major* suportada | oficial (postgresql.org/support/versioning) |

## Ecossistema Spring

| Projeto | Versão | Observações | Fonte |
|---|---|---|---|
| Spring Security | 7.1.2 | | BOM |
| Spring Data (JPA / Commons) | 4.1.2 (release train 2026.0) | | BOM |
| Spring HATEOAS | 3.1.3 | | BOM |
| Spring REST Docs | 4.0.2 | | BOM |
| Spring gRPC | 1.1.2 | | BOM |
| Spring Kafka | 4.1.2 | | BOM |
| Spring Modulith | **2.1.2** | Compilado contra o Spring Boot 4.1.2 (POM do `spring-modulith-core`). A 2.2.0-RC1 está em andamento. | Maven Central |
| Spring Cloud | **2025.1.4** (Oakwood) | Compatível com Boot 4.0.x e 4.1.x (4.1 a partir da 2025.1.2). Correção da versão anterior deste arquivo. | oficial (spring.io/projects/spring-cloud) |
| Spring Cloud Gateway | 5.0.x (`spring-cloud-gateway-server-webmvc` 5.0.4) | Do *train* 2025.1 | Maven Central |
| Spring Cloud Contract | 5.0.3 | Do *train* 2025.1 | Maven Central |

## Bibliotecas principais

| Biblioteca | Versão | Observações | Fonte |
|---|---|---|---|
| Jackson 3 (`tools.jackson.core`) | 3.1.7 | Padrão no Boot 4 | BOM |
| Jackson 2 (`com.fasterxml.jackson.core`) | 2.21.7 | Só para compatibilidade, e está depreciado. As anotações (`jackson-annotations` 2.21) continuam em `com.fasterxml.jackson.annotation`. | BOM |
| Hibernate ORM | 7.4.13.Final | `@UuidGenerator(style = VERSION_7)` disponível | BOM |
| Hibernate Validator | 9.1.5.Final | | BOM |
| Flyway | 12.4.0 | | BOM |
| Driver PostgreSQL | 42.7.14 | | BOM |
| HikariCP | 7.0.2 | | BOM |
| Micrometer / Micrometer Tracing | 1.17.2 / 1.7.2 | | BOM |
| OpenTelemetry | 1.62.0 | Starter `spring-boot-starter-opentelemetry` | BOM |
| Lettuce (Redis) | 7.5.2.RELEASE | | BOM |
| springdoc-openapi | **3.2.0** | Linha 3.x para Boot 4. Gera **OpenAPI 3.1 por padrão** (`springdoc.api-docs.version=openapi_3_1`). A 3.2 não é citada na documentação. | Maven Central / springdoc.org |
| therapi-runtime-javadoc | 0.15.0 | Javadoc no springdoc (sem *release* desde 2022) | Maven Central |
| openapi-generator (plugin Maven) | **7.26.0** | Opções `useSpringBoot4` e `useJackson3` | Maven Central / doc oficial |
| jackson-databind-nullable | 0.2.12 | Suporta Jackson 3 (`JsonNullableJackson3Module`) | Maven Central |
| Jakarta JSON Processing (JSON Patch / Merge Patch) | `jakarta.json-api` 2.1.3 + Eclipse Parsson 1.1.9 | `json-patch` e `zjsonpatch` só suportam Jackson 2 | Maven Central |
| MapStruct | 1.6.3 (estável) | A 1.7.0.Beta2 existe, mas a doc não usa beta | Maven Central |
| Resilience4j | 2.4.0 (14/03/2026) | Módulo `resilience4j-spring-boot4` | Maven Central / GitHub |
| Bucket4j | 8.21.0 (`bucket4j_jdk17-core`) | | Maven Central |
| uuid-creator | 6.1.1 | Alternativa ao gerador do Hibernate | Maven Central |
| Caffeine | Gerenciada pelo BOM | | BOM |

## Testes

| Biblioteca | Versão | Fonte |
|---|---|---|
| JUnit | 6.0.3 | BOM |
| AssertJ | 3.27.7 | BOM |
| Mockito | 5.23.0 | BOM |
| Testcontainers | 2.0.5 | BOM |
| RestAssured | 6.3.0 | Maven Central |
| WireMock | 3.13.2 (estável). A 4.0 ainda está em beta. | Maven Central |
| WireMock Spring Boot (`org.wiremock.integrations:wiremock-spring-boot`) | 4.4.3. A linha 4.x é compatível com Boot 4. | Maven Central / GitHub |
| datasource-proxy-spring-boot-starter | 2.0.1 (Boot 4) | Maven Central / README |
| Atlassian OpenAPI Request Validator (`openapi-request-validator-*`) | 3.0.0 (Spring 7+). É o antigo `swagger-request-validator`. | Maven Central / README |
| k6 | 2.3.0 | GitHub |
| Gatling | 3.16.0 | Maven Central |

## Ferramentas de contrato e observabilidade

| Ferramenta | Versão | Fonte |
|---|---|---|
| Spectral | 6.17.0 | GitHub |
| oasdiff | 1.34.1 | GitHub |
| Prism (*mock*) | 5.16.0 | GitHub |
| Zally | 2.1.1 (2022). O repositório está ativo, mas sem *release* recente. A doc não usa. | GitHub |
| Grafana LGTM (`grafana/otel-lgtm`) | 0.38.0 (lançamentos semanais; fixe a *tag*) | Docker Hub |

## Especificações e padrões

| Especificação | Versão / estado | Observação |
|---|---|---|
| HTTP Semantics / Caching | RFC 9110 / RFC 9111 | |
| HTTP/1.1, HTTP/2, HTTP/3 | RFC 9112, 9113, 9114 | |
| Problem Details | RFC 9457 | O 7.1 deixa de preencher `instance` automaticamente |
| Método QUERY | RFC 10008 (jun/2026) | Suporte mínimo no Framework 7.1 |
| `Deprecation` / `Sunset` | RFC 9745 / RFC 8594 | |
| Cookies | RFC 6265. A 6265bis (`-22`) foi aprovada e está na fila do RFC Editor. | |
| `Idempotency-Key` | *Internet-Draft* `-08` (22/10/2026) | Ainda não é RFC |
| `RateLimit` | *Internet-Draft* `-11` (23/05/2026) | Ainda não é RFC |
| HTTP Message Signatures | RFC 9421 | |
| UUID v7 | RFC 9562 | JDK 26+ tem `UUID.ofEpochMillis`. O JDK 25 não tem. |
| OpenAPI | 3.2.1 (mais recente), 3.1.2, 3.0.4 | A doc usa **3.1**, o padrão do springdoc |
| Structured Concurrency | *Preview*: JDK 25 (JEP 505), 26 (JEP 525), 27 (JEP 533) | Não usar em produção |
| Scoped Values | Final no JDK 25 (JEP 506) | |

## Resumo do que mudou recentemente e afeta APIs

| Novidade | Desde | Página |
|---|---|---|
| Versionamento nativo de API (`version` no `@RequestMapping`, `spring.mvc.apiversion.*`) | Framework 7.0 / Boot 4.0 | 18.03 |
| Headers de depreciação via `ApiVersionDeprecationHandler` | Framework 7.0 | 18.04 |
| `@ImportHttpServices` + `spring.http.serviceclient.<grupo>.*` | Framework 7.0 / Boot 4.0 | 24.03 |
| *Starters* renomeados (`spring-boot-starter-webmvc`, `-restclient`) | Boot 4.0 | 05.01 |
| `RestTemplate` depreciado formalmente | Framework 7.1 | 24.01 |
| Retry e limite de concorrência nativos | Framework 7.0 | 25.02, 25.03 |
| `RestTestClient` | Framework 7.0 | 11.03 |
| `MockMvcTester` | Framework 6.2 | 11.03 |
| Jackson 3 como padrão (inclusive ordenação alfabética de propriedades) | Boot 4.0 | 06.03 |
| JSpecify | Framework 7.0 | 05.01 |
| Starter de OpenTelemetry | Boot 4.0 | 27.06 |
| Logs estruturados | Boot 3.4 | 27.04 |
| Propagação de contexto para `@Async` | Boot 4.1 | 19.03 |
| gRPC | Boot 4.1 | 30.06 |
| `InetAddressFilter` (anti-SSRF) | Boot 4.1 | 26.05 |
| `spring.jackson.factory.constraints.*` | Boot 4.1 | 23.02 |
| AOT cache recomendado em vez de CDS no Java 25+ | Boot 4.x | 29.02 |
| Virtual threads sem *pinning* em `synchronized` | JDK 24 (JEP 491) | 19.02 |
| `UUID.ofEpochMillis` (UUID v7 no JDK) | JDK 26 | 15.03 |

## Fontes consultadas

- https://spring.io/projects/spring-boot · https://spring.io/projects/spring-framework · https://spring.io/projects/spring-cloud
- https://docs.spring.io/spring-boot/system-requirements.html
- https://docs.spring.io/spring-boot/appendix/dependency-versions/coordinates.html
- https://docs.spring.io/spring-boot/appendix/application-properties/index.html
- https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes · https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1-Release-Notes · https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide
- https://github.com/spring-projects/spring-framework/releases · https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.0-Release-Notes · https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-7.1-Release-Notes
- https://github.com/FasterXML/jackson/wiki/Jackson-Release-3.0
- https://www.oracle.com/java/technologies/downloads/ · https://openjdk.org/projects/jdk/27 · https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/util/UUID.html
- https://maven.apache.org/download.cgi · https://www.postgresql.org/support/versioning/
- https://springdoc.org/ · https://openapi-generator.tech/docs/generators/spring/ · https://spec.openapis.org/oas/
- https://wiremock.org/docs/spring-boot/ · https://resilience4j.readme.io/docs/getting-started-3
- https://repo1.maven.org/maven2/ · https://api.github.com (*releases*) · https://hub.docker.com/r/grafana/otel-lgtm
