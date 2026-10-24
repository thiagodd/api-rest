/**
 * Fonte única da navegação do site: partes → módulos → páginas.
 *
 * Gerado a partir de planejamento/CURRICULO.md por `npm run gerar:curriculo`.
 * Depois de gerado, este arquivo é a referência: edite aqui (e no CURRICULO.md)
 * quando uma página mudar de título, nível ou prioridade. Para regenerar do zero,
 * use `npm run gerar:curriculo -- --forcar-navegacao`.
 *
 * O conteúdo de cada página fica em src/content/<pasta da parte>/<id>-<slug>.mdx.
 */

export type Nivel = 'basico' | 'intermediario' | 'avancado'
export type Prioridade = 'essencial' | 'importante' | 'complementar'

export interface PaginaNav {
  /** ID `MM.PP` do currículo (módulo.página) */
  id: string
  slug: string
  titulo: string
  descricao: string
  nivel: Nivel
  prioridade: Prioridade
  /** IDs das páginas que devem ser lidas antes */
  preRequisitos: string[]
  /** Pré-requisito descrito em texto quando não é uma lista de IDs */
  preRequisitosNota?: string
}

export interface ModuloNav {
  id: string
  titulo: string
  paginas: PaginaNav[]
}

export interface ParteNav {
  /** null para os apêndices */
  numero: number | null
  titulo: string
  slug: string
  /** Pasta em src/content */
  pasta: string
  /** Nome de um ícone do lucide-react */
  icone: string
  modulos: ModuloNav[]
}

