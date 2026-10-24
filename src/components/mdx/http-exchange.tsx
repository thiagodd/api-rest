import { ArrowDown, ArrowRight } from 'lucide-react'
import { MetodoBadge, StatusBadge } from '@/components/badges'
import { nomeStatus } from '@/lib/http'
import { BotaoCopiar } from './code-block'
import { formatarCorpo, JsonRealce } from './json-realce'

type Headers = Record<string, string | number>

export interface RequisicaoHttp {
  method: string
  url: string
  headers?: Headers
  body?: unknown
}

export interface RespostaHttp {
  status: number
  headers?: Headers
  body?: unknown
}

function Cabecalhos({ headers }: { headers?: Headers }) {
  if (!headers || !Object.keys(headers).length) return null
  return (
    <div className="space-y-0.5">
      {Object.entries(headers).map(([nome, valor]) => (
        <div key={nome} className="break-all">
          <span className="text-sky-700 dark:text-sky-300">{nome}</span>
          <span className="text-muted-foreground">: </span>
          <span>{valor}</span>
        </div>
      ))}
    </div>
  )
}

function Corpo({ body }: { body?: unknown }) {
  const { texto, json } = formatarCorpo(body)
  if (!texto) return null
  return <div className="mt-3 border-t border-dashed pt-3 whitespace-pre">{json ? <JsonRealce codigo={texto} /> : texto}</div>
}

function textoRequisicao(r: RequisicaoHttp) {
  const linhas = [`${r.method.toUpperCase()} ${r.url} HTTP/1.1`, ...Object.entries(r.headers ?? {}).map(([k, v]) => `${k}: ${v}`)]
  const { texto } = formatarCorpo(r.body)
  return texto ? `${linhas.join('\n')}\n\n${texto}` : linhas.join('\n')
}

function textoResposta(r: RespostaHttp) {
  const linhas = [`HTTP/1.1 ${r.status} ${nomeStatus(r.status)}`, ...Object.entries(r.headers ?? {}).map(([k, v]) => `${k}: ${v}`)]
  const { texto } = formatarCorpo(r.body)
  return texto ? `${linhas.join('\n')}\n\n${texto}` : linhas.join('\n')
}

/** Requisição e resposta HTTP completas, como trafegam na rede. */
export function HttpExchange({
  request,
  response,
  title,
  children,
}: {
  request: RequisicaoHttp
  response?: RespostaHttp
  title?: string
  /** Explicação opcional, mostrada abaixo da troca */
  children?: React.ReactNode
}) {
  return (
    <div role="figure" aria-label={title ?? 'Troca HTTP'} className="@container my-6 overflow-hidden rounded-lg border bg-code">
      {title && <div className="not-prose border-b bg-code-header px-4 py-2 text-xs font-medium text-muted-foreground">{title}</div>}
      <div className="not-prose grid @4xl:grid-cols-[1fr_auto_1fr]">
        <section aria-label="Requisição" className="min-w-0">
          <div className="flex items-center gap-2 border-b px-4 py-2">
            <span className="text-[11px] font-semibold tracking-wide text-muted-foreground uppercase">Requisição</span>
            <BotaoCopiar obterTexto={() => textoRequisicao(request)} className="ml-auto" />
          </div>
          <div className="scrollbar-fina overflow-x-auto px-4 py-3 font-mono text-[0.8rem] leading-relaxed">
            <div className="mb-2 flex items-center gap-2 whitespace-nowrap">
              <MetodoBadge metodo={request.method} />
              <span className="font-semibold">{request.url}</span>
            </div>
            <Cabecalhos headers={request.headers} />
            <Corpo body={request.body} />
          </div>
        </section>

        {response && (
          <>
            <div className="flex items-center justify-center border-y bg-code-header py-1 text-muted-foreground @4xl:border-x @4xl:border-y-0 @4xl:px-1.5" aria-hidden>
              <ArrowDown className="size-4 @4xl:hidden" />
              <ArrowRight className="hidden size-4 @4xl:block" />
            </div>
            <section aria-label="Resposta" className="min-w-0">
              <div className="flex items-center gap-2 border-b px-4 py-2">
                <span className="text-[11px] font-semibold tracking-wide text-muted-foreground uppercase">Resposta</span>
                <BotaoCopiar obterTexto={() => textoResposta(response)} className="ml-auto" />
              </div>
              <div className="scrollbar-fina overflow-x-auto px-4 py-3 font-mono text-[0.8rem] leading-relaxed">
                <div className="mb-2">
                  <StatusBadge status={response.status} />
                </div>
                <Cabecalhos headers={response.headers} />
                <Corpo body={response.body} />
              </div>
            </section>
          </>
        )}
      </div>
      {children && <div className="prose-doc prose-sm border-t bg-background/40 px-4 py-3 [&>*:first-child]:mt-0 [&>*:last-child]:mb-0">{children}</div>}
    </div>
  )
}
