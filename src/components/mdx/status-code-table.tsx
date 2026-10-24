import { RotateCw } from 'lucide-react'
import { useState } from 'react'
import { StatusBadge } from '@/components/badges'
import { corStatus, statusHttp } from '@/lib/http'
import { cn } from '@/lib/utils'

export interface StatusLinha {
  code: number
  /** Sobrescreve a explicação padrão de "quando usar" */
  when?: React.ReactNode
  /** Exemplo concreto no Bazar */
  example?: React.ReactNode
  /** Indica se o cliente pode repetir a requisição com segurança */
  retry?: boolean
}

const classes = [
  { id: 2, rotulo: '2xx Sucesso' },
  { id: 3, rotulo: '3xx Redirecionamento' },
  { id: 4, rotulo: '4xx Erro do cliente' },
  { id: 5, rotulo: '5xx Erro do servidor' },
]

/**
 * Tabela de status codes com quando usar cada um. Aceita números (usa a
 * explicação padrão) ou objetos com `when`, `example` e `retry`.
 */
export function StatusCodeTable({ codes, title, filter = true }: { codes: (number | StatusLinha)[]; title?: string; filter?: boolean }) {
  const linhas = codes.map((c) => (typeof c === 'number' ? { code: c } : c))
  const presentes = classes.filter((c) => linhas.some((l) => Math.floor(l.code / 100) === c.id))
  const [classe, setClasse] = useState<number | null>(null)
  const visiveis = classe ? linhas.filter((l) => Math.floor(l.code / 100) === classe) : linhas
  const temExemplo = linhas.some((l) => l.example)

  return (
    <figure className="not-prose my-6 overflow-hidden rounded-lg border">
      {(title || (filter && presentes.length > 1)) && (
        <div className="flex flex-wrap items-center gap-2 border-b bg-muted/40 px-4 py-2">
          {title && <figcaption className="mr-auto text-sm font-semibold">{title}</figcaption>}
          {filter && presentes.length > 1 && (
            <div className="flex flex-wrap gap-1" role="group" aria-label="Filtrar por classe">
              <BotaoFiltro ativo={classe === null} onClick={() => setClasse(null)}>
                Todos
              </BotaoFiltro>
              {presentes.map((c) => (
                <BotaoFiltro key={c.id} ativo={classe === c.id} onClick={() => setClasse(c.id)} cor={corStatus(c.id * 100)}>
                  {c.rotulo}
                </BotaoFiltro>
              ))}
            </div>
          )}
        </div>
      )}
      <ul className="divide-y">
        {visiveis.map((l) => (
          <li key={l.code} className={cn('grid gap-2 px-4 py-3 text-sm sm:grid-cols-[13rem_1fr]', temExemplo && 'lg:grid-cols-[13rem_1fr_1fr]')}>
            <div className="flex items-start gap-2">
              <StatusBadge status={l.code} className="whitespace-normal" />
              {l.retry && (
                <span title="Pode ser repetida (retentável)" className="mt-0.5 text-muted-foreground">
                  <RotateCw className="size-3.5" aria-label="Retentável" />
                </span>
              )}
            </div>
            <div className="leading-relaxed text-muted-foreground">{l.when ?? statusHttp[l.code]?.quando ?? '—'}</div>
            {temExemplo && <div className="leading-relaxed">{l.example && <span className="text-foreground/90">{l.example}</span>}</div>}
          </li>
        ))}
      </ul>
    </figure>
  )
}

function BotaoFiltro({ ativo, onClick, cor, children }: { ativo: boolean; onClick: () => void; cor?: string; children: React.ReactNode }) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={ativo}
      className={cn('rounded-md border px-2 py-0.5 text-xs transition-colors', ativo ? 'bg-background font-medium shadow-xs' : 'border-transparent text-muted-foreground hover:text-foreground')}
      style={ativo && cor ? { color: cor, borderColor: `color-mix(in oklch, ${cor} 40%, transparent)` } : undefined}
    >
      {children}
    </button>
  )
}
