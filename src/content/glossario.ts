/**
 * Glossário da documentação. Cada termo pode ser marcado no texto com
 * <Term id="...">texto</Term> e aparece na página do Apêndice D (32.04).
 *
 * `pagina` é o ID da página onde o termo é explicado pela primeira vez.
 * A fase 3 completa e revisa esta lista conforme escreve as páginas.
 */

export interface TermoGlossario {
  id: string
  termo: string
  /** Termo original em inglês, quando o termo principal está em português */
  ingles?: string
  definicao: string
  pagina?: string
}

export const glossario: TermoGlossario[] = [
  { id: 'api', termo: 'API', ingles: 'Application Programming Interface', definicao: 'Contrato que permite a um programa usar as funcionalidades de outro sem conhecer sua implementação.', pagina: '01.01' },
  { id: 'rest', termo: 'REST', ingles: 'Representational State Transfer', definicao: 'Estilo arquitetural definido por Roy Fielding, baseado em recursos, representações e uma interface uniforme sobre HTTP.', pagina: '03.01' },
  { id: 'recurso', termo: 'Recurso', ingles: 'resource', definicao: 'Qualquer coisa identificável por uma URI que a API expõe: um produto, um pedido, uma coleção de pedidos.', pagina: '03.02' },
  { id: 'representacao', termo: 'Representação', ingles: 'representation', definicao: 'O estado de um recurso em um formato concreto (ex.: JSON) enviado em uma mensagem HTTP. O recurso é um só; as representações podem ser várias.', pagina: '03.02' },
  { id: 'uri', termo: 'URI', ingles: 'Uniform Resource Identifier', definicao: 'Identificador de um recurso, com esquema, host, caminho e query (RFC 3986).', pagina: '02.01' },
  { id: 'endpoint', termo: 'Endpoint', definicao: 'Combinação de método HTTP e caminho que a API atende, como GET /v1/produtos/{id}.', pagina: '05.03' },
  { id: 'metodo-seguro', termo: 'Método seguro', ingles: 'safe method', definicao: 'Método que não altera o estado do servidor do ponto de vista do cliente, como GET e HEAD.', pagina: '02.02' },
  { id: 'idempotencia', termo: 'Idempotência', ingles: 'idempotency', definicao: 'Propriedade de uma operação cujo efeito no servidor é o mesmo se executada uma ou várias vezes. PUT e DELETE são idempotentes; POST não é.', pagina: '02.02' },
  { id: 'idempotency-key', termo: 'Idempotency-Key', definicao: 'Header com um identificador único enviado pelo cliente para que o servidor reconheça e não reexecute uma requisição repetida (draft da IETF).', pagina: '21.01' },
  { id: 'status-code', termo: 'Status code', definicao: 'Número de três dígitos na resposta HTTP que resume o resultado: 2xx sucesso, 3xx redirecionamento, 4xx erro do cliente, 5xx erro do servidor.', pagina: '02.03' },
  { id: 'header', termo: 'Header', ingles: 'campo de cabeçalho', definicao: 'Par nome/valor que carrega metadados da mensagem HTTP, como Content-Type, Location e Cache-Control.', pagina: '02.04' },
  { id: 'content-negotiation', termo: 'Negociação de conteúdo', ingles: 'content negotiation', definicao: 'Mecanismo em que o cliente indica os formatos aceitos (Accept) e o servidor escolhe a representação mais adequada.', pagina: '02.05' },
  { id: 'media-type', termo: 'Media type', definicao: 'Identificador do formato de um conteúdo, como application/json ou application/problem+json.', pagina: '02.05' },
  { id: 'hateoas', termo: 'HATEOAS', ingles: 'Hypermedia as the Engine of Application State', definicao: 'Restrição REST em que as respostas trazem links para as próximas ações possíveis, guiando o cliente.', pagina: '03.04' },
  { id: 'dto', termo: 'DTO', ingles: 'Data Transfer Object', definicao: 'Objeto que define o formato de entrada ou saída da API, separado da entidade de persistência.', pagina: '06.01' },
  { id: 'problem-details', termo: 'Problem Details', definicao: 'Formato padronizado de erros HTTP (RFC 9457), com os campos type, title, status, detail e instance em application/problem+json.', pagina: '08.01' },
  { id: 'dispatcher-servlet', termo: 'DispatcherServlet', definicao: 'Servlet central do Spring MVC que recebe cada requisição e a encaminha para o handler (controller) correto.', pagina: '09.01' },
  { id: 'n-mais-1', termo: 'Problema N+1', ingles: 'N+1 query problem', definicao: 'Quando carregar uma lista dispara uma consulta para a lista e mais uma para cada item, multiplicando o acesso ao banco.', pagina: '10.04' },
  { id: 'lock-otimista', termo: 'Lock otimista', ingles: 'optimistic locking', definicao: 'Controle de concorrência que detecta, por uma versão, se o registro mudou desde a leitura, em vez de bloqueá-lo.', pagina: '10.06' },
  { id: 'lost-update', termo: 'Lost update', definicao: 'Atualização perdida: dois clientes leem o mesmo estado e o último a gravar apaga, sem saber, a alteração do outro.', pagina: '10.06' },
  { id: 'paginacao-cursor', termo: 'Paginação por cursor', ingles: 'keyset pagination', definicao: 'Paginação que usa a posição do último item visto (cursor) em vez de um deslocamento, estável e eficiente em coleções grandes.', pagina: '13.04' },
  { id: 'json-merge-patch', termo: 'JSON Merge Patch', definicao: 'Formato de atualização parcial (RFC 7396) em que o corpo descreve só os campos alterados; null remove o campo.', pagina: '14.02' },
  { id: 'json-patch', termo: 'JSON Patch', definicao: 'Formato de atualização parcial (RFC 6902) que descreve uma lista de operações (add, remove, replace...) sobre o documento.', pagina: '14.02' },
  { id: 'soft-delete', termo: 'Soft delete', definicao: 'Remoção lógica: o registro é marcado como removido em vez de apagado do banco.', pagina: '14.03' },
  { id: 'uuid-v7', termo: 'UUID v7', definicao: 'UUID ordenável por tempo (RFC 9562): não pode ser adivinhado como um id sequencial e não fragmenta índices como o UUID v4.', pagina: '15.03' },
  { id: 'tolerant-reader', termo: 'Tolerant reader', definicao: 'Prática de consumidores que ignoram campos desconhecidos e toleram valores novos, permitindo que a API evolua sem quebrá-los.', pagina: '15.04' },
  { id: 'openapi', termo: 'OpenAPI', definicao: 'Especificação para descrever APIs HTTP em YAML ou JSON: endpoints, parâmetros, esquemas e respostas.', pagina: '17.01' },
  { id: 'api-first', termo: 'API-first', definicao: 'Abordagem em que o contrato (OpenAPI) é desenhado e revisado antes do código.', pagina: '17.03' },
  { id: 'breaking-change', termo: 'Breaking change', ingles: 'mudança incompatível', definicao: 'Alteração no contrato que faz clientes existentes pararem de funcionar.', pagina: '18.01' },
  { id: 'etag', termo: 'ETag', definicao: 'Header com um identificador da versão de uma representação, usado em cache (If-None-Match) e em concorrência (If-Match).', pagina: '20.02' },
  { id: 'requisicao-condicional', termo: 'Requisição condicional', ingles: 'conditional request', definicao: 'Requisição com pré-condições (If-Match, If-None-Match) que o servidor avalia antes de executar.', pagina: '20.02' },
  { id: 'webhook', termo: 'Webhook', definicao: 'Chamada HTTP que um sistema faz para uma URL registrada por outro, para avisar que um evento aconteceu.', pagina: '21.03' },
  { id: 'sse', termo: 'SSE', ingles: 'Server-Sent Events', definicao: 'Fluxo de eventos do servidor para o cliente sobre uma conexão HTTP aberta (text/event-stream).', pagina: '19.04' },
  { id: 'rate-limiting', termo: 'Rate limiting', ingles: 'limite de taxa', definicao: 'Limite de quantas requisições um cliente pode fazer em um intervalo; ao exceder, a API responde 429.', pagina: '23.01' },
  { id: 'timeout', termo: 'Timeout', definicao: 'Tempo máximo de espera por uma operação (conexão, leitura, chamada inteira) antes de desistir.', pagina: '25.01' },
  { id: 'retry', termo: 'Retry', ingles: 'retentativa', definicao: 'Repetir automaticamente uma chamada que falhou. Só é seguro em operações idempotentes ou protegidas por chave de idempotência.', pagina: '25.02' },
  { id: 'backoff', termo: 'Backoff exponencial', ingles: 'exponential backoff', definicao: 'Estratégia de retry em que o intervalo entre tentativas cresce (1s, 2s, 4s...), normalmente com jitter (variação aleatória).', pagina: '25.02' },
  { id: 'circuit-breaker', termo: 'Circuit breaker', definicao: 'Padrão que interrompe chamadas a uma dependência que está falhando, dando tempo para ela se recuperar e falhando rápido enquanto isso.', pagina: '25.03' },
  { id: 'bola', termo: 'BOLA', ingles: 'Broken Object Level Authorization', definicao: 'Falha em que a API não verifica se o usuário pode acessar o objeto pedido (ex.: trocar o id na URL e ver o pedido de outra pessoa).', pagina: '26.03' },
  { id: 'ssrf', termo: 'SSRF', ingles: 'Server-Side Request Forgery', definicao: 'Ataque em que o servidor é induzido a fazer requisições para destinos escolhidos pelo atacante, como a rede interna.', pagina: '26.05' },
  { id: 'slo', termo: 'SLO', ingles: 'Service Level Objective', definicao: 'Meta de confiabilidade medida por um indicador (SLI), como "99,9% das requisições em menos de 300 ms".', pagina: '27.01' },
  { id: 'trace', termo: 'Trace distribuído', ingles: 'distributed trace', definicao: 'Registro do caminho de uma requisição por vários serviços, formado por spans ligados por um traceId.', pagina: '27.06' },
  { id: 'outbox', termo: 'Outbox', ingles: 'transactional outbox', definicao: 'Padrão em que eventos são gravados em uma tabela na mesma transação dos dados e publicados depois, garantindo que nenhum se perca.', pagina: '30.04' },
  { id: 'monolito-modular', termo: 'Monólito modular', definicao: 'Aplicação implantada como uma unidade, mas dividida em módulos com fronteiras explícitas e verificáveis.', pagina: '30.01' },
  { id: 'cacheavel', termo: 'Método cacheável', ingles: 'cacheable method', definicao: 'Método cujas respostas podem ser guardadas e reutilizadas por caches. Na prática, GET, HEAD e QUERY.', pagina: '02.02' },
  { id: 'codificacao-percentual', termo: 'Codificação percentual', ingles: 'percent-encoding', definicao: 'Forma de escrever na URI caracteres especiais ou não ASCII: cada byte vira % seguido de dois dígitos hexadecimais (espaço = %20, é = %C3%A9).', pagina: '02.01' },
  { id: 'hop-by-hop', termo: 'Header hop-by-hop', definicao: 'Header que vale só para a conexão entre dois pontos vizinhos (Connection, Keep-Alive, Transfer-Encoding, Upgrade) e não é repassado por proxies.', pagina: '02.04' },
  { id: 'structured-fields', termo: 'Structured Field Values', definicao: 'Sintaxe comum para headers HTTP modernos (RFC 9651), com itens, listas, dicionários e tipos definidos, usada por Deprecation e RateLimit.', pagina: '02.04' },
  { id: 'q-value', termo: 'q-value', definicao: 'Peso de 0 a 1 que o cliente associa a cada opção em headers como Accept e Accept-Language (q=0 significa "não aceito").', pagina: '02.05' },
  { id: 'stateless', termo: 'Stateless', ingles: 'sem estado', definicao: 'Restrição em que cada requisição traz tudo o que é preciso para ser entendida, sem depender de estado da conversa guardado no servidor. Não proíbe guardar o estado dos recursos.', pagina: '02.07' },
  { id: 'cookie', termo: 'Cookie', definicao: 'Pequeno dado que o servidor envia com Set-Cookie e o navegador devolve no header Cookie a cada requisição para o mesmo site (RFC 6265).', pagina: '02.07' },
  { id: 'sticky-session', termo: 'Sticky session', ingles: 'afinidade de sessão', definicao: 'Configuração do balanceador que manda cada cliente sempre para a mesma instância, para contornar sessões guardadas em memória.', pagina: '02.07' },
  { id: 'head-of-line-blocking', termo: 'Bloqueio head-of-line', ingles: 'head-of-line blocking', definicao: 'Quando um item lento no início de uma fila atrasa todos os que vêm atrás, como respostas em ordem numa conexão HTTP/1.1 ou pacotes perdidos no TCP.', pagina: '02.08' },
  { id: 'multiplexacao', termo: 'Multiplexação', ingles: 'multiplexing', definicao: 'No HTTP/2 e HTTP/3, transmissão de várias trocas (streams) intercaladas na mesma conexão, sem que uma espere a outra.', pagina: '02.08' },
  { id: 'quic', termo: 'QUIC', definicao: 'Protocolo de transporte sobre UDP com TLS 1.3 integrado (RFC 9000), base do HTTP/3: sem bloqueio head-of-line entre streams e com migração de conexão.', pagina: '02.08' },
  { id: 'interface-uniforme', termo: 'Interface uniforme', ingles: 'uniform interface', definicao: 'Restrição central do REST: todos os componentes conversam pela mesma interface genérica (no HTTP, métodos, status, headers e media types), com identificação de recursos, manipulação por representações, mensagens autodescritivas e HATEOAS.', pagina: '03.01' },
  { id: 'hal', termo: 'HAL', ingles: 'Hypertext Application Language', definicao: 'Formato de hipermídia para JSON (application/hal+json) que acrescenta _links (relações e URIs) e _embedded (recursos embutidos) às representações.', pagina: '03.04' },
  { id: 'affordance', termo: 'Affordance', definicao: 'Ação que uma representação oferece ao cliente, como "cancelar" num pedido CRIADO. Muda conforme o estado do recurso.', pagina: '03.04' },
  { id: 'grpc', termo: 'gRPC', definicao: 'Framework de RPC com contrato em Protocol Buffers, mensagens binárias e transporte HTTP/2, com suporte a streaming. Usado principalmente entre serviços internos.', pagina: '04.01' },
  { id: 'graphql', termo: 'GraphQL', definicao: 'Linguagem de consulta e estilo de API em que o cliente escolhe, numa única requisição, exatamente os campos e relações de que precisa, a partir de um esquema tipado.', pagina: '04.01' },
  { id: 'long-polling', termo: 'Long polling', definicao: 'Técnica em que o servidor segura a resposta até haver uma novidade ou um tempo limite, e o cliente faz nova requisição em seguida.', pagina: '04.01' },
]
