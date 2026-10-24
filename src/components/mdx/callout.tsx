import { Info, Lightbulb, OctagonAlert, TriangleAlert, type LucideIcon } from 'lucide-react'

type TipoCallout = 'info' | 'tip' | 'warning' | 'danger'

const configuracao: Record<TipoCallout, { icone: LucideIcon; titulo: string; cor: string }> = {
  info: { icone: Info, titulo: 'Nota', cor: 'var(--callout-info)' },
  tip: { icone: Lightbulb, titulo: 'Dica', cor: 'var(--callout-tip)' },
  warning: { icone: TriangleAlert, titulo: 'Atenção', cor: 'var(--callout-warning)' },
  danger: { icone: OctagonAlert, titulo: 'Cuidado', cor: 'var(--callout-danger)' },
}

export function Callout({ type = 'info', title, children }: { type?: TipoCallout; title?: string; children: React.ReactNode }) {
  const { icone: Icone, titulo, cor } = configuracao[type] ?? configuracao.info
  return (
    <aside
      role="note"
      className="my-6 flex gap-3 rounded-lg border px-4 py-3.5 text-sm leading-relaxed"
      style={{ borderColor: `color-mix(in oklch, ${cor} 35%, transparent)`, background: `color-mix(in oklch, ${cor} 7%, transparent)` }}
    >
      <Icone className="mt-0.5 size-4.5 shrink-0" style={{ color: cor }} aria-hidden />
      <div className="min-w-0 flex-1">
        <p className="not-prose mb-1 font-semibold" style={{ color: cor }}>
          {title ?? titulo}
        </p>
        <div className="prose-doc prose-sm [&>*:first-child]:mt-0 [&>*:last-child]:mb-0">{children}</div>
      </div>
    </aside>
  )
}
