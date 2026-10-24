# Descobertas: o que entrou além da lista original

> Tópicos que acrescentei à sua lista inicial, com o motivo de cada um. Entre parênteses está a página do `CURRICULO.md` onde o tópico aparece.

## Novidades de 2025–2026 que poucos tutoriais cobrem

1. **Método HTTP QUERY, RFC 10008 (jun/2026)** (02.02, 13.01): é o primeiro método HTTP novo em mais de uma década. Permite buscas complexas com corpo de forma segura e idempotente, e o Spring 7.1 já traz suporte mínimo.
2. **Header `Deprecation`, RFC 9745** (18.04): agora é padrão oficial, e o versionamento nativo do Spring 7 o emite sozinho com `Sunset` e `Link`. Isso torna a depreciação legível por máquina.
3. **`RestTemplate` depreciado no Framework 7.1 (nov/2026)** (24.01, 32.03): código novo não deve usá-lo, e o legado precisa de um plano de migração para `RestClient`.
4. **Retry e limite de concorrência nativos no `spring-core`** (`@Retryable`, `@ConcurrencyLimit`, `RetryTemplate`) (25.02, 25.03): muitos casos simples deixam de precisar do Spring Retry ou do Resilience4j.
5. **Registro de clientes HTTP (`@ImportHttpServices`)** (24.03): substitui o OpenFeign de forma nativa, com configuração por propriedades por grupo.
6. **`RestTestClient`** (11.03): uma única API de teste que roda contra MockMvc ou contra o servidor real, sem precisar do WebFlux.
7. **Jackson 3 (pacote `tools.jackson`)** (06.03, 32.03): é o padrão no Boot 4. Muitos tutoriais e *snippets* antigos não compilam mais, e alguns valores padrão mudaram.
8. **JSpecify (`@NullMarked`, `@Nullable`)** (05.01): o Spring 7 adota null safety verificável, e projetos podem usar o mesmo padrão para pegar NPEs em tempo de compilação.
9. **Mudança no `ProblemDetail` do 7.1, que não preenche mais `instance`** (08.02): quem dependia desse campo vai ter testes quebrando na atualização.
10. **Propagação automática de contexto para `@Async` (Boot 4.1)** (19.03): logs e *traces* de tarefas em segundo plano deixam de "perder" o `traceId`.
11. **Filtro anti-SSRF (`InetAddressFilter`) nos clientes HTTP (Boot 4.1)** (26.05): é a defesa nativa essencial para quem chama URLs fornecidas por usuários, como webhooks.
12. **Limites de leitura do Jackson configuráveis (`spring.jackson.factory.*`)** (23.02): protegem contra payloads JSON abusivos (strings enormes, aninhamento profundo).
13. **Suporte oficial a gRPC no Boot 4.1** (30.06): a opção para comunicação interna mudou de "biblioteca de terceiros" para "suportado pelo Spring".
14. **Resolução do *pinning* de virtual threads em `synchronized` (JEP 491, JDK 24)** (19.02): muitos alertas antigos sobre virtual threads deixaram de valer, e é importante saber quais ainda valem.
15. **Scoped Values (final no JDK 25) e Structured Concurrency** (19.06): são os sucessores do `ThreadLocal` e do `CompletableFuture` encadeado para propagar contexto e paralelizar chamadas.
16. **CDS e AOT cache da JVM (JEP 483/514/515)** (29.02): reduzem bastante o *startup* sem as restrições da imagem nativa.
17. **Starter de OpenTelemetry no Boot 4** (27.06): é preciso saber quando usar o starter, quando usar o Micrometer Tracing e quando usar o agente Java.

## Padrões e especificações que iniciantes não conhecem

