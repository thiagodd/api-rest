import { MetodoBadge, StatusBadge } from '@/components/badges'
import { corMetodo, nomeStatus } from '@/lib/http'
import { BotaoCopiar } from './code-block'

export interface ParametroEndpoint {
  name: string
  in: 'path' | 'query' | 'header' | 'body'
  type?: string
  required?: boolean
  description?: React.ReactNode
}

export interface RespostaEndpoint {
  status: number
  description?: React.ReactNode
}

const rotulosLocal: Record<ParametroEndpoint['in'], string> = {
  path: 'Caminho',
  query: 'Query',
  header: 'Headers',
  body: 'Corpo',
}

/** Destaca `{variaveis}` do caminho. */
function Caminho({ path }: { path: string }) {
  return (
    <>
      {path.split(/(\{[^}]+\})/g).map((trecho, i) =>
        trecho.startsWith('{') ? (
          <span key={i} className="rounded bg-muted px-0.5 text-primary">
            {trecho}
          </span>
        ) : (
          <span key={i}>{trecho}</span>
        ),
      )}
    </>
  )
}

/** Cartão de endpoint no estilo de referência de API. */
export function Endpoint({
  method,
  path,
  summary,
  params = [],
  responses = [],
  deprecated = false,
  children,
}: {
  method: string
  path: string
  summary?: string
  params?: ParametroEndpoint[]
  responses?: RespostaEndpoint[]
  deprecated?: boolean
  /** Descrição e exemplos (ex.: <HttpExchange>, <CodeTabs>) */
  children?: React.ReactNode
}) {
  const cor = corMetodo(method)
  const grupos = (['path', 'query', 'header', 'body'] as const)
    .map((local) => ({ local, itens: params.filter((p) => p.in === local) }))
    .filter((g) => g.itens.length)

  return (
    <section className="my-8 overflow-hidden rounded-xl border bg-card shadow-xs" style={{ borderTopColor: cor, borderTopWidth: 3 }}>
      <header className="not-prose flex flex-wrap items-center gap-x-3 gap-y-1 border-b px-4 py-3">
        <MetodoBadge metodo={method} className="text-xs" />
        <code className={`min-w-0 font-mono text-sm font-semibold break-all ${deprecated ? 'line-through opacity-70' : ''}`}>
          <Caminho path={path} />
        </code>
        {deprecated && <span className="rounded border border-amber-500/40 bg-amber-500/10 px-1.5 text-[11px] font-medium text-amber-700 dark:text-amber-300">Depreciado</span>}
        <BotaoCopiar obterTexto={() => `${method.toUpperCase()} ${path}`} className="ml-auto" />
        {summary && <p className="w-full text-sm text-muted-foreground">{summary}</p>}
      </header>

      {grupos.length > 0 && (
        <div className="not-prose border-b px-4 py-3">
          <h4 className="mb-2 text-xs font-semibold tracking-wide text-muted-foreground uppercase">Parâmetros</h4>
          <div className="space-y-3">
            {grupos.map(({ local, itens }) => (
              <div key={local}>
                <p className="mb-1 text-[11px] font-medium text-muted-foreground">{rotulosLocal[local]}</p>
                <ul className="divide-y rounded-md border">
                  {itens.map((p) => (
                    <li key={p.name} className="flex flex-col gap-1 px-3 py-2 text-sm sm:flex-row sm:gap-4">
                      <div className="flex shrink-0 items-baseline gap-2 sm:w-48">
                        <code className="font-mono text-[13px] font-semibold">{p.name}</code>
                        {p.type && <span className="font-mono text-[11px] text-muted-foreground">{p.type}</span>}
                        {p.required && <span className="text-[10px] font-semibold text-destructive uppercase">obrigatório</span>}
                      </div>
                      {p.description && <div className="text-muted-foreground">{p.description}</div>}
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
        </div>
      )}

      {responses.length > 0 && (
        <div className="not-prose border-b px-4 py-3 last:border-b-0">
          <h4 className="mb-2 text-xs font-semibold tracking-wide text-muted-foreground uppercase">Respostas</h4>
          <ul className="space-y-1.5">
            {responses.map((r) => (
              <li key={r.status} className="flex flex-col gap-1 text-sm sm:flex-row sm:items-baseline sm:gap-3">
                <StatusBadge status={r.status} comNome={false} className="w-fit" />
                <span className="text-muted-foreground">
                  <span className="font-medium text-foreground">{nomeStatus(r.status)}</span>
                  {r.description && <> · {r.description}</>}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {children && <div className="prose-doc prose-sm px-4 py-1 [&>.not-prose]:my-4">{children}</div>}
    </section>
  )
}
