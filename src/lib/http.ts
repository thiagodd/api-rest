/** Dados de referência de HTTP usados pelos componentes (status, métodos, cores). */

export type MetodoHttp = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'QUERY' | 'HEAD' | 'OPTIONS'

export const coresMetodo: Record<string, string> = {
  GET: 'var(--metodo-get)',
  POST: 'var(--metodo-post)',
  PUT: 'var(--metodo-put)',
  PATCH: 'var(--metodo-patch)',
  DELETE: 'var(--metodo-delete)',
  QUERY: 'var(--metodo-query)',
}

export function corMetodo(metodo: string) {
  return coresMetodo[metodo.toUpperCase()] ?? 'var(--metodo-outro)'
}

export function corStatus(status: number) {
  const classe = Math.floor(status / 100)
  return `var(--status-${classe >= 1 && classe <= 5 ? classe : 1}xx)`
}

export interface InfoStatus {
  nome: string
  quando: string
}

/** Frases de status (RFC 9110) e quando usar cada uma em uma API. */
export const statusHttp: Record<number, InfoStatus> = {
  100: { nome: 'Continue', quando: 'O servidor aceitou os headers e o cliente pode enviar o corpo (Expect: 100-continue).' },
  200: { nome: 'OK', quando: 'Sucesso com corpo na resposta: leituras (GET) e ações que devolvem um resultado.' },
  201: { nome: 'Created', quando: 'Um recurso foi criado. Inclua o header Location com a URI do novo recurso.' },
  202: { nome: 'Accepted', quando: 'O pedido foi aceito, mas o processamento continua em segundo plano. Aponte para um recurso de status.' },
  204: { nome: 'No Content', quando: 'Sucesso sem corpo: DELETE, ou PUT/PATCH quando o cliente não precisa da representação.' },
  206: { nome: 'Partial Content', quando: 'Resposta a um pedido com Range: só um trecho do arquivo foi enviado.' },
  301: { nome: 'Moved Permanently', quando: 'A URI mudou para sempre. Clientes podem trocar o método para GET; prefira 308 em APIs.' },
  302: { nome: 'Found', quando: 'Redirecionamento temporário histórico e ambíguo quanto ao método. Evite em APIs.' },
  303: { nome: 'See Other', quando: 'O resultado está em outra URI e deve ser buscado com GET (ex.: operação assíncrona concluída).' },
  304: { nome: 'Not Modified', quando: 'A representação em cache do cliente continua válida (If-None-Match / If-Modified-Since).' },
  307: { nome: 'Temporary Redirect', quando: 'Redirecionamento temporário que preserva o método e o corpo.' },
  308: { nome: 'Permanent Redirect', quando: 'Redirecionamento permanente que preserva o método e o corpo.' },
  400: { nome: 'Bad Request', quando: 'A requisição está malformada: JSON inválido, tipo errado, parâmetro impossível de interpretar.' },
  401: { nome: 'Unauthorized', quando: 'Falta autenticação ou ela é inválida. Envie WWW-Authenticate.' },
  403: { nome: 'Forbidden', quando: 'O cliente está autenticado, mas não tem permissão para esta ação.' },
  404: { nome: 'Not Found', quando: 'O recurso não existe, ou você não quer revelar que ele existe.' },
  405: { nome: 'Method Not Allowed', quando: 'O recurso existe, mas não aceita este método. Envie o header Allow.' },
  406: { nome: 'Not Acceptable', quando: 'Nenhuma representação atende ao header Accept do cliente.' },
  409: { nome: 'Conflict', quando: 'A requisição conflita com o estado atual do recurso (ex.: cancelar um pedido já enviado).' },
  410: { nome: 'Gone', quando: 'O recurso existia e foi removido de propósito, sem endereço novo (ex.: versão desligada).' },
  412: { nome: 'Precondition Failed', quando: 'Uma pré-condição (If-Match) falhou: o recurso mudou desde a última leitura.' },
  413: { nome: 'Content Too Large', quando: 'O corpo excede o limite aceito (uploads, lotes grandes demais).' },
  415: { nome: 'Unsupported Media Type', quando: 'O Content-Type enviado não é aceito por este endpoint.' },
  422: { nome: 'Unprocessable Content', quando: 'O JSON é válido, mas viola regras de validação ou de negócio.' },
  428: { nome: 'Precondition Required', quando: 'O servidor exige uma requisição condicional (If-Match) para evitar lost updates.' },
  429: { nome: 'Too Many Requests', quando: 'O cliente excedeu o limite de requisições. Envie Retry-After.' },
  500: { nome: 'Internal Server Error', quando: 'Erro inesperado no servidor. Nunca exponha a stack trace.' },
  501: { nome: 'Not Implemented', quando: 'O servidor não reconhece ou não implementa o método.' },
  502: { nome: 'Bad Gateway', quando: 'Um gateway ou proxy recebeu uma resposta inválida do serviço de destino.' },
  503: { nome: 'Service Unavailable', quando: 'O serviço está sobrecarregado ou em manutenção. Envie Retry-After quando souber.' },
  504: { nome: 'Gateway Timeout', quando: 'Um gateway ou proxy não recebeu resposta a tempo do serviço de destino.' },
}

export function nomeStatus(status: number) {
  return statusHttp[status]?.nome ?? ''
}