18. **Header `Prefer` (RFC 7240)** (02.04, 14.01, 21.02): o cliente escolhe entre resposta mínima ou completa e pode pedir processamento assíncrono, sem inventar parâmetros próprios.
19. **Header `Link` (RFC 8288)** (02.04, 13.03): é a forma padrão de expressar paginação e relações sem poluir o corpo.
20. **Structured Field Values (RFC 9651)** (02.04): é a sintaxe que os headers modernos (`Deprecation`, `RateLimit`) usam. Sem ela, você os interpreta errado.
21. **Por que não usar o prefixo `X-` em headers (RFC 6648)** (02.04): evita dívidas de nomenclatura que não podem ser desfeitas depois.
22. **Headers `RateLimit` / `RateLimit-Policy` (*draft* IETF)** (23.01): indicam ao cliente quanto ele ainda pode consumir antes de levar 429.
23. **`Idempotency-Key` como *draft* da IETF** (21.01): o padrão popularizado pela Stripe está sendo formalizado, e vale seguir a forma do *draft*.
24. **JSON Merge Patch (RFC 7396) × JSON Patch (RFC 6902) e o problema `null` × ausente** (14.02): é a origem de bugs em que um PATCH apaga dados sem querer.
25. **UUID v7 (RFC 9562)** (15.03): ids que não podem ser enumerados e que não destroem índices B-tree, ao contrário do UUID v4.
26. **RFC 3339 como perfil de datas** (15.01): ISO 8601 é amplo demais, e a RFC 3339 define exatamente o que uma API deve aceitar.
27. **Standard Webhooks e CloudEvents** (21.03): padrões de mercado para assinar e formatar webhooks, em vez de cada um inventar o seu.
28. **HTTP Message Signatures (RFC 9421)** (21.03): é o padrão IETF para assinar requisições HTTP, uma alternativa mais formal ao HMAC simples.
29. **Header `Sunset` (RFC 8594) e o 410 Gone após o desligamento** (18.04): fecham o ciclo de vida de uma versão.
30. **428 Precondition Required** (20.03): força o cliente a usar `If-Match` e impede *lost updates* por esquecimento.
31. **303 See Other em operações assíncronas** (21.02): redireciona corretamente para o resultado quando a operação conclui.
32. **207 Multi-Status e o resultado por item em lotes** (14.04): falhas parciais precisam de um formato explícito.
33. **`Content-Disposition` (RFC 6266) com `filename*` UTF-8** (22.02): sem isso, downloads com acentos no nome quebram.
34. **Range requests e 206** (22.02): retomada de download e *streaming* de arquivos grandes.
35. **`Forwarded` / `X-Forwarded-*` e `forward-headers-strategy`** (26.05): sem essa configuração, a API atrás de *proxy* gera URLs de `Location` erradas (http em vez de https).
36. **OpenAPI 3.1/3.2, Arazzo e Overlay** (17.01): o ecossistema OpenAPI evoluiu. A 3.1 alinhou com JSON Schema, e há especificações irmãs para fluxos e transformações.
37. **AsyncAPI** (30.03): é o "OpenAPI dos eventos", para documentar o que a API publica em filas.

## Design e evolução que separam júnior de sênior

