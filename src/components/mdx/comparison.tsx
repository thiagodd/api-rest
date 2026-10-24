import { Check, Minus, X } from 'lucide-react'
import { cn } from '@/lib/utils'

type Valor = React.ReactNode | boolean | null

export interface LinhaComparacao {
  label: React.ReactNode
  values: Valor[]
}

function Celula({ valor }: { valor: Valor }) {
  if (valor === true) return <Check className="mx-auto size-4 text-[var(--callout-tip)]" aria-label="Sim" />
  if (valor === false) return <X className="mx-auto size-4 text-[var(--callout-danger)]" aria-label="Não" />
  if (valor === null) return <Minus className="mx-auto size-4 text-muted-foreground" aria-label="Não se aplica" />
  return <>{valor}</>
}

/** Tabela comparativa (ex.: PUT × PATCH). `true`/`false`/`null` viram ícones. */
export function Comparison({
  columns,
  rows,
  highlight,
  title,
  caption,
}: {
  columns: string[]
  rows: LinhaComparacao[]
  /** Índice da coluna recomendada, destacada visualmente */
  highlight?: number
  title?: string
  caption?: React.ReactNode
}) {
  return (
    <figure className="not-prose my-6 overflow-hidden rounded-lg border">
      {title && <figcaption className="border-b bg-muted/50 px-4 py-2 text-sm font-semibold">{title}</figcaption>}
      <div className="scrollbar-fina overflow-x-auto">
        <table className="w-full min-w-[32rem] border-collapse text-sm">
          <thead>
            <tr className="border-b bg-muted/30">
              <th className="w-1/4 px-4 py-2.5 text-left font-medium text-muted-foreground" scope="col">
                <span className="sr-only">Critério</span>
              </th>
              {columns.map((c, i) => (
                <th key={c} scope="col" className={cn('px-4 py-2.5 text-left font-semibold', highlight === i && 'bg-primary/8 text-primary')}>
                  {c}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((linha, r) => (
              <tr key={r} className="border-b last:border-b-0">
                <th scope="row" className="px-4 py-2.5 text-left align-top font-medium">
                  {linha.label}
                </th>
                {columns.map((_, i) => (
                  <td key={i} className={cn('px-4 py-2.5 align-top leading-relaxed text-muted-foreground', highlight === i && 'bg-primary/5', typeof linha.values[i] === 'boolean' || linha.values[i] === null ? 'text-center' : '')}>
                    <Celula valor={linha.values[i]} />
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {caption && <p className="border-t px-4 py-2 text-xs text-muted-foreground">{caption}</p>}
    </figure>
  )
}