export const navigation: ParteNav[] = [
  {
    "numero": 1,
    "titulo": "Começando e fundamentos de HTTP",
    "slug": "comecando-e-fundamentos-de-http",
    "pasta": "01-comecando-e-fundamentos-de-http",
    "icone": "Rocket",
    "modulos": [
      {
        "id": "M01",
        "titulo": "Começando",
        "paginas": [
          {
            "id": "01.01",
            "slug": "como-usar-esta-documentacao",
            "titulo": "Como usar esta documentação",
            "descricao": "Explica a organização, os níveis e as trilhas para o leitor não se perder em 140 páginas. Também apresenta o Bazar, que acompanha todos os exemplos.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": []
          },
          {
            "id": "01.02",
            "slug": "preparando-o-ambiente",
            "titulo": "Preparando o ambiente",
            "descricao": "Garante que todos os exemplos rodem igual na máquina do leitor.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "01.01"
            ]
          }
        ]
      },
      {
        "id": "M02",
        "titulo": "HTTP essencial",
        "paginas": [
          {
            "id": "02.01",
            "slug": "anatomia-de-uma-mensagem-http",
            "titulo": "Anatomia de uma mensagem HTTP",
            "descricao": "Tudo em APIs REST é HTTP. Quem não lê uma requisição crua não consegue depurar problemas de integração.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "01.02"
            ]
          },
          {
            "id": "02.02",
            "slug": "metodos-http-semantica-seguranca-e-idempotencia",
            "titulo": "Métodos HTTP: semântica, segurança e idempotência",
            "descricao": "Escolher o método errado quebra caches, retries automáticos e proxies. Idempotência é a base de toda a confiabilidade da Parte 9.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "02.01"
            ]
          },
          {
            "id": "02.03",
            "slug": "status-codes-em-profundidade",
            "titulo": "Status codes em profundidade",
            "descricao": "O status code é o primeiro (e às vezes o único) dado que clientes, gateways, monitoramento e retries leem. \"200 com {\"erro\": ...}\" é um dos erros mais comuns de iniciantes.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "02.02"
            ]
          },
          {
            "id": "02.04",
            "slug": "headers-essenciais",
            "titulo": "Headers essenciais",
            "descricao": "Metadados como localização, cache, retries e paginação viajam em headers. Uma API que ignora headers reinventa tudo no corpo.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "02.03"
            ]
          },
          {
            "id": "02.05",
            "slug": "content-negotiation-e-media-types",
            "titulo": "Content negotiation e media types",
            "descricao": "Explica por que o Spring às vezes devolve 406 ou 415 e como uma mesma URI pode servir JSON, CSV ou uma versão diferente do recurso.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "02.04"
            ]
          },
          {
            "id": "02.06",
            "slug": "json-para-apis",
            "titulo": "JSON para APIs",
            "descricao": "JSON parece trivial, mas números grandes, null × ausente e convenções de nomes causam bugs de integração silenciosos.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "02.05"
            ]
          },
          {
            "id": "02.07",
            "slug": "cookies-estado-e-sessoes",
            "titulo": "Cookies, estado e sessões",
            "descricao": "A restrição stateless do REST costuma ser mal entendida. Saber onde o estado mora evita APIs que não escalam horizontalmente.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "02.04"
            ]
          },
          {
            "id": "02.08",
            "slug": "http-1-1-http-2-e-http-3",
            "titulo": "HTTP/1.1, HTTP/2 e HTTP/3",
            "descricao": "Influencia a latência, o custo de conexões entre serviços e decisões como \"agrupar chamadas ou não\".",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "02.01"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 2,
    "titulo": "REST e estilos de API",
    "slug": "rest-e-estilos-de-api",
    "pasta": "02-rest-e-estilos-de-api",
    "icone": "Network",
    "modulos": [
      {
        "id": "M03",
        "titulo": "REST de verdade",
        "paginas": [
          {
            "id": "03.01",
            "slug": "as-restricoes-de-fielding",
            "titulo": "As restrições de Fielding",
            "descricao": "REST é um estilo arquitetural com restrições precisas, não \"JSON sobre HTTP\". Entender as restrições explica por que as boas práticas existem.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "02.07"
            ]
          },
          {
            "id": "03.02",
            "slug": "recursos-representacoes-e-a-interface-uniforme",
            "titulo": "Recursos, representações e a interface uniforme",
            "descricao": "Pensar em recursos (e não em tabelas ou métodos Java) é o que diferencia um design REST de um RPC disfarçado.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "03.01"
            ]
          },
          {
            "id": "03.03",
            "slug": "o-modelo-de-maturidade-de-richardson",
            "titulo": "O modelo de maturidade de Richardson",
            "descricao": "É um vocabulário comum para avaliar APIs existentes e conversar com o time.",
            "nivel": "basico",
            "prioridade": "importante",
            "preRequisitos": [
              "03.02"
            ]
          },
          {
            "id": "03.04",
            "slug": "hateoas-e-hipermidia",
            "titulo": "HATEOAS e hipermídia",
            "descricao": "Links na resposta permitem ao cliente descobrir ações válidas (por exemplo, \"pode cancelar?\") sem duplicar regras de negócio, mas têm custo. Saber quando vale a pena é sinal de maturidade.",
            "nivel": "intermediario",
            "prioridade": "complementar",
            "preRequisitos": [
              "03.03"
            ]
          }
        ]
      },
      {
        "id": "M04",
        "titulo": "Estilos de API",
        "paginas": [
          {
            "id": "04.01",
            "slug": "rest-rpc-graphql-grpc-websocket-sse-e-webhooks-quando-usar",
            "titulo": "REST, RPC, GraphQL, gRPC, WebSocket, SSE e webhooks: quando usar cada um",
            "descricao": "Um sênior escolhe o estilo pelo problema, não pela moda, e consegue justificar a escolha.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "03.03"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 3,
    "titulo": "Construindo APIs com Spring Boot",
    "slug": "construindo-apis-com-spring-boot",
    "pasta": "03-construindo-apis-com-spring-boot",
    "icone": "Leaf",
    "modulos": [
      {
        "id": "M05",
        "titulo": "Primeiro projeto",
        "paginas": [
          {
            "id": "05.01",
            "slug": "spring-boot-4-em-uma-pagina",
            "titulo": "Spring Boot 4 em uma página",
            "descricao": "O leitor sabe o básico de Spring Boot, mas a versão 4 mudou coisas importantes (Jackson 3, modularização, null safety). Esta página nivela o conhecimento.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "01.02"
            ]
          },
          {
            "id": "05.02",
            "slug": "criando-o-projeto-bazar",
            "titulo": "Criando o projeto Bazar",
            "descricao": "A estrutura de pacotes definida aqui se mantém até o projeto final. Organizar por módulo de negócio evita o \"pacote service com 80 classes\".",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "05.01"
            ]
          },
          {
            "id": "05.03",
            "slug": "primeiro-endpoint",
            "titulo": "Primeiro endpoint",
            "descricao": "É a porta de entrada para entender o que o Spring faz por baixo quando um método vira um endpoint.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "05.02",
              "02.03"
            ]
          },
          {
            "id": "05.04",
            "slug": "um-crud-com-status-corretos",
            "titulo": "Um CRUD com status corretos",
            "descricao": "A maioria dos CRUDs devolve 200 para tudo. Aqui o leitor aplica 201, 204 e 404 corretamente desde o primeiro dia.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "05.03"
            ]
          }
        ]
      },
      {
        "id": "M06",
        "titulo": "Modelando entrada e saída",
        "paginas": [
          {
            "id": "06.01",
            "slug": "dtos-com-records-e-por-que-nao-expor-entidades",
            "titulo": "DTOs com records e por que não expor entidades",
            "descricao": "Expor a entidade acopla o contrato ao banco, vaza campos sensíveis e abre espaço para mass assignment.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "05.04"
            ]
          },
          {
            "id": "06.02",
            "slug": "mapeamento-manual-ou-mapstruct",
            "titulo": "Mapeamento: manual ou MapStruct",
            "descricao": "O mapeamento manual cresce rápido e esconde bugs. O MapStruct gera código verificado em compilação, sem reflexão.",
            "nivel": "basico",
            "prioridade": "importante",
            "preRequisitos": [
              "06.01"
            ]
          },
          {
            "id": "06.03",
            "slug": "jackson-3-no-spring-boot-4",
            "titulo": "Jackson 3 no Spring Boot 4",
            "descricao": "A serialização define o contrato real da API. No Boot 4 o Jackson mudou de pacote e de padrões, e muitos tutoriais antigos deixaram de funcionar.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "06.01",
              "02.06"
            ]
          }
        ]
      },
      {
        "id": "M07",
        "titulo": "Validação",
        "paginas": [
          {
            "id": "07.01",
            "slug": "bean-validation-essencial",
            "titulo": "Bean Validation essencial",
            "descricao": "Validar na borda impede dados inválidos de chegarem ao domínio e devolve erros úteis ao cliente.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "06.01"
            ]
          },
          {
            "id": "07.02",
            "slug": "validacao-avancada",
            "titulo": "Validação avançada",
            "descricao": "Regras reais (CPF, intervalos de datas, campos dependentes) não cabem nas anotações prontas.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "07.01"
            ]
          }
        ]
      },
      {
        "id": "M08",
        "titulo": "Tratamento de erros",
        "paginas": [
          {
            "id": "08.01",
            "slug": "problem-details-rfc-9457",
            "titulo": "Problem Details (RFC 9457)",
            "descricao": "Um formato padrão de erro permite que clientes tratem falhas de forma genérica. A RFC 9457 é o padrão que o Spring adota.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "02.05",
              "07.01"
            ]
          },
          {
            "id": "08.02",
            "slug": "problemdetail-no-spring-tratamento-global-de-erros",
            "titulo": "ProblemDetail no Spring: tratamento global de erros",
            "descricao": "Centralizar o tratamento de erros elimina try/catch nos controllers e garante respostas consistentes, sem vazar stack traces.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "08.01"
            ]
          }
        ]
      },
      {
        "id": "M09",
        "titulo": "Spring MVC por dentro",
        "paginas": [
          {
            "id": "09.01",
            "slug": "o-caminho-de-uma-requisicao-no-spring-mvc",
            "titulo": "O caminho de uma requisição no Spring MVC",
            "descricao": "Sem esse modelo mental, depurar 404, 406 ou um JSON estranho vira tentativa e erro. Ele também é a base para estender o framework.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "08.02"
            ]
          },
          {
            "id": "09.02",
            "slug": "argument-resolvers-e-return-value-handlers-customizados",
            "titulo": "Argument resolvers e return value handlers customizados",
            "descricao": "Permite injetar dados recorrentes (cliente autenticado, contexto de paginação) sem repetir código em todo controller.",
            "nivel": "avancado",
            "prioridade": "complementar",
            "preRequisitos": [
              "09.01"
            ]
          },
          {
            "id": "09.03",
            "slug": "httpmessageconverter-e-a-serializacao-por-dentro",
            "titulo": "HttpMessageConverter e a serialização por dentro",
            "descricao": "Explica 406/415 e como servir CSV, PDF ou outros formatos sem hacks.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "09.01",
              "02.05",
              "06.03"
            ]
          },
          {
            "id": "09.04",
            "slug": "filtros-interceptors-e-aop-quando-usar-cada-um",
            "titulo": "Filtros, interceptors e AOP: quando usar cada um",
            "descricao": "É uma decisão frequente (logging, correlation ID, métricas, auditoria). Escolher o mecanismo errado causa bugs de ordem e de exceções não tratadas.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "09.01"
            ]
          },
          {
            "id": "09.05",
            "slug": "functional-endpoints-routerfunction",
            "titulo": "Functional endpoints (RouterFunction)",
            "descricao": "É uma alternativa às anotações, útil para roteamento dinâmico e para quem vem do WebFlux.",
            "nivel": "intermediario",
            "prioridade": "complementar",
            "preRequisitos": [
              "09.01"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 4,
    "titulo": "Persistência para APIs",
    "slug": "persistencia-para-apis",
    "pasta": "04-persistencia-para-apis",
    "icone": "Database",
    "modulos": [
      {
        "id": "M10",
        "titulo": "Spring Data JPA a serviço da API",
        "paginas": [
          {
            "id": "10.01",
            "slug": "spring-data-jpa-essencial-para-apis",
            "titulo": "Spring Data JPA essencial para APIs",
            "descricao": "Quase toda API persiste dados. Usar JPA sem entender o que ele faz é a principal fonte de problemas de desempenho em APIs Spring.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "06.01"
            ]
          },
          {
            "id": "10.02",
            "slug": "migrations-com-flyway",
            "titulo": "Migrations com Flyway",
            "descricao": "O esquema do banco precisa ser versionado como o código. Sem isso, deploys e ambientes divergem.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "10.01"
            ]
          },
          {
            "id": "10.03",
            "slug": "transacoes-na-camada-de-servico",
            "titulo": "Transações na camada de serviço",
            "descricao": "Limites de transação errados geram dados inconsistentes, conexões presas e deadlocks. Chamar uma API externa dentro de uma transação é um erro clássico.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "10.01"
            ]
          },
          {
            "id": "10.04",
            "slug": "o-problema-n-1-e-estrategias-de-fetch",
            "titulo": "O problema N+1 e estratégias de fetch",
            "descricao": "É a causa nº 1 de endpoints lentos em APIs com JPA, e só aparece com dados reais.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "10.03"
            ]
          },
          {
            "id": "10.05",
            "slug": "projecoes-e-consultas-para-leitura",
            "titulo": "Projeções e consultas para leitura",
            "descricao": "Carregar entidades completas para montar uma listagem desperdiça memória e banco. Projeções devolvem só o necessário.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "10.04"
            ]
          },
          {
            "id": "10.06",
            "slug": "concorrencia-no-banco-locking-otimista-e-pessimista",
            "titulo": "Concorrência no banco: locking otimista e pessimista",
            "descricao": "Duas requisições simultâneas reduzindo o mesmo estoque causam overselling. A API precisa de uma estratégia explícita.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "10.03"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 5,
    "titulo": "Testes de API",
    "slug": "testes-de-api",
    "pasta": "05-testes-de-api",
    "icone": "FlaskConical",
    "modulos": [
      {
        "id": "M11",
        "titulo": "Testando APIs Spring Boot",
        "paginas": [
          {
            "id": "11.01",
            "slug": "estrategia-de-testes-para-apis",
            "titulo": "Estratégia de testes para APIs",
            "descricao": "Sem estratégia os testes ficam lentos, frágeis ou não pegam bugs reais. Esta página define o que testar em cada camada.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "10.01"
            ]
          },
          {
            "id": "11.02",
            "slug": "testes-unitarios-de-dominio-e-servicos",
            "titulo": "Testes unitários de domínio e serviços",
            "descricao": "Regras de negócio (máquina de estados do pedido, cálculo de total) devem ser testadas rápido e sem Spring.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "11.01"
            ]
          },
          {
            "id": "11.03",
            "slug": "testando-a-camada-web-webmvctest-mockmvctester",
            "titulo": "Testando a camada web: @WebMvcTest, MockMvcTester e RestTestClient",
            "descricao": "Verifica o contrato HTTP (status, headers, JSON, erros) em milissegundos, sem banco.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "11.02",
              "08.02"
            ]
          },
          {
            "id": "11.04",
            "slug": "testes-de-integracao-com-springboottest-e-testcontainers",
            "titulo": "Testes de integração com @SpringBootTest e Testcontainers",
            "descricao": "H2 não é PostgreSQL. Testes com o banco real pegam bugs de SQL, migrations e transações.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "11.03",
              "10.02"
            ]
          },
          {
            "id": "11.05",
            "slug": "testes-ponta-a-ponta-com-restassured",
            "titulo": "Testes ponta a ponta com RestAssured",
            "descricao": "Valida fluxos completos (criar produto → criar pedido → pagar) pela perspectiva de um cliente real, via HTTP.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "11.04"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 6,
    "titulo": "Design de APIs profissional",
    "slug": "design-de-apis-profissional",
    "pasta": "06-design-de-apis-profissional",
    "icone": "PenTool",
    "modulos": [
      {
        "id": "M12",
        "titulo": "Recursos e URIs",
        "paginas": [
          {
            "id": "12.01",
            "slug": "modelando-recursos-a-partir-do-dominio",
            "titulo": "Modelando recursos a partir do domínio",
            "descricao": "O modelo de recursos é a decisão mais cara de mudar depois. Um modelo ruim gera dezenas de endpoints RPC disfarçados.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "03.02",
              "10.01"
            ]
          },
          {
            "id": "12.02",
            "slug": "convencoes-de-nomenclatura",
            "titulo": "Convenções de nomenclatura",
            "descricao": "Consistência é o que faz uma API parecer \"profissional\". Clientes aprendem um padrão e o aplicam em toda a API.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "12.01"
            ]
          },
          {
            "id": "12.03",
            "slug": "relacionamentos-e-sub-recursos",
            "titulo": "Relacionamentos e sub-recursos",
            "descricao": "Decide entre aninhar URIs, usar links ou embutir dados. Isso impacta cache, permissões e o número de chamadas do cliente.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "12.01"
            ]
          },
          {
            "id": "12.04",
            "slug": "acoes-que-nao-sao-crud-e-maquinas-de-estado",
            "titulo": "Ações que não são CRUD e máquinas de estado",
            "descricao": "\"cancelar pedido\" não é um PATCH de status. Modelar ações corretamente protege as regras de negócio e deixa o contrato claro.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "12.03",
              "11.02"
            ]
          }
        ]
      },
      {
        "id": "M13",
        "titulo": "Consultando coleções",
        "paginas": [
          {
            "id": "13.01",
            "slug": "filtros-e-busca",
            "titulo": "Filtros e busca",
            "descricao": "Filtros mal projetados viram dezenas de endpoints ou queries lentas e inseguras.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "12.03",
              "10.05"
            ]
          },
          {
            "id": "13.02",
            "slug": "ordenacao",
            "titulo": "Ordenação",
            "descricao": "Ordenação sem critério de desempate gera páginas inconsistentes. Ordenação livre abre risco de desempenho.",
            "nivel": "basico",
            "prioridade": "importante",
            "preRequisitos": [
              "13.01"
            ]
          },
          {
            "id": "13.03",
            "slug": "paginacao-por-offset",
            "titulo": "Paginação por offset",
            "descricao": "É o modelo mais comum e o padrão do Spring, mas tem custos (COUNT, páginas instáveis) que um sênior precisa conhecer.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "13.02"
            ]
          },
          {
            "id": "13.04",
            "slug": "paginacao-por-cursor-keyset",
            "titulo": "Paginação por cursor (keyset)",
            "descricao": "É a única paginação que escala para coleções grandes e com escrita constante (pedidos, eventos). APIs como Stripe, Slack e Google usam.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "13.03"
            ]
          },
          {
            "id": "13.05",
            "slug": "respostas-parciais-e-expansao",
            "titulo": "Respostas parciais e expansão",
            "descricao": "Reduz o tráfego para clientes móveis e evita chamadas extras, mas aumenta a complexidade e prejudica o cache.",
            "nivel": "avancado",
            "prioridade": "complementar",
            "preRequisitos": [
              "13.04",
              "06.03"
            ]
          }
        ]
      },
      {
        "id": "M14",
        "titulo": "Escrita e modificação",
        "paginas": [
          {
            "id": "14.01",
            "slug": "criacao-com-post-e-put-com-id-do-cliente",
            "titulo": "Criação com POST (e PUT com id do cliente)",
            "descricao": "Os detalhes da criação (status, Location, corpo de retorno, ids) afetam toda a experiência do cliente.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "12.02",
              "07.01"
            ]
          },
          {
            "id": "14.02",
            "slug": "put-patch-json-merge-patch-e-json-patch",
            "titulo": "PUT × PATCH: JSON Merge Patch e JSON Patch",
            "descricao": "Atualização parcial mal feita apaga dados (o null acidental) ou não consegue limpar campos. É uma das partes mais sutis do design de APIs.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "14.01",
              "02.06"
            ]
          },
          {
            "id": "14.03",
            "slug": "delete-soft-delete-e-arquivamento",
            "titulo": "DELETE, soft delete e arquivamento",
            "descricao": "Apagar de verdade dados com histórico (produto já vendido) quebra relatórios e auditoria. Decidir a semântica do DELETE é parte do contrato.",
            "nivel": "basico",
            "prioridade": "importante",
            "preRequisitos": [
              "14.01"
            ]
          },
          {
            "id": "14.04",
            "slug": "operacoes-em-lote",
            "titulo": "Operações em lote",
            "descricao": "Integrações (por exemplo, o ERP de um lojista) precisam atualizar centenas de itens. Fazer N chamadas é lento; um lote mal projetado gera falhas parciais confusas.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "14.02",
              "10.03"
            ]
          }
        ]
      },
      {
        "id": "M15",
        "titulo": "Tipos de dados no contrato",
        "paginas": [
          {
            "id": "15.01",
            "slug": "datas-horas-e-fusos-horarios",
            "titulo": "Datas, horas e fusos horários",
            "descricao": "Bugs de fuso aparecem na virada do dia, no horário de verão e com clientes em outros países, e quase sempre já em produção.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "06.03"
            ]
          },
          {
            "id": "15.02",
            "slug": "dinheiro-e-numeros-decimais",
            "titulo": "Dinheiro e números decimais",
            "descricao": "Double para dinheiro gera centavos errados, e um erro de moeda é um incidente financeiro.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "15.01"
            ]
          },
          {
            "id": "15.03",
            "slug": "identificadores-sequenciais-uuid-v7-e-ulid",
            "titulo": "Identificadores: sequenciais, UUID v7 e ULID",
            "descricao": "Ids sequenciais expostos permitem enumeração e vazam volume de negócio. UUID v4 aleatório degrada índices. O UUID v7 resolve os dois problemas.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "10.01"
            ]
          },
          {
            "id": "15.04",
            "slug": "enums-evolucao-e-tolerant-reader",
            "titulo": "Enums, evolução e tolerant reader",
            "descricao": "Adicionar um valor de enum é uma mudança que quebra clientes que não estão preparados. Projetar para evolução evita incidentes.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "15.01",
              "06.03"
            ]
          },
          {
            "id": "15.05",
            "slug": "internacionalizacao-na-api",
            "titulo": "Internacionalização na API",
            "descricao": "Mensagens de erro e conteúdos traduzidos dependem de negociar o idioma corretamente.",
            "nivel": "intermediario",
            "prioridade": "complementar",
            "preRequisitos": [
              "08.02",
              "02.05"
            ]
          }
        ]
      },
      {
        "id": "M16",
        "titulo": "Consistência e guia de estilo",
        "paginas": [
          {
            "id": "16.01",
            "slug": "projetando-erros-catalogo-codigos-e-decisoes-dificeis",
            "titulo": "Projetando erros: catálogo, códigos e decisões difíceis",
            "descricao": "Além do formato (08.01), um sênior define quais erros existem, quando usar cada status e como o cliente deve reagir.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "08.02",
              "12.04"
            ]
          },
          {
            "id": "16.02",
            "slug": "escrevendo-o-guia-de-estilo-da-sua-api",
            "titulo": "Escrevendo o guia de estilo da sua API",
            "descricao": "Em empresas com várias equipes, o guia de estilo é o que mantém dezenas de APIs coerentes. Ele vira regras automáticas de lint em 17.04.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "16.01",
              "15.04",
              "13.04",
              "14.02"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 7,
    "titulo": "Contratos, documentação e ciclo de vida",
    "slug": "contratos-documentacao-e-ciclo-de-vida",
    "pasta": "07-contratos-documentacao-e-ciclo-de-vida",
    "icone": "FileText",
    "modulos": [
      {
        "id": "M17",
        "titulo": "OpenAPI e contrato",
        "paginas": [
          {
            "id": "17.01",
            "slug": "a-especificacao-openapi",
            "titulo": "A especificação OpenAPI",
            "descricao": "O contrato OpenAPI é a base para documentação, geração de clientes, mocks, testes e governança. Precisa ser lido e escrito com fluência.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "16.01"
            ]
          },
          {
            "id": "17.02",
            "slug": "springdoc-openapi-documentacao-a-partir-do-codigo",
            "titulo": "springdoc-openapi: documentação a partir do código",
            "descricao": "É o caminho mais rápido para ter um contrato e uma UI de testes. Ainda assim, o contrato gerado precisa de cuidado para ser útil.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "17.01",
              "08.02"
            ]
          },
          {
            "id": "17.03",
            "slug": "api-first-o-contrato-como-fonte-da-verdade",
            "titulo": "API-first: o contrato como fonte da verdade",
            "descricao": "Projetar o contrato antes do código permite revisão, mocks para o front-end e trabalho em paralelo, além de evitar que detalhes de implementação vazem para a API.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "17.02"
            ]
          },
          {
            "id": "17.04",
            "slug": "governanca-linting-e-deteccao-de-breaking-changes",
            "titulo": "Governança: linting e detecção de breaking changes",
            "descricao": "Automatizar o guia de estilo e bloquear mudanças incompatíveis no CI é o que torna a consistência sustentável.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "17.03",
              "16.02"
            ]
          },
          {
            "id": "17.05",
            "slug": "documentacao-alem-da-referencia",
            "titulo": "Documentação além da referência",
            "descricao": "A referência OpenAPI diz o que existe; guias dizem como usar. A falta de guias é a principal queixa de quem integra APIs.",
            "nivel": "intermediario",
            "prioridade": "complementar",
            "preRequisitos": [
              "17.02"
            ]
          }
        ]
      },
      {
        "id": "M18",
        "titulo": "Evolução e versionamento",
        "paginas": [
          {
            "id": "18.01",
            "slug": "compatibilidade-retroativa-o-que-quebra-um-cliente",
            "titulo": "Compatibilidade retroativa: o que quebra um cliente",
            "descricao": "Versionar é caro. Quem sabe evoluir sem quebrar raramente precisa de uma v2.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "17.01",
              "15.04"
            ]
          },
          {
            "id": "18.02",
            "slug": "estrategias-de-versionamento",
            "titulo": "Estratégias de versionamento",
            "descricao": "Quando uma mudança incompatível é inevitável, a estratégia de versão define custo de operação, cache e experiência do cliente.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "18.01",
              "02.05"
            ]
          },
          {
            "id": "18.03",
            "slug": "versionamento-nativo-no-spring-framework-7",
            "titulo": "Versionamento nativo no Spring Framework 7",
            "descricao": "Desde o Spring 7 / Boot 4 o versionamento é suportado pelo framework, sem hacks de prefixo ou controllers duplicados.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "18.02",
              "09.01"
            ]
          },
          {
            "id": "18.04",
            "slug": "depreciacao-e-desligamento-de-apis",
            "titulo": "Depreciação e desligamento de APIs",
            "descricao": "Desligar uma versão sem processo derruba clientes. Os headers padronizados permitem que ferramentas avisem automaticamente.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "18.03"
            ]
          },
          {
            "id": "18.05",
            "slug": "testes-de-contrato",
            "titulo": "Testes de contrato",
            "descricao": "Garantem que produtor e consumidores continuam compatíveis sem precisar de ambientes integrados frágeis.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "18.01",
              "11.04"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 8,
    "titulo": "Concorrência e execução assíncrona",
    "slug": "concorrencia-e-execucao-assincrona",
    "pasta": "08-concorrencia-e-execucao-assincrona",
    "icone": "Workflow",
    "modulos": [
      {
        "id": "M19",
        "titulo": "Threads, virtual threads e tarefas em segundo plano",
        "paginas": [
          {
            "id": "19.01",
            "slug": "o-modelo-thread-per-request-e-seus-limites",
            "titulo": "O modelo thread-per-request e seus limites",
            "descricao": "Entender quantas requisições a API atende ao mesmo tempo e o que acontece quando uma dependência fica lenta é a base de desempenho e resiliência.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "09.01",
              "10.03"
            ]
          },
          {
            "id": "19.02",
            "slug": "virtual-threads-em-apis-spring",
            "titulo": "Virtual threads em APIs Spring",
            "descricao": "Virtual threads (Java 21+) permitem milhares de requisições bloqueantes simultâneas com código imperativo simples, mas não removem outros gargalos.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "19.01"
            ]
          },
          {
            "id": "19.03",
            "slug": "async-scheduled-e-tarefas-em-segundo-plano",
            "titulo": "@Async, @Scheduled e tarefas em segundo plano",
            "descricao": "Operações demoradas (relatórios, envio de webhooks) não podem segurar a requisição. É preciso executá-las fora dela, com segurança.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "19.02"
            ]
          },
          {
            "id": "19.04",
            "slug": "server-sent-events-e-uma-visao-de-websocket",
            "titulo": "Server-Sent Events (e uma visão de WebSocket)",
            "descricao": "Acompanhar o status do pedido em tempo real é um requisito comum. SSE resolve com HTTP simples, sem a complexidade do WebSocket.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "19.03",
              "04.01"
            ]
          },
          {
            "id": "19.05",
            "slug": "webflux-visao-geral-e-quando-usar",
            "titulo": "WebFlux: visão geral e quando usar",
            "descricao": "O leitor vai encontrar WebFlux em projetos e entrevistas e precisa saber quando ele realmente compensa, especialmente depois das virtual threads.",
            "nivel": "avancado",
            "prioridade": "complementar",
            "preRequisitos": [
              "19.02"
            ]
          },
          {
            "id": "19.06",
            "slug": "scoped-values-e-concorrencia-estruturada",
            "titulo": "Scoped values e concorrência estruturada",
            "descricao": "São as novas ferramentas do Java para propagar contexto e paralelizar chamadas com segurança, substituindo ThreadLocal e CompletableFuture encadeados.",
            "nivel": "avancado",
            "prioridade": "complementar",
            "preRequisitos": [
              "19.02"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 9,
    "titulo": "Padrões avançados de HTTP",
    "slug": "padroes-avancados-de-http",
    "pasta": "09-padroes-avancados-de-http",
    "icone": "Zap",
    "modulos": [
      {
        "id": "M20",
        "titulo": "Cache e concorrência no HTTP",
        "paginas": [
          {
            "id": "20.01",
            "slug": "cache-http-rfc-9111",
            "titulo": "Cache HTTP (RFC 9111)",
            "descricao": "O cache HTTP é a otimização mais barata que existe. Bem feito, reduz carga e latência; mal feito, mostra dados de outro usuário.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "02.04",
              "14.01"
            ]
          },
          {
            "id": "20.02",
            "slug": "etag-e-requisicoes-condicionais",
            "titulo": "ETag e requisições condicionais",
            "descricao": "Com ETag, o cliente revalida sem baixar de novo (304). Com o mesmo mecanismo se faz controle de concorrência (20.03).",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "20.01"
            ]
          },
          {
            "id": "20.03",
            "slug": "concorrencia-otimista-com-if-match-412-e-428",
            "titulo": "Concorrência otimista com If-Match (412 e 428)",
            "descricao": "Impede que dois usuários editando o mesmo produto ou estoque sobrescrevam as alterações um do outro (lost update) via HTTP, sem locks.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "20.02",
              "10.06"
            ]
          }
        ]
      },
      {
        "id": "M21",
        "titulo": "Confiabilidade nas escritas",
        "paginas": [
          {
            "id": "21.01",
            "slug": "idempotency-keys",
            "titulo": "Idempotency keys",
            "descricao": "Quando a rede falha depois que o servidor processou um POST, o cliente não sabe se o pedido foi criado. A idempotency key permite repetir com segurança, sem cobrança dupla.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "20.03",
              "10.03",
              "02.02"
            ]
          },
          {
            "id": "21.02",
            "slug": "operacoes-assincronas-e-de-longa-duracao-202-polling",
            "titulo": "Operações assíncronas e de longa duração (202 + polling)",
            "descricao": "Reembolsos e relatórios levam segundos ou minutos. Segurar a conexão causa timeouts em cascata. O padrão 202 dá ao cliente controle e visibilidade.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "19.03",
              "21.01"
            ]
          },
          {
            "id": "21.03",
            "slug": "webhooks-enviando-e-recebendo",
            "titulo": "Webhooks: enviando e recebendo",
            "descricao": "Integrações modernas são orientadas a eventos. Webhooks mal feitos perdem eventos, duplicam ações ou viram vetor de ataque.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "21.02",
              "19.03"
            ]
          }
        ]
      },
      {
        "id": "M22",
        "titulo": "Conteúdo e transferência",
        "paginas": [
          {
            "id": "22.01",
            "slug": "upload-de-arquivos",
            "titulo": "Upload de arquivos",
            "descricao": "Uploads sem limites derrubam o servidor ou permitem armazenar conteúdo malicioso.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "14.01"
            ]
          },
          {
            "id": "22.02",
            "slug": "download-streaming-e-range",
            "titulo": "Download, streaming e Range",
            "descricao": "Exportar 1 milhão de linhas carregando tudo em memória causa OutOfMemoryError. O streaming resolve.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "22.01",
              "10.05"
            ]
          },
          {
            "id": "22.03",
            "slug": "compressao",
            "titulo": "Compressão",
            "descricao": "JSON comprime muito bem (70–90%). É ganho de banda quase gratuito, com algumas armadilhas.",
            "nivel": "intermediario",
            "prioridade": "complementar",
            "preRequisitos": [
              "02.04"
            ]
          }
        ]
      },
      {
        "id": "M23",
        "titulo": "Proteção da API",
        "paginas": [
          {
            "id": "23.01",
            "slug": "rate-limiting-e-quotas",
            "titulo": "Rate limiting e quotas",
            "descricao": "Protege a API contra abuso e clientes descontrolados e garante uso justo entre parceiros. Também é contrato: o cliente precisa saber os limites.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "21.01",
              "09.04"
            ]
          },
          {
            "id": "23.02",
            "slug": "limites-de-payload-e-protecao-contra-abuso",
            "titulo": "Limites de payload e proteção contra abuso",
            "descricao": "JSON de 50 MB, arrays com 1 milhão de itens ou aninhamento profundo derrubam a API sem nenhum ataque sofisticado.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "23.01",
              "06.03"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 10,
    "titulo": "Consumindo outras APIs",
    "slug": "consumindo-outras-apis",
    "pasta": "10-consumindo-outras-apis",
    "icone": "Plug",
    "modulos": [
      {
        "id": "M24",
        "titulo": "Clientes HTTP no Spring",
        "paginas": [
          {
            "id": "24.01",
            "slug": "panorama-dos-clientes-http-no-spring",
            "titulo": "Panorama dos clientes HTTP no Spring",
            "descricao": "Há quatro opções, e uma está sendo depreciada. Escolher certo evita retrabalho.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "08.02",
              "19.02"
            ]
          },
          {
            "id": "24.02",
            "slug": "restclient-a-fundo",
            "titulo": "RestClient a fundo",
            "descricao": "É o cliente padrão para aplicações MVC. Saber tratar erros, configurar timeouts e interceptar requisições é obrigatório para integrar com terceiros.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "24.01"
            ]
          },
          {
            "id": "24.03",
            "slug": "http-interfaces-httpexchange-e-o-registro-de-clientes",
            "titulo": "HTTP interfaces (@HttpExchange) e o registro de clientes",
            "descricao": "Clientes declarativos reduzem boilerplate e, com o registro do Spring 7, ficam configuráveis por propriedade, como o Feign, mas nativos.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "24.02"
            ]
          },
          {
            "id": "24.04",
            "slug": "webclient-em-aplicacoes-mvc",
            "titulo": "WebClient em aplicações MVC",
            "descricao": "Ainda é a opção para streaming de respostas e para chamadas paralelas não bloqueantes, e aparece muito em código existente.",
            "nivel": "intermediario",
            "prioridade": "complementar",
            "preRequisitos": [
              "24.02",
              "19.05"
            ]
          },
          {
            "id": "24.05",
            "slug": "testando-clientes-http",
            "titulo": "Testando clientes HTTP",
            "descricao": "Integrações falham de formas que só aparecem com respostas reais (lentidão, 503, JSON inesperado). É preciso simulá-las.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "24.03",
              "11.04"
            ]
          }
        ]
      },
      {
        "id": "M25",
        "titulo": "Resiliência",
        "paginas": [
          {
            "id": "25.01",
            "slug": "timeouts-e-orcamento-de-tempo",
            "titulo": "Timeouts e orçamento de tempo",
            "descricao": "Sem timeout, uma dependência lenta trava threads e derruba a API inteira. É a primeira linha de defesa de qualquer integração.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "24.05",
              "19.01"
            ]
          },
          {
            "id": "25.02",
            "slug": "retry-quando-e-seguro-e-como-fazer",
            "titulo": "Retry: quando é seguro e como fazer",
            "descricao": "Retry ingênuo duplica cobranças e amplifica incidentes (retry storms). Feito certo, absorve falhas transitórias.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "25.01",
              "21.01"
            ]
          },
          {
            "id": "25.03",
            "slug": "circuit-breaker-bulkhead-e-limites-de-concorrencia",
            "titulo": "Circuit breaker, bulkhead e limites de concorrência",
            "descricao": "Evita que uma dependência fora do ar consuma todos os recursos e permite falhar rápido enquanto ela se recupera.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "25.02"
            ]
          },
          {
            "id": "25.04",
            "slug": "fallbacks-e-degradacao-graciosa",
            "titulo": "Fallbacks e degradação graciosa",
            "descricao": "Quando a FreteJá cai, o Bazar pode oferecer frete fixo em vez de impedir a compra. Isso é decisão de produto expressa em código.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "25.03"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 11,
    "titulo": "Segurança essencial para APIs",
    "slug": "seguranca-essencial-para-apis",
    "pasta": "11-seguranca-essencial-para-apis",
    "icone": "ShieldCheck",
    "modulos": [
      {
        "id": "M26",
        "titulo": "O essencial de segurança para quem constrói APIs",
        "paginas": [
          {
            "id": "26.01",
            "slug": "panorama-owasp-api-security-top-10",
            "titulo": "Panorama: OWASP API Security Top 10",
            "descricao": "A maioria dos incidentes em APIs vem de falhas de autorização e de exposição de dados, não de criptografia. O Top 10 dá o mapa.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "12.03",
              "23.01"
            ]
          },
          {
            "id": "26.02",
            "slug": "cors",
            "titulo": "CORS",
            "descricao": "É o erro mais comum quando um front-end consome a API. É frequentemente \"resolvido\" com *, abrindo brechas.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "26.01",
              "02.04"
            ]
          },
          {
            "id": "26.03",
            "slug": "autorizacao-por-objeto-exposicao-de-dados-e-validacao",
            "titulo": "Autorização por objeto, exposição de dados e validação de entrada",
            "descricao": "BOLA/IDOR (cliente A lendo o pedido do cliente B) é o risco nº 1 do OWASP API, e se resolve no design e no código da API, não no framework.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "26.01",
              "06.01"
            ]
          },
          {
            "id": "26.04",
            "slug": "resource-server-com-jwt-o-essencial",
            "titulo": "Resource Server com JWT (o essencial)",
            "descricao": "É a forma mais comum de proteger APIs. Esta página entrega o mínimo funcional e correto; o resto está na doc de segurança.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "26.03"
            ]
          },
          {
            "id": "26.05",
            "slug": "transporte-segredos-headers-de-seguranca-e-ssrf",
            "titulo": "Transporte, segredos, headers de segurança e SSRF",
            "descricao": "HTTPS mal configurado, segredos no Git e chamadas a URLs fornecidas pelo usuário (webhooks!) são falhas comuns e graves.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "26.04",
              "21.03"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 12,
    "titulo": "Observabilidade",
    "slug": "observabilidade",
    "pasta": "12-observabilidade",
    "icone": "Activity",
    "modulos": [
      {
        "id": "M27",
        "titulo": "Enxergando a API em produção",
        "paginas": [
          {
            "id": "27.01",
            "slug": "fundamentos-de-observabilidade",
            "titulo": "Fundamentos de observabilidade",
            "descricao": "Sem observabilidade a equipe descobre os problemas pelo cliente. Esta página define o vocabulário e o que medir.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "19.03"
            ]
          },
          {
            "id": "27.02",
            "slug": "spring-boot-actuator",
            "titulo": "Spring Boot Actuator",
            "descricao": "Expõe saúde, métricas e informações operacionais prontas, mas exposto sem cuidado vaza dados sensíveis.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "27.01"
            ]
          },
          {
            "id": "27.03",
            "slug": "health-checks-liveness-readiness-e-indicadores",
            "titulo": "Health checks: liveness, readiness e indicadores",
            "descricao": "Health checks errados reiniciam pods saudáveis em cascata ou mandam tráfego para instâncias que não estão prontas.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "27.02"
            ]
          },
          {
            "id": "27.04",
            "slug": "logs-estruturados",
            "titulo": "Logs estruturados",
            "descricao": "Logs em JSON podem ser pesquisados e correlacionados. Logs em texto livre viram grep em incidente.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "27.01"
            ]
          },
          {
            "id": "27.05",
            "slug": "metricas-com-micrometer",
            "titulo": "Métricas com Micrometer",
            "descricao": "Métricas alimentam dashboards, alertas e SLOs, e são a forma mais barata de saber se a API está saudável.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "27.02"
            ]
          },
          {
            "id": "27.06",
            "slug": "tracing-distribuido-com-opentelemetry",
            "titulo": "Tracing distribuído com OpenTelemetry",
            "descricao": "Com várias dependências (PagaFácil, FreteJá, banco, Redis), só o tracing mostra onde o tempo de uma requisição lenta foi gasto.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "27.05",
              "24.02"
            ]
          },
          {
            "id": "27.07",
            "slug": "correlation-id-e-contexto-de-requisicao",
            "titulo": "Correlation ID e contexto de requisição",
            "descricao": "Ligar o erro relatado pelo cliente ao log e ao trace exatos economiza horas de investigação.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "27.06",
              "27.04"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 13,
    "titulo": "Performance",
    "slug": "performance",
    "pasta": "13-performance",
    "icone": "Gauge",
    "modulos": [
      {
        "id": "M28",
        "titulo": "Medir, entender e otimizar",
        "paginas": [
          {
            "id": "28.01",
            "slug": "medindo-antes-de-otimizar-testes-de-carga",
            "titulo": "Medindo antes de otimizar: testes de carga",
            "descricao": "Otimizar sem medir é chute. Testes de carga revelam gargalos, regressões e a capacidade real.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "27.05"
            ]
          },
          {
            "id": "28.02",
            "slug": "pool-de-conexoes-hikaricp",
            "titulo": "Pool de conexões (HikariCP)",
            "descricao": "O pool do banco é o gargalo mais comum em APIs, especialmente com virtual threads, quando há mais concorrência do que conexões.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "28.01",
              "19.02"
            ]
          },
          {
            "id": "28.03",
            "slug": "cache-na-aplicacao-cacheable-caffeine-e-redis",
            "titulo": "Cache na aplicação: @Cacheable, Caffeine e Redis",
            "descricao": "Dados lidos muito e alterados pouco (catálogo, categorias) não precisam ir ao banco toda vez. Porém, cache errado serve dados obsoletos ou derruba o banco quando expira.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "28.01",
              "20.01"
            ]
          },
          {
            "id": "28.04",
            "slug": "consultas-e-paginacao-eficientes",
            "titulo": "Consultas e paginação eficientes",
            "descricao": "A maior parte da latência de uma API está no banco. Índices e queries corretos valem mais que qualquer tuning de JVM.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "28.01",
              "13.04",
              "10.05"
            ]
          },
          {
            "id": "28.05",
            "slug": "profiling-da-jvm",
            "titulo": "Profiling da JVM",
            "descricao": "Quando métricas e traces não explicam a lentidão (CPU, GC, contenção), o profiling mostra exatamente qual código está consumindo recursos.",
            "nivel": "avancado",
            "prioridade": "complementar",
            "preRequisitos": [
              "28.01"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 14,
    "titulo": "Produção e deploy",
    "slug": "producao-e-deploy",
    "pasta": "14-producao-e-deploy",
    "icone": "Container",
    "modulos": [
      {
        "id": "M29",
        "titulo": "Da máquina do desenvolvedor para produção",
        "paginas": [
          {
            "id": "29.01",
            "slug": "configuracao-externa-e-profiles",
            "titulo": "Configuração externa e profiles",
            "descricao": "A mesma imagem precisa rodar em desenvolvimento, homologação e produção mudando só a configuração. Segredos não podem estar no repositório.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": [
              "05.02"
            ]
          },
          {
            "id": "29.02",
            "slug": "containers-dockerfile-layered-jars-e-buildpacks",
            "titulo": "Containers: Dockerfile, layered jars e buildpacks",
            "descricao": "Imagens grandes e inseguras atrasam deploys e ampliam a superfície de ataque. A JVM precisa conhecer os limites do contêiner.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [
              "29.01"
            ]
          },
          {
            "id": "29.03",
            "slug": "imagem-nativa-com-graalvm",
            "titulo": "Imagem nativa com GraalVM",
            "descricao": "O startup em milissegundos e o baixo uso de memória importam em serverless e scale-to-zero, ao custo de build lento e restrições.",
            "nivel": "avancado",
            "prioridade": "complementar",
            "preRequisitos": [
              "29.02"
            ]
          },
          {
            "id": "29.04",
            "slug": "kubernetes-para-apis-probes-graceful-shutdown-e-rollouts",
            "titulo": "Kubernetes para APIs: probes, graceful shutdown e rollouts",
            "descricao": "Deploys sem graceful shutdown e sem probes corretas derrubam requisições em andamento a cada versão nova.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "29.02",
              "27.03"
            ]
          },
          {
            "id": "29.05",
            "slug": "api-gateway-e-bff",
            "titulo": "API Gateway e BFF",
            "descricao": "Decidir o que fica no gateway (TLS, autenticação, rate limit, roteamento) e o que fica na aplicação evita duplicação e lacunas de segurança.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "29.04",
              "23.01",
              "26.04"
            ]
          },
          {
            "id": "29.06",
            "slug": "ci-cd-para-apis",
            "titulo": "CI/CD para APIs",
            "descricao": "O pipeline é onde todas as garantias anteriores (testes, lint de contrato, breaking changes, imagem segura) viram algo automático.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "29.02",
              "17.04",
              "18.05"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 15,
    "titulo": "Arquitetura",
    "slug": "arquitetura",
    "pasta": "15-arquitetura",
    "icone": "Boxes",
    "modulos": [
      {
        "id": "M30",
        "titulo": "APIs no contexto da arquitetura",
        "paginas": [
          {
            "id": "30.01",
            "slug": "monolito-modular-com-spring-modulith",
            "titulo": "Monólito modular com Spring Modulith",
            "descricao": "A maioria dos sistemas deveria começar como monólito bem modularizado. O Spring Modulith verifica as fronteiras e prepara uma futura extração.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "05.02",
              "10.03"
            ]
          },
          {
            "id": "30.02",
            "slug": "microsservicos-quando-nao-usar-e-como-se-comunicam",
            "titulo": "Microsserviços: quando (não) usar e como se comunicam",
            "descricao": "Microsserviços resolvem problemas organizacionais ao custo de complexidade distribuída. Escolhê-los cedo demais é um erro comum e caro.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "30.01",
              "25.03"
            ]
          },
          {
            "id": "30.03",
            "slug": "eventos-e-mensageria-introducao",
            "titulo": "Eventos e mensageria: introdução",
            "descricao": "Eventos desacoplam serviços no tempo e permitem integrações escaláveis. Toda API sênior convive com mensageria.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "30.02"
            ]
          },
          {
            "id": "30.04",
            "slug": "padrao-outbox",
            "titulo": "Padrão Outbox",
            "descricao": "\"salvar no banco e publicar o evento\" sem Outbox perde ou duplica eventos (dual write). É a base para webhooks e eventos confiáveis.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "30.03",
              "10.03",
              "19.03"
            ]
          },
          {
            "id": "30.05",
            "slug": "consistencia-eventual-e-sagas",
            "titulo": "Consistência eventual e sagas",
            "descricao": "Sem transação distribuída, processos como \"reservar estoque → cobrar → confirmar\" precisam de compensação explícita, e a API precisa comunicar estados intermediários.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "30.04"
            ]
          },
          {
            "id": "30.06",
            "slug": "grpc-com-spring-boot-4-1",
            "titulo": "gRPC com Spring Boot 4.1",
            "descricao": "Para comunicação interna de alto volume, gRPC oferece contrato forte e desempenho. Desde o Boot 4.1 o suporte é oficial.",
            "nivel": "avancado",
            "prioridade": "complementar",
            "preRequisitos": [
              "30.02",
              "04.01"
            ]
          },
          {
            "id": "30.07",
            "slug": "graphql-com-spring-for-graphql",
            "titulo": "GraphQL com Spring for GraphQL",
            "descricao": "Para front-ends com telas muito variadas, GraphQL pode substituir dezenas de endpoints sob medida, mas traz outros problemas.",
            "nivel": "avancado",
            "prioridade": "complementar",
            "preRequisitos": [
              "04.01",
              "10.04"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": 16,
    "titulo": "Projeto final, o Bazar",
    "slug": "projeto-final-o-bazar",
    "pasta": "16-projeto-final-o-bazar",
    "icone": "Store",
    "modulos": [
      {
        "id": "M31",
        "titulo": "Construindo o Bazar do zero à produção",
        "paginas": [
          {
            "id": "31.01",
            "slug": "visao-requisitos-e-decisoes-arquiteturais",
            "titulo": "Visão, requisitos e decisões arquiteturais",
            "descricao": "Projetos profissionais começam por requisitos (inclusive os não funcionais) e decisões registradas, não pelo código.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [],
            "preRequisitosNota": "todas as Partes 1–15 essenciais"
          },
          {
            "id": "31.02",
            "slug": "o-contrato-primeiro",
            "titulo": "O contrato primeiro",
            "descricao": "Aplica o API-first de ponta a ponta.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "31.01",
              "17.03",
              "17.04"
            ]
          },
          {
            "id": "31.03",
            "slug": "catalogo",
            "titulo": "Catálogo",
            "descricao": "Consolida CRUD, filtros, keyset, PATCH, ETag, cache e upload em um módulo real.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "31.02"
            ]
          },
          {
            "id": "31.04",
            "slug": "clientes",
            "titulo": "Clientes",
            "descricao": "Consolida dados sensíveis, BOLA, validação customizada e segurança essencial.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "31.03"
            ]
          },
          {
            "id": "31.05",
            "slug": "estoque-frete-e-pedidos",
            "titulo": "Estoque, frete e pedidos",
            "descricao": "É o coração transacional, com concorrência, idempotência e máquina de estados.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "31.04"
            ]
          },
          {
            "id": "31.06",
            "slug": "pagamentos-e-reembolsos",
            "titulo": "Pagamentos e reembolsos",
            "descricao": "Integração externa com dinheiro envolvido, que exige todos os padrões de resiliência e idempotência.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "31.05"
            ]
          },
          {
            "id": "31.07",
            "slug": "eventos-outbox-webhooks-e-tempo-real",
            "titulo": "Eventos, Outbox, webhooks e tempo real",
            "descricao": "Garante que nenhum evento de pedido se perca e que parceiros e clientes sejam notificados de forma confiável.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "31.06"
            ]
          },
          {
            "id": "31.08",
            "slug": "relatorios-assincronos-e-exportacao",
            "titulo": "Relatórios assíncronos e exportação",
            "descricao": "Consolida jobs em segundo plano, operações longas e streaming.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "31.07"
            ]
          },
          {
            "id": "31.09",
            "slug": "evoluindo-v2-do-produto-e-depreciacao-da-v1",
            "titulo": "Evoluindo: v2 do produto e depreciação da v1",
            "descricao": "Exercita o ciclo de vida completo de uma mudança incompatível.",
            "nivel": "avancado",
            "prioridade": "importante",
            "preRequisitos": [
              "31.08"
            ]
          },
          {
            "id": "31.10",
            "slug": "observabilidade-carga-e-deploy",
            "titulo": "Observabilidade, carga e deploy",
            "descricao": "Uma API só está pronta quando é operável: medida, observada e implantada sem downtime.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "31.09"
            ]
          },
          {
            "id": "31.11",
            "slug": "revisao-final-o-checklist-da-api-senior",
            "titulo": "Revisão final: o checklist da API sênior",
            "descricao": "Fecha o aprendizado com uma revisão crítica do próprio projeto, como seria feito em uma API review real.",
            "nivel": "avancado",
            "prioridade": "essencial",
            "preRequisitos": [
              "31.10"
            ]
          }
        ]
      }
    ]
  },
  {
    "numero": null,
    "titulo": "Apêndices",
    "slug": "apendices",
    "pasta": "17-apendices",
    "icone": "BookOpen",
    "modulos": [
      {
        "id": "M32",
        "titulo": "Material de consulta",
        "paginas": [
          {
            "id": "32.01",
            "slug": "apendice-a-checklist-de-revisao-de-api",
            "titulo": "Apêndice A: Checklist de revisão de API",
            "descricao": "É a ferramenta prática para revisar qualquer API (a própria ou de outro time) em minutos.",
            "nivel": "intermediario",
            "prioridade": "essencial",
            "preRequisitos": [],
            "preRequisitosNota": "nenhum (cada item aponta para a página correspondente)"
          },
          {
            "id": "32.02",
            "slug": "apendice-b-referencia-rapida-de-status-codes-metodos",
            "titulo": "Apêndice B: Referência rápida de status codes, métodos e headers",
            "descricao": "É a consulta rápida do dia a dia.",
            "nivel": "basico",
            "prioridade": "essencial",
            "preRequisitos": []
          },
          {
            "id": "32.03",
            "slug": "apendice-c-migrando-do-spring-boot-3-para-o-4",
            "titulo": "Apêndice C: Migrando do Spring Boot 3 para o 4",
            "descricao": "O leitor provavelmente trabalha com projetos em Boot 3.x. A migração tem armadilhas específicas para APIs.",
            "nivel": "intermediario",
            "prioridade": "importante",
            "preRequisitos": [
              "05.01",
              "06.03"
            ]
          },
          {
            "id": "32.04",
            "slug": "apendice-d-glossario",
            "titulo": "Apêndice D: Glossário",
            "descricao": "Reúne em um só lugar os termos em português e inglês usados na doc.",
            "nivel": "basico",
            "prioridade": "importante",
            "preRequisitos": []
          }
        ]
      }
    ]
  }
]