38. **Expand-contract (*parallel change*)** (10.02, 18.01, 29.04): técnica para mudar o banco e o contrato sem *downtime* nem quebrar a versão anterior durante o *rolling update*.
39. **Catálogo de *breaking changes* e a Lei de Hyrum** (18.01): muita coisa que parece inofensiva quebra clientes, como novo enum, nova validação ou mudança da ordenação padrão.
40. ***Tolerant reader* e enums extensíveis** (15.04): projetar os consumidores para tolerar evolução é metade da compatibilidade.
41. **Dinheiro: centavos inteiros × decimal em string + ISO 4217** (15.02): `double` para dinheiro é um bug financeiro esperando para acontecer.
42. **Números grandes em JSON e a precisão do JavaScript** (02.06): ids `long` acima de 2⁵³ chegam corrompidos ao front-end.
43. **Ids públicos × internos e ids legíveis** (15.03): equilibra segurança (enumeração) e usabilidade (atendimento ao cliente).
44. **Paginação: serialização estável de `Page` no Spring Data (`PagedModel`/`VIA_DTO`)** (13.03): serializar `PageImpl` direto expõe detalhes internos que mudam entre versões.
45. **Respostas parciais e *field masks*** (13.05): reduzem payload para clientes móveis (o padrão do Google AIP-161).
46. **Guia de estilo de API e processo de *API review*** (16.02): é o que mantém várias equipes coerentes, e serve de base para o *lint* automático.
47. ***Linting* de contrato (Spectral) e detecção de *breaking changes* (oasdiff) no CI** (17.04): transformam o guia de estilo em regra automática.
48. **Testes de contrato dirigidos pelo consumidor (Pact / Spring Cloud Contract)** (18.05): garantem compatibilidade sem ambientes integrados frágeis.
49. **Métricas de uso por versão e cliente** (18.04, 27.05): sem elas não há como saber quando é seguro desligar uma versão.
50. **Catálogo de erros e o critério 400 × 409 × 422** (16.01): transforma "erro genérico" em contrato acionável pelo cliente.

## Confiabilidade e operação

51. **Orçamento de tempo (*deadline*) e alinhamento de *timeouts* entre gateway, API e dependências** (25.01): *timeouts* desalinhados causam trabalho desperdiçado e *retries* duplicados.
52. ***Retry storms*, *jitter* e *retries* em camadas** (25.02): *retry* mal feito transforma uma falha pequena em indisponibilidade total.
53. **Fallbacks e *load shedding*** (25.04): decidir como degradar é decisão de produto, e um *fallback* errado esconde falhas.
54. **Não fazer chamadas HTTP externas dentro de transações** (10.03): segura conexões do banco e causa exaustão do *pool* sob carga.
55. **Open Session in View (`spring.jpa.open-in-view`)** (10.01): vem ligado por padrão e esconde problemas de N+1 e de conexões.
56. ***Lock* otimista × atualização atômica para estoque** (10.06): evita vender mais do que existe.
57. **`SKIP LOCKED` e *jobs* agendados em várias instâncias** (19.03, 30.04): evita que duas instâncias processem a mesma tarefa.
58. **Lei de Little e saturação do *pool* de *threads*** (19.01): explica por que uma dependência lenta derruba a API inteira.
59. **Dimensionamento do *pool* de conexões com virtual threads** (28.02): virtual threads movem o gargalo para o banco, e "mais conexões" costuma piorar.
60. ***Cache stampede*** (28.03): quando um item muito acessado expira, centenas de requisições batem no banco ao mesmo tempo.
61. ***Coordinated omission* em testes de carga** (28.01): é o erro de medição que faz a latência parecer melhor do que é.
62. **Cardinalidade de métricas** (27.05): uma tag errada (id do pedido) derruba o sistema de métricas.
63. **SLI, SLO e orçamento de erro** (27.01): transformam "a API está boa?" em números acordados.
64. ***Liveness* que não depende de dependências externas** (27.03): é o erro clássico que faz o Kubernetes reiniciar todos os pods quando o banco oscila.
65. ***Graceful shutdown* + `preStop` + *readiness*** (29.04): sem isso, todo *deploy* derruba requisições em andamento.
66. **SBOM e *scan* de vulnerabilidades da imagem** (27.02, 29.02): são exigência crescente de *compliance*, e o Boot já gera o SBOM.

## Segurança específica de APIs (sem invadir a outra doc)

