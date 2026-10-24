import { Check, ListChecks } from 'lucide-react'
import { useProgresso } from '@/lib/progresso'
import { cn } from '@/lib/utils'
import { usePagina } from './contexto'

export interface ItemChecklist {
  id?: string
  label: React.ReactNode
  /** Texto auxiliar abaixo do item */
  hint?: React.ReactNode
}

/** Checklist marcável. As marcações ficam salvas no navegador, por página e por `id`. */
export function Checklist({ id = 'checklist', title, items }: { id?: string; title?: string; items: (string | ItemChecklist)[] }) {
  const { chave } = usePagina()
  const chaveCompleta = `${chave}:${id}`
  const marcados = useProgresso((s) => s.checklists[chaveCompleta]) ?? []
  const alternar = useProgresso((s) => s.alternarItemChecklist)
  const lista = items.map((item, i) => (typeof item === 'string' ? { id: String(i), label: item } : { ...item, id: item.id ?? String(i) }))
  const feitos = lista.filter((i) => marcados.includes(i.id)).length

  return (
    <section className="not-prose my-6 rounded-lg border bg-card">
      <header className="flex items-center gap-2 border-b px-4 py-2.5">
        <ListChecks className="size-4 text-primary" aria-hidden />
        <span className="text-sm font-semibold">{title ?? 'Checklist'}</span>
        <span className="ml-auto font-mono text-xs text-muted-foreground tabular-nums">
          {feitos}/{lista.length}
        </span>
      </header>
      <ul className="divide-y">
        {lista.map((item) => {
          const marcado = marcados.includes(item.id)
          return (
            <li key={item.id}>
              <label className="flex cursor-pointer items-start gap-3 px-4 py-2.5 transition-colors hover:bg-muted/40">
                <input type="checkbox" className="peer sr-only" checked={marcado} onChange={() => alternar(chaveCompleta, item.id)} />
                <span
                  className={cn(
                    'mt-0.5 flex size-4.5 shrink-0 items-center justify-center rounded border-2 transition-colors peer-focus-visible:ring-2 peer-focus-visible:ring-ring',
                    marcado ? 'border-primary bg-primary text-primary-foreground' : 'border-muted-foreground/40',
                  )}
                  aria-hidden
                >
                  {marcado && <Check className="size-3" strokeWidth={3} />}
                </span>
                <span className="text-sm leading-relaxed">
                  <span className={cn(marcado && 'text-muted-foreground line-through decoration-muted-foreground/50')}>{item.label}</span>
                  {item.hint && <span className="block text-xs text-muted-foreground">{item.hint}</span>}
                </span>
              </label>
            </li>
          )
        })}
      </ul>
    </section>
  )
}