67. **OWASP API Security Top 10 (2023)** (26.01): o mapa de riscos específico de APIs, diferente do Top 10 web tradicional.
68. **BOLA/IDOR e BOPLA (*mass assignment* / exposição excessiva)** (26.03): são os riscos nº 1 e nº 3 e se resolvem no código da API, não no *framework*.
69. **SSRF via URLs de webhook** (26.05): todo sistema que aceita URL do cliente vira alvo para atacar a rede interna.
70. **Validação de assinatura e proteção contra *replay* em webhooks recebidos** (21.03): sem isso qualquer um pode "confirmar" um pagamento.
71. **Validação do tipo real de arquivos (*magic bytes*)** (22.01): o `Content-Type` enviado pelo cliente não é confiável.
72. **BREACH e compressão** (22.03): comprimir respostas que refletem segredos pode vazá-los.
73. **LGPD: minimização e mascaramento em respostas e logs** (26.03, 27.04): é exigência legal no Brasil e afeta o design das representações.

## Arquitetura e ecossistema

74. **Spring Modulith (monólito modular verificável)** (30.01): é o caminho recomendado antes de microsserviços e tem suporte oficial do Spring.
75. **Padrão *inbox* e consumidores idempotentes** (30.03, 30.04): o Outbox resolve o envio; o *inbox* resolve o recebimento duplicado.
76. **Spring Modulith Event Publication Registry como Outbox** (30.04): é uma implementação pronta do padrão, integrada ao Spring.
77. **Disponibilidade composta em chamadas síncronas encadeadas** (30.02): cinco serviços a 99,9% em série dão cerca de 99,5%. Esse número justifica a comunicação assíncrona.
78. **BFF (*Backend for Frontend*)** (29.05): resolve necessidades específicas de cada front-end sem poluir a API principal.
79. **GraphQL com Spring for GraphQL e `@BatchMapping`** (30.07): é a alternativa real a REST para front-ends com telas muito variadas, e precisa de proteção contra N+1 e consultas abusivas.

## Ferramentas e práticas de aprendizado

80. **Suporte a Docker Compose e Testcontainers no desenvolvimento** (05.02, 11.04): o Boot sobe as dependências locais sozinho, sem configuração manual.
81. **WireMock com cenários de falha** (24.05): integrações só são testadas de verdade com lentidão, 503 e respostas inesperadas.
82. ***Mock* do contrato com Prism no fluxo API-first** (17.03): o front-end começa antes do back-end.
83. **Spring REST Docs e o modelo Diátaxis** (17.05): documentação gerada de testes, que não fica desatualizada, mais guias além da referência.
84. ***Profiling* com JFR e async-profiler** (28.05): mostra o que métricas e *traces* não explicam.
85. **Apêndice de migração do Boot 3 para o 4** (32.03): a maioria dos projetos reais ainda está no Boot 3.x.
86. **Checklist de revisão de API** (32.01): uma ferramenta prática para aplicar tudo no trabalho, não só no estudo.
87. **Logs estruturados nativos (ECS, Logstash, GELF)** (27.04): desde o Boot 3.4 não é preciso configurar Logback na mão para ter JSON.
88. **Spring Data `Window`/`ScrollPosition` para *keyset*** (13.04): suporte nativo à paginação por cursor, pouco conhecido.
89. **Validação de métodos embutida no Spring MVC (`HandlerMethodValidationException`)** (07.01): mudou como erros de `@PathVariable`/`@RequestParam` chegam ao tratador global.
90. **`ShallowEtagHeaderFilter` só economiza banda, não processamento** (20.02): é um equívoco comum achar que ele alivia o servidor.
91. **Jackson 3 ordena as propriedades em ordem alfabética por padrão (`SORT_PROPERTIES_ALPHABETICALLY=true`)** (06.03): a ordem dos campos no JSON muda na migração. Isso quebra *snapshots* de teste e clientes frágeis.
92. **`spring.data.web.pageable.serialization-mode` ainda vem como `direct`** (13.03): a serialização estável de páginas (`via-dto`) precisa ser ativada explicitamente.
93. **`UUID.ofEpochMillis` no JDK 26+** (15.03): o UUID v7 entrou no JDK, mas só depois da LTS 25. No JDK 25, use o gerador `VERSION_7` do Hibernate.
